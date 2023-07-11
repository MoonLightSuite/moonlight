package io.github.moonlightsuite.moonlight.util;

import io.github.moonlightsuite.moonlight.core.base.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * <em>EXPERIMENTAL:</em> Utility class for tracking execution time.
 */
public class Stopwatch {
    private static final Map<UUID, Pair<Long, Long>> sessions = new HashMap<>();
    private final UUID sessionId;

    private Stopwatch(UUID id) {
        sessionId = id;
    }

    public static Stopwatch start() {
        UUID id = UUID.randomUUID();
        Long start = System.currentTimeMillis();
        sessions.put(id, new Pair<>(start, null));
        return new Stopwatch(id);
    }

    public long stop() {
        Long start = sessions.remove(sessionId).getFirst();
        Long end = System.currentTimeMillis();
        sessions.put(sessionId, new Pair<>(start, end));

        return end - start;
    }

    public static long getDuration(UUID sessionId) {
        Pair<Long, Long> session = sessions.get(sessionId);
        if(session != null && session.getSecond() != null) {
            return session.getSecond() - session.getFirst();
        } else
            throw new UnsupportedOperationException("The requested session " +
                                                    "never started or never " +
                                                    "ended");
    }

    public long getDuration() {
        Pair<Long, Long> session = sessions.get(sessionId);
        if(session != null && session.getSecond() != null) {
            return session.getSecond() - session.getFirst();
        } else
            throw new UnsupportedOperationException("The requested session " +
                                                    "never ended");
    }

    public static Map<UUID, Pair<Long, Long>> getSessions() {
        return new HashMap<>(sessions);
    }
}

