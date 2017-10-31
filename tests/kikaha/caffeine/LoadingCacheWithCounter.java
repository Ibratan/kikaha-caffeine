package kikaha.caffeine;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Policy;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import kikaha.core.modules.security.Session;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@RequiredArgsConstructor
class LoadingCacheWithCounter implements LoadingCache<String, Session> {

    final AtomicInteger counter = new AtomicInteger();

    final LoadingCache<String, Session> target;

    @Override
    public Session getIfPresent(Object key) {
        return null;
    }

    @Override
    public Session get(String key, Function<? super String, ? extends Session> mappingFunction) {
        return null;
    }

    @Override
    public Map<String, Session> getAllPresent(Iterable<?> keys) {
        return null;
    }

    public void put(String k, Session v ) {
        counter.incrementAndGet();
        target.put(k, v);
    }

    @Override
    public Session get(String key) {
        return target.get(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Session> map) {

    }

    @Override
    public void invalidate(Object key) {

    }

    @Override
    public void invalidateAll(Iterable<?> keys) {

    }

    @Override
    public void invalidateAll() {

    }

    @Override
    public long estimatedSize() {
        return 0;
    }

    @Override
    public CacheStats stats() {
        return null;
    }

    @Override
    public ConcurrentMap<String, Session> asMap() {
        return null;
    }

    @Override
    public void cleanUp() {

    }

    @Override
    public Policy<String, Session> policy() {
        return null;
    }

    @Override
    public Map<String, Session> getAll(Iterable<? extends String> keys) {
        return null;
    }

    @Override
    public void refresh(String key) {

    }
}