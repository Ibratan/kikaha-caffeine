package kikaha.caffeine;

import com.github.benmanes.caffeine.cache.AsyncCacheLoader;
import lombok.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import javax.inject.*;

@Singleton @Named("not-configured-async")
public class NonConfiguredAsyncCacheLoader implements AsyncCacheLoader<String, Integer> {

    @Override
    public CompletableFuture<Integer> asyncLoad(String key, Executor executor) {
        return CompletableFuture.supplyAsync(() -> Integer.valueOf( key ) + 20, executor);
    }
}
