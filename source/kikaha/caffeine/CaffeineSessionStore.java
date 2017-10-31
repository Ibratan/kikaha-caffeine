package kikaha.caffeine;

import com.github.benmanes.caffeine.cache.LoadingCache;
import io.undertow.server.HttpServerExchange;
import kikaha.core.modules.security.Session;
import kikaha.core.modules.security.SessionIdManager;
import kikaha.core.modules.security.SessionStore;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Singleton @Slf4j
public class CaffeineSessionStore implements SessionStore {

    private final Lock lock = new ReentrantLock();

    @Inject @Named("session-cache")
    LoadingCache<String, Session> sessionCache;

    @Override
    public Session createOrRetrieveSession( HttpServerExchange exchange, SessionIdManager sessionIdManager ) {
        final String sessionId = sessionIdManager.retrieveSessionIdFrom(exchange);

        Session session = sessionCache.get( sessionId );
        if ( session == null )
            session = tryToCreateAndStoreNewSession(sessionId, exchange, sessionIdManager);

        return session;
    }

    @Override
    public Session tryToCreateAndStoreNewSession(String sessionId, HttpServerExchange exchange, SessionIdManager sessionIdManager) {
        Session session;

        lock.lock();
        try {
            session = sessionCache.get(sessionId);
            if ( session == null ) {
                session = createAndStoreNewSession(sessionId, exchange, sessionIdManager);
                System.out.println( Thread.currentThread().getName() + ": Session created: " + sessionId );
            }
        } finally {
            lock.unlock();
        }

        return session;
    }

    @Override
    public void invalidateSession( Session session ) {
        sessionCache.invalidate( session.getId() );
    }

    @Override
    public void flush( Session currentSession ) {
        storeSession( currentSession.getId(), currentSession );
    }

    @Override
    public Session getSessionFromCache( String sessionId ) {
        return sessionCache.getIfPresent( sessionId );
    }

    @Override
    public void storeSession( String sessionId, Session session ) {
        sessionCache.put( sessionId, session );
    }
}
