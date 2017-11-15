package kikaha.caffeine;

import com.github.benmanes.caffeine.cache.LoadingCache;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import kikaha.core.modules.security.Session;
import kikaha.core.modules.security.SessionCookie;
import kikaha.core.test.HttpServerExchangeStub;
import kikaha.core.test.KikahaRunner;
import kikaha.core.util.Threads;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.inject.Named;

import static org.junit.Assert.assertEquals;

@RunWith(KikahaRunner.class)
public class CaffeineSessionStoreStressTest {

    @Inject CaffeineSessionStore sessionStore;
    @Inject SessionCookie sessionIdManager;

    @Inject @Named("session-cache")
    LoadingCache<String, Session> sessionCache;
    LoadingCacheWithCounter cacheWithMetrics;

    @Before
    public void setupMocks(){
        sessionStore.sessionCache
            = cacheWithMetrics
            = new LoadingCacheWithCounter( sessionStore.sessionCache );
    }

    @Test( timeout = 3000 )
    public void createManySessionsSimultaneously()
    {
        try (val threads = Threads.elasticPool()) {
            try ( val bg = threads.background() ) {
                for (int i = 0; i < 10; i++)
                    bg.run(() -> {
                        for (int j = 0; j < 100; j++)
                            sessionStore.createOrRetrieveSession(
                                    createExchange(j % 10), sessionIdManager);
                    });
            }
        }

        assertEquals( 10, cacheWithMetrics.counter.get() );
    }

    static HttpServerExchange createExchange(int n ){
        final HttpServerExchange exchange = HttpServerExchangeStub.createHttpExchange();
        exchange.getRequestHeaders().put( Headers.COOKIE, "JSESSIONID=" + n );
        return exchange;
    }

}
