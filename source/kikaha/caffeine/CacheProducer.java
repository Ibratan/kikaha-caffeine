package kikaha.caffeine;

import com.github.benmanes.caffeine.cache.*;
import kikaha.config.Config;
import kikaha.core.cdi.CDI;
import kikaha.core.cdi.ProviderContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * A producer of Caffeine caches.
 * Created by miere.teixeira on 27/10/2017.
 */
@SuppressWarnings("unchecked")
@Singleton @Slf4j
public class CacheProducer {

    static final CacheLoader EMPTY_CACHE_LOADER = s -> null;
    static final AsyncCacheLoader EMPTY_ASYNC_CACHE_LOADER = (key, executor) -> completedFuture(null);

    private final Map<String, Cache> caches = new HashMap<>();
    private final Map<String, LoadingCache> loadingCaches = new HashMap<>();
    private final Map<String, AsyncLoadingCache> asyncLoadingCaches = new HashMap<>();

    @Inject CDI cdi;
    @Inject Config config;

    @Produces Cache produceCache(ProviderContext context){
        final String name = getNameFrom( context );
        return new LazyCache( name );
    }

    @Produces LoadingCache produceLoadingCache(ProviderContext context){
        final String name = getNameFrom( context );
        return new LazyLoadingCache(
            () -> loadingCaches.computeIfAbsent( name,
                n -> buildLoadingCache( name, createNewCacheBuilder( name ) ))
        );
    }

    @Produces AsyncLoadingCache produceAsyncLoadingCache(ProviderContext context){
        final String name = getNameFrom( context );
        return new LazyAsyncLoadingCache( name );
    }

    private Caffeine<Object, Object> createNewCacheBuilder(String name ) {
        log.info( "Creating cache named " + name );

        final Caffeine<Object, Object> builder = Caffeine.newBuilder();
        configureExpirationBySize( name, builder );
        configureExpirationByTime( name, builder );
        configureWriterFor( name, builder );

        return builder;
    }

    private void configureExpirationBySize(String name, Caffeine<Object, Object> builder) {
        final long size = getLong( "server.cache." + name + ".maximum-size" ),
                   weight = getLong( "server.cache." + name + ".maximum-weight" );

        if ( size > 0 ) {
            log.info( "  >> maximum-size: " + size );
            builder.maximumSize(size);
        }
        if ( weight > 0 ) {
            log.info( "  >> maximum-weight: " + weight );
            builder.maximumWeight(weight);
        }
    }

    private void configureExpirationByTime( String name, Caffeine<Object, Object> builder ){
        final TimeUnit expirationTimeUnit = TimeUnit.valueOf( config.getString( "server.cache." + name + ".expiration.time-unit", "MINUTES" ) );

        final long accessTime = getLong( "server.cache." + name + ".expiration.time-after-access" ),
                   writeTime = getLong( "server.cache." + name + ".expiration.time-after-write" );

        if ( accessTime > 0 ) {
            log.info( "  >> expiration.time-after-access: " + accessTime + " " + expirationTimeUnit );
            builder.expireAfterAccess(accessTime, expirationTimeUnit);
        }
        if ( writeTime > 0 ) {
            log.info( "  >> expiration.time-after-write: " + writeTime + " " + expirationTimeUnit );
            builder.expireAfterWrite(writeTime, expirationTimeUnit);
        }
    }

    private void configureWriterFor( String name, Caffeine<Object, Object> builder ){
        final CacheWriter writer = cdi.load(CacheWriter.class, w -> name.equals( getNameFrom(w) ) );
        if ( writer != null ) {
            log.info( "  >> Configured with CacheWriter: " + writer );
            builder.writer(writer);
        } else
            log.info( "  >> No CacheWriter configured" );
    }

    private LoadingCache buildLoadingCache(String name, Caffeine<Object, Object> builder) {
        CacheLoader cacheLoader = cdi.load( CacheLoader.class, l -> name.equals( getNameFrom(l) ) );
        if ( cacheLoader == null ) {
            log.warn( "  >> No CacheLoader defined for. Ignoring...");
            cacheLoader = EMPTY_CACHE_LOADER;
        } else
            log.info( "  >> Configured with CacheLoader: " + cacheLoader );
        return builder.build(cacheLoader);
    }

    private AsyncLoadingCache buildAsyncLoadingCache(String name, Caffeine<Object, Object> builder) {
        AsyncCacheLoader cacheLoader = cdi.load( AsyncCacheLoader.class, l -> name.equals( getNameFrom(l) ) );
        if ( cacheLoader == null ) {
            log.warn( "  >> No CacheLoader defined. Ignoring...");
            cacheLoader = EMPTY_ASYNC_CACHE_LOADER;
        } else
            log.info( "  >> Configured with CacheLoader: " + cacheLoader );
        return builder.buildAsync(cacheLoader);
    }

    private String getNameFrom( @NonNull Object obj ) {
        final Named named = obj.getClass().getAnnotation(Named.class);
        return ( named == null ) ? null : named.value();
    }

    private String getNameFrom( @NonNull ProviderContext context ) {
        final Named named = context.getAnnotation(Named.class);
        if ( named == null )
            throw new UnsupportedOperationException( context + " should be annotated with @Named" );
        return named.value();
    }

    private long getLong( String path ) {
        Object v = config.getObject( path );
        if ( v == null )
            return 0;
        if ( v instanceof Integer )
            return Long.valueOf( (Integer)v );
        if ( v instanceof String )
            return Long.valueOf( (String)v );
        return (Long) v;
    }

    @RequiredArgsConstructor
    class LazyCache implements Cache<Object, Object> {

        private final String name;

        @Getter(lazy = true)
        private final Cache target = createCache();

        private Cache createCache() {
            return caches.computeIfAbsent( name, n -> createNewCacheBuilder( name ).build() );
        }

        @Delegate Cache getCache(){ return getTarget(); }
    }

    @RequiredArgsConstructor
    class LazyAsyncLoadingCache implements AsyncLoadingCache<Object, Object> {

        private final String name;

        @Getter(lazy = true)
        private final AsyncLoadingCache target = createCache();

        private AsyncLoadingCache createCache() {
            return asyncLoadingCaches.computeIfAbsent( name,
                    n -> buildAsyncLoadingCache( name, createNewCacheBuilder( name ) ));
        }

        @Delegate AsyncLoadingCache getCache(){ return getTarget(); }
    }
}
