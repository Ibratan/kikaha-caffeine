package kikaha.caffeine;

import com.github.benmanes.caffeine.cache.CacheWriter;
import com.github.benmanes.caffeine.cache.RemovalCause;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by miere.teixeira on 27/10/2017.
 */
@Singleton @Named( "not-configured" )
public class NonConfiguredCacheWriter implements CacheWriter<String, Integer> {

    final Map<String, Integer> data = new HashMap<>();

    @Override
    public void write(String s, Integer integer) {
        data.put( s, integer );
    }

    @Override
    public void delete(String s, Integer integer, RemovalCause removalCause) {
        if ( !removalCause.wasEvicted() ) data.remove(s);
    }
}
