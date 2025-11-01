package com.learn.service.impl;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.learn.service.SseSessionManager;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * Implementation of SSE session management.
 * Maintains a thread-safe map of active connections and enforces
 * single-session-per-user policy.
 */
@Service
public class SseSessionManagerImpl implements SseSessionManager {

    private static final Logger log = LoggerFactory.getLogger(SseSessionManagerImpl.class);
    private final ConcurrentHashMap<String, Sinks.One<Void>> activeConnections = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unused")
    public Mono<Void> createConnection(String userId) {
        Sinks.One<Void> oldSink = activeConnections.get(userId);
        if (oldSink != null) {
            log.debug("🔄 Closing old SSE connection for user: {} (new connection requested)", userId);
            oldSink.tryEmitEmpty();
        }

        Sinks.One<Void> newSink = Sinks.one();
        activeConnections.put(userId, newSink);
        log.debug("Created new SSE connection for user: {}", userId);

        return newSink.asMono()
                .doFinally(signal -> {
                    activeConnections.remove(userId, newSink);
                    log.debug("Removed SSE connection for user: {}", userId);
                });
    }

    @Override
    public void closeConnection(String userId) {
        Sinks.One<Void> sink = activeConnections.remove(userId);
        if (sink != null) {
            log.debug("Manually closing SSE connection for user: {}", userId);
            sink.tryEmitEmpty();
        }
    }
}
