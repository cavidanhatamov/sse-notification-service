package com.learn.service;

import reactor.core.publisher.Mono;

/**
 * Manages Server-Sent Events (SSE) sessions.
 * Ensures only one active SSE connection per user.
 */
public interface SseSessionManager {

    /**
     * Creates a new SSE connection for the user.
     * If an existing connection exists, it will be closed automatically.
     *
     * @param userId the user identifier
     * @return Mono that completes when the connection should be terminated
     */
    Mono<Void> createConnection(String userId);

    /**
     * Manually closes an active SSE connection for the user.
     *
     * @param userId the user identifier
     */
    void closeConnection(String userId);
}
