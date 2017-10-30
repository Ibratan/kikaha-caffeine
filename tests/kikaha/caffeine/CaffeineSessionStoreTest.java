package kikaha.caffeine;

import static org.junit.Assert.*;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import kikaha.core.modules.security.DefaultSession;
import kikaha.core.modules.security.Session;
import kikaha.core.modules.security.SessionCookie;
import kikaha.core.test.HttpServerExchangeStub;
import kikaha.core.test.KikahaRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

/**
 * Unit tests for {@link CaffeineSessionStoreTest}.
 * Created by miere.teixeira on 30/10/2017.
 */
@RunWith(KikahaRunner.class)
public class CaffeineSessionStoreTest {

    final static String
        SESSION_ID = "123",

        MSG_NO_SESSION_REQUIRED_BEFORE_CREATION = "Should there be no session stored on the cache before it can be created",
        MSG_NO_SESSION_REQUIRED_AFTER_INVALIDATION = "Should there be no session stored on the cache after it is invalidated"
    ;

    @Inject CaffeineSessionStore sessionStore;
    @Inject SessionCookie sessionIdManager;

    @Before
    public void cleanUpSession(){
        sessionStore.sessionCache.invalidateAll();
    }

    @Test( timeout = 3000 )
    public void createSession() throws Exception {
        assertNull(MSG_NO_SESSION_REQUIRED_BEFORE_CREATION, sessionStore.sessionCache.getIfPresent( SESSION_ID ) );

        final Session session = sessionStore.createOrRetrieveSession( createExchange(), sessionIdManager );
        assertNotNull( session );
        assertEquals( SESSION_ID, session.getId() );
        assertEquals( session, sessionStore.sessionCache.getIfPresent( SESSION_ID ) );
    }

    @Test
    public void retrieveSession() throws Exception {
        assertNull(MSG_NO_SESSION_REQUIRED_BEFORE_CREATION, sessionStore.sessionCache.getIfPresent( SESSION_ID ) );
        final Session session = new DefaultSession( SESSION_ID );
        sessionStore.sessionCache.put( SESSION_ID, session );

        final Session foundSession = sessionStore.createOrRetrieveSession( createExchange(), sessionIdManager );
        assertEquals( session, foundSession );
    }

    @Test
    public void invalidateSession() throws Exception {
        assertNull(MSG_NO_SESSION_REQUIRED_BEFORE_CREATION, sessionStore.sessionCache.getIfPresent( SESSION_ID ) );
        final Session session = new DefaultSession( SESSION_ID );
        sessionStore.sessionCache.put( SESSION_ID, session );

        sessionStore.invalidateSession( session );
        assertNull(MSG_NO_SESSION_REQUIRED_AFTER_INVALIDATION, sessionStore.sessionCache.getIfPresent( SESSION_ID ) );
    }

    @Test
    public void flush() throws Exception {
        assertNull(MSG_NO_SESSION_REQUIRED_BEFORE_CREATION, sessionStore.sessionCache.getIfPresent( SESSION_ID ) );
        final Session session = new DefaultSession( SESSION_ID );
        sessionStore.sessionCache.put( SESSION_ID, session );

        session.setAttribute( "a", "b" );
        sessionStore.flush( session );

        final Session found = sessionStore.sessionCache.getIfPresent( SESSION_ID );
        assertNotNull( found );
        assertEquals( "b", found.getAttribute( "a" ) );
    }

    @Test
    public void getSessionFromCache() throws Exception {
        assertNull(MSG_NO_SESSION_REQUIRED_BEFORE_CREATION, sessionStore.sessionCache.getIfPresent( SESSION_ID ) );
        final Session session = new DefaultSession( SESSION_ID );
        sessionStore.sessionCache.put( SESSION_ID, session );

        final Session found = sessionStore.getSessionFromCache( SESSION_ID );
        assertEquals( session, found );
    }

    @Test
    public void storeSession() throws Exception {
        assertNull(MSG_NO_SESSION_REQUIRED_BEFORE_CREATION, sessionStore.sessionCache.getIfPresent( SESSION_ID ) );
        final Session session = new DefaultSession( SESSION_ID );

        sessionStore.storeSession( SESSION_ID, session );

        final Session found = sessionStore.sessionCache.getIfPresent( SESSION_ID );
        assertNotNull( found );
        assertEquals( session, found );
    }

    static HttpServerExchange createExchange(){
        final HttpServerExchange exchange = HttpServerExchangeStub.createHttpExchange();
        exchange.getRequestHeaders().put( Headers.COOKIE, "JSESSIONID=" + SESSION_ID );
        return exchange;
    }
}