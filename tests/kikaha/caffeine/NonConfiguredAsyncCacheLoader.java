package kikaha.caffeine;

import com.github.benmanes.caffeine.cache.AsyncCacheLoader;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Singleton @Named("not-configured-async")
public class NonConfiguredAsyncCacheLoader implements AsyncCacheLoader<String, Integer> {

    @Override
    public CompletableFuture<Integer> asyncLoad(String key, Executor executor) {
        return CompletableFuture.supplyAsync(() -> Integer.valueOf( key ) + 20, executor);
    }
}
