package kikaha.caffeine;

import com.github.benmanes.caffeine.cache.*;
import kikaha.config.Config;
import kikaha.core.cdi.CDI;
import kikaha.core.cdi.ProviderContext;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by miere.teixeira on 27/10/2017.
 */
@Singleton @Slf4j
public class CacheProducer {

    final Map<String, Cache> caches = new HashMap<>();
    final Map<String, LoadingCache> loadingCaches = new HashMap<>();
    final Map<String, AsyncLoadingCache> asyncLoadingCaches = new HashMap<>();

    @Inject CDI cdi;
    @Inject Config config;

    @Produces Cache produceCache(ProviderContext context){
        final String name = getNameFrom( context );
        return caches.computeIfAbsent( name,
                n -> buildCache( name, createNewCacheBuilder( name ) ));
    }

    @Produces LoadingCache produceLoadingCache(ProviderContext context){
        final String name = getNameFrom( context );
        return loadingCaches.computeIfAbsent( name,
                n -> buildLoadingCache( name, createNewCacheBuilder( name ) ));
    }

    @Produces AsyncLoadingCache produceAsyncLoadingCache(ProviderContext context){
        final String name = getNameFrom( context );
        return asyncLoadingCaches.computeIfAbsent( name,
                n -> buildAsyncLoadingCache( name, createNewCacheBuilder( name ) ));
    }

    private Caffeine<Object, Object> createNewCacheBuilder(String name ) {

        final Caffeine<Object, Object> builder = Caffeine.newBuilder();
        configureExpirationBySize( name, builder );
        configureExpirationByTime( name, builder );
        configureWriterFor( name, builder );

        return builder;
    }

    private void configureExpirationBySize(String name, Caffeine<Object, Object> builder) {
        final long size = getLong( "server.cache." + name + ".maximum-size" ),
                   weight = getLong( "server.cache." + name + ".maximum-weight" );

        if ( size > 0 )
            builder.maximumSize( size );
        if ( weight > 0 )
            builder.maximumWeight( weight );
    }

    private void configureExpirationByTime( String name, Caffeine<Object, Object> builder ){
        final TimeUnit expirationTimeUnit = TimeUnit.valueOf( config.getString( "server.cache." + name + ".expiration.time-unit", "MINUTES" ) );

        final long accessTime = getLong( "server.cache." + name + ".expiration.time-after-access" ),
                   writeTime = getLong( "server.cache." + name + ".expiration.time-after-write" );

        if ( accessTime > 0 )
            builder.expireAfterAccess( accessTime, expirationTimeUnit );
        if ( writeTime > 0 )
            builder.expireAfterWrite( writeTime, expirationTimeUnit );
    }

    private void configureWriterFor( String name, Caffeine<Object, Object> builder ){
        final CacheWriter writer = cdi.load(CacheWriter.class, w -> name.equals( getNameFrom(w) ) );
        if ( writer != null ) {
            log.info( "  >>  configured with CacheWriter: " + writer );
            builder.writer(writer);
        }
    }

    private Cache buildCache(String name, Caffeine<Object, Object> builder) {
        log.info( "Configuring cache: " + name );
        final CacheLoader cacheLoader = cdi.load( CacheLoader.class, l -> name.equals( getNameFrom(l) ) );
        if ( cacheLoader == null )
            log.warn( "  >> Cache would not have a CacheLoader. Ignoring." );
        return builder.build();
    }

    private LoadingCache buildLoadingCache(String name, Caffeine<Object, Object> builder) {
        log.info( "Configuring cache: " + name );
        final CacheLoader cacheLoader = cdi.load( CacheLoader.class, l -> name.equals( getNameFrom(l) ) );
        if ( cacheLoader == null )
            throw new RuntimeException( "No CacheLoader defined for LoadingCache named " + name );
        log.info( "  >>  configured with CacheLoader: " + cacheLoader );
        return builder.build(cacheLoader);
    }

    private AsyncLoadingCache buildAsyncLoadingCache(String name, Caffeine<Object, Object> builder) {
        log.info( "Configuring cache: " + name );
        final AsyncCacheLoader cacheLoader = cdi.load( AsyncCacheLoader.class, l -> name.equals( getNameFrom(l) ) );
        if ( cacheLoader == null )
            throw new RuntimeException( "No CacheLoader defined for LoadingCache named " + name );
        log.info( "  >>  configured with CacheLoader: " + cacheLoader );
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

    long getLong( String path ) {
        Object v = config.getObject( path );
        if ( v == null )
            return 0;
        if ( v instanceof Integer )
            return Long.valueOf( (Integer)v );
        if ( v instanceof String )
            return Long.valueOf( (String)v );
        return (Long) v;
    }
}