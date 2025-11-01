package com.learn.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learn.dto.NotificationDTO;
import com.learn.dto.NotificationFilterDTO;
import com.learn.dto.NotificationIdResponseDTO;
import com.learn.dto.NotificationRequestDTO;
import com.learn.dto.NotificationResponseDTO;
import com.learn.service.NotificationService;
import com.learn.service.SseSessionManager;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST API for notification management and real-time SSE streaming.
 */
@Tag(name = "Notifications", description = "Real-time notification management with SSE streaming")
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;
    private final SseSessionManager sseSessionManager;

    public NotificationController(
            NotificationService notificationService,
            SseSessionManager sseSessionManager) {
        this.notificationService = notificationService;
        this.sseSessionManager = sseSessionManager;
    }

    // ================ SSE REAL-TIME STREAMING ENDPOINTS ================

    /**
     * Subscribe to real-time notifications via Server-Sent Events (SSE)
     * <p>
     * Real-time notification delivery using MongoDB Change Streams with SSE format
     * <p>
     * Headers:
     * - Accept-Language: en, az, ru (default: az) - notifications will be streamed
     * in
     * this language
     * <p>
     * Usage:
     * - Frontend: new
     * EventSource('/api/v1/learn-sse/notifications/subscribe/user123')
     * - cURL: curl -N -H "Accept: text/event-stream" -H "Accept-Language: az"
     * "<a href="http://localhost:9090/api/v1/learn-sse/notifications/subscribe/user123">...</a>"
     * <p>
     * Features:
     * - Real-time SSE streaming via MongoDB Change Streams (no polling!)
     * - Multi-language support via Accept-Language header
     * - Reactive backpressure support
     * - Auto-reconnection support for clients
     * - Historical notifications sent first, then live updates
     */
    @Operation(summary = "Subscribe to real-time notifications via SSE with Change Streams", description = "Establishes a Server-Sent Events (SSE) connection for real-time notification streaming. Only one active connection per user is allowed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SSE stream established successfully"),
            @ApiResponse(responseCode = "408", description = "Request timeout - SSE connection timeout"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping(value = "/subscribe/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<NotificationDTO>> subscribeToNotifications(
            @Parameter(description = "User identifier", required = true, example = "user123") @PathVariable String userId,
            @Parameter(description = "Language code for notifications", example = "az") @RequestHeader(value = "Accept-Language", defaultValue = "az") @Pattern(regexp = "^(?i)(en|az|ru)$") String acceptLanguage) {
        String language = acceptLanguage.toLowerCase();
        log.debug("SSE subscription started for user: {} in language: {}", userId, language);

        Flux<NotificationDTO> notificationStream = notificationService.getNotificationStream(userId, language)
                .doOnNext(notification -> log.debug("Streaming SSE notification to user {}: {}", userId,
                        notification.getId()))
                .doOnCancel(() -> log.debug("SSE subscription cancelled for user: {}", userId))
                .doOnComplete(() -> log.debug("SSE subscription completed for user: {}", userId));

        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache")
                .header("Connection", "keep-alive")
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(notificationStream);
    }

    /**
     * Unsubscribe from SSE notifications (manually close connection)
     * <p>
     * Usage:
     * - POST /api/v1/learn-sse/notifications/unsubscribe/user123
     * - This will close any active SSE connection for the user
     */
    @Operation(summary = "Unsubscribe from SSE notifications", description = "Manually closes any active SSE connection for the specified user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully unsubscribed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/unsubscribe/{userId}")
    public ResponseEntity<Void> unsubscribeFromNotifications(
            @Parameter(description = "User identifier", required = true, example = "user123") @PathVariable String userId) {
        log.debug("Unsubscribe request received for user: {}", userId);
        sseSessionManager.closeConnection(userId);
        return ResponseEntity.ok().build();
    }

    // ================ NOTIFICATION MANAGEMENT ENDPOINTS ================

    /**
     * Get notifications with dynamic filtering and pagination
     * <p>
     * Headers:
     * - Accept-Language: en, az, ru (default: az)
     * <p>
     * Supported filters:
     * - read: true/false (filter by read/unread status)
     * - channel: EMAIL|SMS|PUSH (filter by channel)
     * - priority: LOW|NORMAL|HIGH|URGENT (filter by priority)
     * <p>
     * Pagination:
     * - page: page number (0-based, default: 0)
     * - size: page size (1-100, default: 20)
     * <p>
     * Sorting:
     * - sortBy: field name (default: timestamps.createdAt)
     * - sortDirection: ASC|DESC (default: DESC)
     * <p>
     * Examples:
     * - Get ALL notifications: /{userId}
     * - Get UNREAD notifications: /{userId}?read=false
     * - Get UNREAD COUNT: /{userId}?read=false&size=1 (check totalCount in
     * response)
     * - Get EMAIL notifications: /{userId}?channel=EMAIL
     * - Get HIGH PRIORITY notifications: /{userId}?priority=HIGH
     * - Paginate results: /{userId}?page=1&size=10
     * - Sort by priority: /{userId}?sortBy=priority&sortDirection=ASC
     */
    @Operation(summary = "Get filtered notifications with pagination", description = "Retrieves notifications for a user with optional filtering by read status, channel, priority, and pagination support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications", content = @Content(schema = @Schema(implementation = NotificationResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{userId}")
    public Mono<ResponseEntity<NotificationResponseDTO>> getFilteredNotifications(
            @Parameter(description = "User identifier", required = true, example = "user123") @PathVariable String userId,
            @Parameter(description = "Language code", example = "az") @RequestHeader(value = "Accept-Language", defaultValue = "az") @Pattern(regexp = "^(?i)(en|az|ru)$") String acceptLanguage,
            @Parameter(description = "Filter options for notifications") NotificationFilterDTO filter) {

        if (filter == null) {
            filter = new NotificationFilterDTO();
        }
        log.debug("Filtering notifications for user {} with filter: {}", userId, filter);
        String language = acceptLanguage.toLowerCase();
        return notificationService.getFilteredNotifications(userId, filter, language)
                .map(ResponseEntity::ok);
    }

    /**
     * Get a single notification by ID
     * Returns 400 Bad Request if notification not found
     * <p>
     * Headers:
     * - Accept-Language: en, az, ru (default: az)
     */
    @Operation(summary = "Get notification by ID", description = "Retrieves a specific notification by its unique identifier in the specified language")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved notification"),
            @ApiResponse(responseCode = "400", description = "Notification not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/notification/{notificationId}")
    public Mono<ResponseEntity<NotificationDTO>> getNotification(
            @Parameter(description = "Notification ID", required = true, example = "507f1f77bcf86cd799439011") @PathVariable String notificationId,
            @Parameter(description = "Language code", example = "az") @RequestHeader(value = "Accept-Language", defaultValue = "az") @Pattern(regexp = "^(?i)(en|az|ru)$") String acceptLanguage) {
        String language = acceptLanguage.toLowerCase();
        return notificationService.getNotificationById(notificationId, language)
                .map(ResponseEntity::ok);
    }

    /**
     * Mark a single notification as read
     * Returns 400 Bad Request if notification not found
     */
    @Operation(summary = "Mark notification as read", description = "Marks a specific notification as read by updating its status and timestamps")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully marked as read"),
            @ApiResponse(responseCode = "400", description = "Notification not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/notification/{notificationId}/read")
    public Mono<Void> markNotificationAsRead(
            @Parameter(description = "Notification ID", required = true, example = "507f1f77bcf86cd799439011") @PathVariable String notificationId) {
        return notificationService.markNotificationAsRead(notificationId)
                .then(); // Returns empty Mono for 200 OK
    }

    /**
     * Mark all notifications as read for a user
     */
    @Operation(summary = "Mark all notifications as read", description = "Marks all notifications for a specific user as read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully marked all as read"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{userId}/mark-all-read")
    public Mono<Void> markAllNotificationsAsRead(
            @Parameter(description = "User identifier", required = true, example = "user123") @PathVariable String userId) {
        return notificationService.markAllNotificationsAsRead(userId).then();
    }

    /**
     * Softly delete (disable) all notifications for a user
     */
    @Operation(summary = "Disable all notifications", description = "Soft deletes all notifications for a user by marking them as disabled")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully disabled all notifications"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{userId}/disable-all")
    public Mono<Void> disableAllNotifications(
            @Parameter(description = "User identifier", required = true, example = "user123") @PathVariable String userId) {
        return notificationService.disableAllNotifications(userId)
                .then(); // 200 OK
    }

    // ================ BASIC NOTIFICATION SENDING ================

    /**
     * Send a notification via REST API
     *
     * <p>
     * This endpoint generates a notification ID, publishes the request to Kafka,
     * and returns the ID immediately. The actual processing (template rendering in
     * ALL languages and DB save) is handled asynchronously by
     * {@link com.learn.consumer.NotificationConsumer}.
     *
     * <h3>Flow:</h3>
     * <ol>
     * <li>REST API receives POST /send with NotificationRequestDTO</li>
     * <li>Controller generates a unique notification ID</li>
     * <li>Request is published to Kafka with ID as header</li>
     * <li>Returns 202 ACCEPTED with notification ID immediately</li>
     * <li>Kafka consumer processes message: renders template in ALL languages</li>
     * <li>Consumer saves notification to MongoDB with the pre-generated ID</li>
     * <li>MongoDB Change Streams push notification to SSE clients</li>
     * </ol>
     *
     * <p>
     * <b>Benefits of this approach:</b>
     * <ul>
     * <li>Fast response - returns ID immediately without waiting for processing</li>
     * <li>Immediate tracking - client gets ID before processing completes</li>
     * <li>Async processing - heavy template rendering happens in the background</li>
     * <li>Scalable - Kafka consumers can be scaled independently</li>
     * <li>Reliable - Kafka handles retry logic for failed processing</li>
     * </ul>
     *
     * <p>
     * <b>Multi-Language Support:</b> Templates are automatically rendered in ALL
     * supported languages (en, az, ru) and stored in the renderedContent map. SSE
     * clients receive content in their requested language via the Accept-Language
     * header.
     *
     * <p>
     * <b>Note:</b> External systems can also publish directly to the Kafka topic
     * "notification-requests" for asynchronous processing via
     * {@link com.learn.consumer.NotificationConsumer}.
     *
     * @param notificationRequest Notification request with template ID, user ID,
     *                            and parameters
     * @return 202 ACCEPTED with notification ID
     */
    @Operation(summary = "Send a notification", description = "Generates a notification ID, publishes to Kafka, and returns the ID immediately. Templates are rendered in ALL languages asynchronously.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Notification accepted for processing", content = @Content(schema = @Schema(implementation = NotificationIdResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "500", description = "Failed to publish to Kafka")
    })
    @PostMapping("/send")
    public Mono<ResponseEntity<NotificationIdResponseDTO>> sendNotification(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Notification request with template ID, user ID, and template parameters", required = true, content = @Content(schema = @Schema(implementation = NotificationRequestDTO.class))) @Valid @RequestBody NotificationRequestDTO notificationRequest) {

        log.debug("REST API: Received notification request for user: {} with template: {}",
                notificationRequest.getUserId(), notificationRequest.getTemplateId());

        // Publish to Kafka with generated ID and return immediately
        return notificationService.publishNotificationRequest(notificationRequest)
                .map(notificationId -> {
                    log.debug("REST API: Notification request published to Kafka with ID: {} for user: {}",
                            notificationId, notificationRequest.getUserId());
                    return ResponseEntity.status(HttpStatus.ACCEPTED)
                            .body(NotificationIdResponseDTO.of(notificationId));
                });
    }

}
