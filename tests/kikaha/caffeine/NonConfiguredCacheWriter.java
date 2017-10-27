package kikaha.caffeine;

import com.github.benmanes.caffeine.cache.CacheWriter;
import com.github.benmanes.caffeine.cache.RemovalCause;

import lombok.*;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.*;

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
        if ( !removalCause.wasEvicted() ) {
            System.out.println("Delete" + s);
            data.remove(s);
        }
    }
}
