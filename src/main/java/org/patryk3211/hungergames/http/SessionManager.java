package org.patryk3211.hungergames.http;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.*;

public class SessionManager {
    private static class SessionData {
        public Instant timeout;

        public SessionData(long timeout) {
            this.timeout = Instant.now().plusSeconds(timeout);
        }

        public boolean timedOut() {
            return timeout.isBefore(Instant.now());
        }
    }

    private final long sessionTimeout;

    private final Map<UUID, SessionData> authorizedSessions = new HashMap<>();
    private final Map<String, String> credentials = new HashMap<>();

    public SessionManager(long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public void addCredentials(String user, String password) {
        credentials.put(user, password);
    }

    public UUID newSession(String user, String password) {
        String passCorrect = credentials.get(user);
        // Null oznacza że użytkownik nie istnieje w naszej liscie
        if(passCorrect == null)
            return null;
        // Hasła się nie zgadzają
        if(!passCorrect.equals(password))
            return null;
        // Generujemy nowe ID i dodajemy je do naszej listy sesji
        UUID id = UUID.randomUUID();
        authorizedSessions.put(id, new SessionData(sessionTimeout));
        return id;
    }

    public boolean isAuthorized(UUID id) {
        SessionData data = authorizedSessions.get(id);
        if(data == null)
            // Sesja nie istnieje w naszej liście
            return false;
        if(data.timedOut()) {
            // Sesja wygasła
            authorizedSessions.remove(id);
            return false;
        }
        // Sesja autoryzowana, zaktualizuj czas wygaśnięcia
        data.timeout = Instant.now().plusSeconds(sessionTimeout);
        return true;
    }

    public void removeSession(UUID id) {
        authorizedSessions.remove(id);
    }
}
