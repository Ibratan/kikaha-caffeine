package kikaha.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import io.undertow.server.HttpServerExchange;
import kikaha.core.modules.security.DefaultSession;
import kikaha.core.modules.security.Session;
import kikaha.core.modules.security.SessionIdManager;
import kikaha.core.modules.security.SessionStore;
import lombok.*;

import java.util.*;
import javax.inject.*;

@Singleton
public class CaffeineSessionStore implements SessionStore {

    @Inject @Named("session-cache")
    Cache<String, Session> sessionCache;

    @Override
    public Session createOrRetrieveSession( HttpServerExchange exchange, SessionIdManager sessionIdManager ) {
        final String sessionId = sessionIdManager.retrieveSessionIdFrom( exchange );
        return sessionCache.get( sessionId, s -> this.createAndStoreNewSession( s, exchange, sessionIdManager ));
    }

    @Override
    public Session createAndStoreNewSession(String sessionId, HttpServerExchange exchange, SessionIdManager sessionIdManager) {
        final Session session = new DefaultSession( sessionId );
        sessionIdManager.attachSessionId(exchange, session.getId());
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
