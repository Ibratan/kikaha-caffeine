package kikaha.caffeine;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.LoadingCache;
import kikaha.core.test.KikahaRunner;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.inject.Named;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(KikahaRunner.class)
public class CacheTest {

    @Named("not-configured")
    @Inject Cache<String, Integer> notConfigured;

    @Named("not-configured-async")
    @Inject AsyncLoadingCache<String, Integer> notConfiguredAsync;

    @Named("configured")
    @Inject LoadingCache<String, Integer> configured;

    @Inject NonConfiguredCacheWriter nonConfiguredCacheWriter;

    @Before
    public void clear(){
        notConfigured.invalidateAll();
        configured.invalidateAll();
    }

    @Test
    public void canInjectCacheIntoObjects(){
        assertNotNull( configured );
        assertNotNull( notConfigured );
    }

    @Test
    public void willHoldOnly100ObjectsInMemoryOnNonConfiguredCaches(){
        for (int i = 0; i < 200; i++) {
            notConfigured.put( String.valueOf( i ), i );
        }

        awaitEviction();
        assertEquals( 200, notConfigured.estimatedSize() );
    }

    @Test
    public void willHoldOnly100ObjectsInMemoryOnTheConfiguredCache(){
        for (int i = 0; i < 100; i++) {
            configured.put( String.valueOf( i ), i );
        }

        awaitEviction();
        assertEquals( 100, configured.estimatedSize() );
    }

    @Test
    public void willStoreAllDataIntoTheCacheWriter(){
        for (int i = 0; i < 200; i++) {
            notConfigured.put( String.valueOf( i ), i );
        }

        awaitEviction();
        assertEquals( 200, nonConfiguredCacheWriter.data.size() );
    }

    @SneakyThrows static void awaitEviction(){
        Thread.sleep( 3000L );
    }

    @Test @SneakyThrows
    public void willReadDataFromCacheLoader(){
        int computed = configured.get( "2" );
        assertEquals( 12, computed );
    }

    @Test @SneakyThrows
    public void willReadDataFromAsyncCacheLoader(){
        int computed = notConfiguredAsync.get( "2" ).get();
        assertEquals( 22, computed );
    }
}
