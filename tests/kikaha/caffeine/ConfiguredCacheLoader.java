package kikaha.caffeine;

import com.github.benmanes.caffeine.cache.CacheLoader;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import javax.inject.*;

@Singleton @Named("configured") @Slf4j
public class ConfiguredCacheLoader implements CacheLoader<String, Integer> {

    @Override
    public Integer load(String s) throws Exception {
        log.warn( "Loading cached data for: " + s );
        return Integer.valueOf( s ) + 10;
    }
}
