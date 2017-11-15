package kikaha.caffeine;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Policy;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor
class LazyLoadingCache implements LoadingCache<Object, Object> {

    private final Supplier<LoadingCache> supplier;

    @Getter(lazy = true)
    private final LoadingCache target = supplier.get();

    @Override
    public Object get(Object o) {
        return getTarget().get(o);
    }

    @Override
    public Map<Object, Object> getAll(Iterable<?> iterable) {
        return getTarget().getAll(iterable);
    }

    @Override
    public void refresh(Object o) {
        getTarget().refresh(o);
    }

    @Override
    public Object getIfPresent(Object o) {
        return getTarget().getIfPresent(o);
    }

    @Override
    public Object get(Object o, Function<? super Object, ?> function) {
        return getTarget().get(o, function);
    }

    @Override
    public Map<Object, Object> getAllPresent(Iterable<?> iterable) {
        return getTarget().getAllPresent( iterable );
    }

    @Override
    public void put(Object o, Object o2) {
        getTarget().put(o, o2);
    }

    @Override
    public void putAll(Map<?, ?> map) {
        getTarget().putAll( map );
    }

    @Override
    public void invalidate(Object o) {
        getTarget().invalidate(o);
    }

    @Override
    public void invalidateAll(Iterable<?> iterable) {
        getTarget().invalidateAll( iterable );
    }

    @Override
    public void invalidateAll() {
        getTarget().invalidateAll();
    }

    @Override
    public long estimatedSize() {
        return getTarget().estimatedSize();
    }

    @Override
    public CacheStats stats() {
        return getTarget().stats();
    }

    @Override
    public ConcurrentMap<Object, Object> asMap() {
        return getTarget().asMap();
    }

    @Override
    public void cleanUp() {
        getTarget().cleanUp();
    }

    @Override
    public Policy<Object, Object> policy() {
        return getTarget().policy();
    }
}