package kikaha.caffeine;

import com.github.benmanes.caffeine.cache.CacheLoader;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton @Named("configured") @Slf4j
public class ConfiguredCacheLoader implements CacheLoader<String, Integer> {

    @Override
    public Integer load(String s) throws Exception {
        log.warn( "Loading cached data for: " + s );
        return Integer.valueOf( s ) + 10;
    }
}
