package com.learn.service.impl;

import java.time.Instant;
import java.util.List;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.learn.dto.NotificationDTO;
import com.learn.dto.NotificationFilterDTO;
import com.learn.dto.NotificationRequestDTO;
import com.learn.dto.NotificationResponseDTO;
import com.learn.exception.KafkaPublishException;
import com.learn.exception.NotificationNotFoundException;
import com.learn.exception.TemplateNotFoundException;
import com.learn.model.Notification;
import com.learn.repository.NotificationRepository;
import com.learn.repository.TemplateRepository;
import com.learn.service.NotificationService;
import com.learn.service.SseSessionManager;
import com.learn.service.TemplateRenderingService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementation of a notification service with SSE streaming support using MongoDB
 * Change Streams.
 */
@Service
public class NotificationServiceImpl implements NotificationService {

        private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

        private final NotificationRepository notificationRepository;
        private final SseSessionManager sseSessionManager;
        private final TemplateRenderingService templateRenderingService;
        private final KafkaTemplate<String, NotificationRequestDTO> kafkaTemplate;
        private final ReactiveMongoTemplate mongoTemplate;
        private final TemplateRepository templateRepository;

        @Value("${app.sse.max-connection-duration}")
        private int maxConnectionDuration;

        @Value("${app.sse.connection-timeout}")
        private int connectionTimeout;

        @Value("${app.kafka.topics.notification-requests}")
        private String notificationRequestsTopic;

        public NotificationServiceImpl(
                        NotificationRepository notificationRepository,
                        SseSessionManager sseSessionManager,
                        TemplateRenderingService templateRenderingService,
                        KafkaTemplate<String, NotificationRequestDTO> kafkaTemplate,
                        ReactiveMongoTemplate mongoTemplate,
                        TemplateRepository templateRepository) {
                this.notificationRepository = notificationRepository;
                this.sseSessionManager = sseSessionManager;
                this.templateRenderingService = templateRenderingService;
                this.kafkaTemplate = kafkaTemplate;
                this.mongoTemplate = mongoTemplate;
                this.templateRepository = templateRepository;
        }

        @Override
        public Mono<Void> markNotificationAsRead(String notificationId) {
                return notificationRepository.markAsRead(notificationId, Instant.now())
                                .flatMap(updateCount -> {
                                        if (updateCount == 0) {
                                                return Mono.error(new NotificationNotFoundException(notificationId));
                                        }
                                        log.debug("Marked notification as read: {}", notificationId);
                                        return Mono.empty();
                                });
        }

        @Override
        public Mono<Void> markAllNotificationsAsRead(String userId) {
                return notificationRepository.markAllAsReadByUserId(userId, Instant.now())
                                .doOnSuccess(
                                                updateCount -> log.debug(
                                                                "✅ Marked {} notifications as read for user: {}",
                                                                updateCount, userId))
                                .then();
        }

        @Override
        public Mono<Void> disableAllNotifications(String userId) {
                return notificationRepository.disableAllByUserId(userId)
                                .doOnSuccess(
                                                updateCount -> log.debug("🗑️ Disabled {} notifications for user: {}",
                                                                updateCount, userId))
                                .then();
        }

        @Override
        public Mono<Void> deleteAllDisabledNotifications(String userId) {
                return notificationRepository.deleteByUserIdAndDisabledTrue(userId)
                                .doOnSuccess(deleteCount -> log.debug(
                                                "💀 Permanently deleted {} disabled notifications for user: {}",
                                                deleteCount, userId))
                                .then();
        }

        @Override
        public Mono<String> saveNotification(Notification notification) {
                log.debug("💾 Saving notification to MongoDB: {} for user: {}", notification.getId(),
                                notification.getUserId());
                return notificationRepository.save(notification)
                                .map(Notification::getId);
        }

        @Override
        public Mono<NotificationDTO> getNotificationById(String notificationId) {
                return getNotificationById(notificationId, "az");
        }

        @Override
        public Mono<NotificationDTO> getNotificationById(String notificationId, String language) {
                return notificationRepository.findById(notificationId)
                                .filter(notification -> !notification.isDisabled())
                                .map(notification -> NotificationDTO.fromNotification(notification, language))
                                .switchIfEmpty(Mono.defer(() -> 
                                        Mono.error(new NotificationNotFoundException(notificationId))));
        }

        @Override
        public Mono<NotificationResponseDTO> getFilteredNotifications(String userId, NotificationFilterDTO filter) {
                return getFilteredNotifications(userId, filter, "az");
        }

        @Override
        public Mono<NotificationResponseDTO> getFilteredNotifications(String userId, NotificationFilterDTO filter,
                        String language) {
                Mono<List<Notification>> notificationsMono = notificationRepository
                                .findNotificationsWithFilters(userId, filter)
                                .collectList();

                Mono<Long> countMono = notificationRepository
                                .countNotificationsWithFilters(userId, filter);

                return Mono.zip(notificationsMono, countMono)
                                .map(tuple -> {
                                        List<Notification> notifications = tuple.getT1();
                                        Long totalCount = tuple.getT2();
                                        return NotificationResponseDTO.of(notifications, totalCount, filter.getPage(),
                                                        filter.getSize(),
                                                        language);
                                });
        }

        @Override
        public Flux<NotificationDTO> getNotificationStream(String userId) {
                return getNotificationStream(userId, "az");
        }

        @Override
        @SuppressWarnings("unused")
        public Flux<NotificationDTO> getNotificationStream(String userId, String language) {
                java.time.Instant connectionStartTime = java.time.Instant.now();

                Mono<Void> cancellationSignal = sseSessionManager.createConnection(userId);

                // 1. Get historical unsent notifications first
                Flux<NotificationDTO> historicalNotifications = getUnsentNotifications(userId)
                                .flatMap(notification -> markNotificationAsSent(notification.getId())
                                                .thenReturn(notification))
                                .map(notification -> NotificationDTO.fromNotification(notification, language))
                                .doOnNext(dto -> log.debug("Sent historical notification: {} to user: {}",
                                                dto.getId(), userId));

                // 2. Watch for new notifications via MongoDB Change Streams
                Flux<NotificationDTO> liveNotifications = watchNotificationChanges(userId)
                                .takeWhile(notification -> {
                                        java.time.Duration elapsed = java.time.Duration.between(connectionStartTime,
                                                        java.time.Instant.now());
                                        if (elapsed.getSeconds() >= maxConnectionDuration) {
                                                log.debug("SSE connection for user {} reached max duration ({}s), terminating",
                                                                userId, maxConnectionDuration);
                                                return false;
                                        }
                                        return true;
                                })
                                .flatMap(notification -> markNotificationAsSent(notification.getId())
                                                .thenReturn(notification))
                                .map(notification -> NotificationDTO.fromNotification(notification, language))
                                .doOnNext(dto -> log.debug("Sent live notification: {} to user: {}",
                                                dto.getId(), userId));

                // 3. Combine historical and live notifications
                return Flux.concat(historicalNotifications, liveNotifications)
                                .timeout(java.time.Duration.ofSeconds(connectionTimeout))
                                .takeUntilOther(cancellationSignal)
                                .doOnSubscribe(subscription -> log.debug(
                                                "Started SSE stream with Change Streams for user: {} in language: {} (max duration: {}s, timeout: {}s)",
                                                userId, language, maxConnectionDuration, connectionTimeout))
                                .doOnCancel(() -> log.debug("Cancelled SSE stream for user: {}", userId))
                                .doOnComplete(() -> log.debug("SSE stream completed for user: {}", userId))
                                .onErrorResume(java.util.concurrent.TimeoutException.class, error -> {
                                        log.warn("SSE connection timeout for user: {} after {}s", userId,
                                                        connectionTimeout);
                                        return Flux.empty();
                                });
        }

        /**
         * Watch MongoDB Change Streams for new notifications inserted for a specific
         * user.
         */
        private Flux<Notification> watchNotificationChanges(String userId) {
                // MongoDB Change Stream aggregation pipeline to filter by userId and insert
                // operations only
                Aggregation aggregation = Aggregation.newAggregation(
                                Aggregation.match(Criteria.where("operationType").is("insert")
                                                .and("fullDocument.userId").is(userId)
                                                .and("fullDocument.disabled").is(false)
                                                .and("fullDocument.status.sent").is(false)));

                return mongoTemplate
                                .changeStream("notifications", ChangeStreamOptions.builder()
                                                .filter(aggregation)
                                                .build(), Notification.class)
                                .map(ChangeStreamEvent::getBody)
                                .filter(java.util.Objects::nonNull)
                                .doOnNext(notification -> log.debug(
                                                "Change Stream: New notification inserted for user {}: {}",
                                                userId, notification.getId()))
                                .onErrorResume(error -> {
                                        log.debug("Change Stream error for user {}, resuming with empty stream: {}", 
                                                        userId, error.getMessage());
                                        return Flux.empty();
                                });
        }

        private Flux<Notification> getUnsentNotifications(String userId) {
                return notificationRepository.findByUserIdAndStatus_SentFalseAndDisabledFalse(userId)
                                .doOnNext(notification -> log.debug("Found unsent notification: {} for user: {}",
                                                notification.getId(), userId))
                                .filter(notification -> !notification.isDisabled());
        }

        private Mono<Boolean> markNotificationAsSent(String notificationId) {
                return notificationRepository.markAsSent(notificationId, java.time.Instant.now())
                                .map(updateCount -> updateCount > 0)
                                .doOnSuccess(success -> {
                                        if (success) {
                                                log.debug("Marked notification as sent via SSE: {}", notificationId);
                                        }
                                });
        }

        @Override
        public Mono<String> processAndSaveNotification(NotificationRequestDTO notificationRequest) {
                return processAndSaveNotification(notificationRequest, null);
        }

        @Override
        public Mono<String> processAndSaveNotification(NotificationRequestDTO notificationRequest,
                        String notificationId) {
                log.debug("🚀 Processing notification: template={}, user={}, pregenerated-id={}",
                                notificationRequest.getTemplateId(), notificationRequest.getUserId(),
                                notificationId != null ? notificationId : "will generate");

                // Build notification entity from DTO
                Notification notification = buildNotificationFromDTO(notificationRequest);

                // Use provided ID or generate a new one using MongoDB ObjectId (strongly unique in distributed systems)
                notification.setId(notificationId != null ? notificationId : new ObjectId().toHexString());

                // Render template -> Save to MongoDB
                return Mono.just(notification)
                                .flatMap(templateRenderingService::renderNotification)
                                .doOnNext(rendered -> log.debug("Template rendered for notification {}: subject='{}'",
                                                rendered.getId(), rendered.getSubject()))
                                .flatMap(this::saveNotification)
                                .doOnSuccess(savedId -> log.debug(
                                                "Notification processed and saved: {} for user: {}",
                                                savedId, notification.getUserId()));
        }

        @Override
        @SuppressWarnings("unused")
        public Mono<String> publishNotificationRequest(NotificationRequestDTO notificationRequest) {
                // Validate template existence before publishing to Kafka
                return templateRepository.existsById(notificationRequest.getTemplateId())
                                .flatMap(exists -> {
                                        if (!exists) {
                                                return Mono.error(new TemplateNotFoundException(
                                                                notificationRequest.getTemplateId()));
                                        }

                                        // Generate notification ID upfront using MongoDB ObjectId (strongly unique in distributed systems)
                                        String notificationId = new ObjectId().toHexString();

                                        log.debug("Publishing notification request to Kafka with ID: {} for user: {} with template: {}",
                                                        notificationId, notificationRequest.getUserId(),
                                                        notificationRequest.getTemplateId());

                                        // Build message with a custom header: "notificationId"
                                        Message<NotificationRequestDTO> message = MessageBuilder
                                                        .withPayload(notificationRequest)
                                                        .setHeader(KafkaHeaders.TOPIC, notificationRequestsTopic)
                                                        .setHeader(KafkaHeaders.KEY, notificationRequest.getUserId())
                                                        .setHeader("notificationId", notificationId)
                                                        .build();

                                        return Mono.fromFuture(
                                                        kafkaTemplate.send(message)
                                                                        .thenApply(sendResult -> {
                                                                                log.debug("Successfully published to Kafka with ID: {} for user: {}",
                                                                                                notificationId,
                                                                                                notificationRequest.getUserId());
                                                                                return notificationId;
                                                                        })
                                                                        .exceptionally(ex -> {
                                                                                throw new KafkaPublishException(
                                                                                                notificationRequestsTopic,
                                                                                                ex);
                                                                        })
                                                                        .toCompletableFuture());
                                });
        }

        private Notification buildNotificationFromDTO(NotificationRequestDTO dto) {
                Notification notification = new Notification();

                notification.setTemplateId(dto.getTemplateId());
                notification.setUserId(dto.getUserId());
                notification.setChannel(dto.getChannel());
                notification.setPriority(dto.getPriority() != null ? dto.getPriority() : "NORMAL");
                notification.setSourceSystem(dto.getSourceSystem());
                notification.setParams(dto.getParams());
                notification.setMetadata(dto.getMetadata());
                notification.setDisabled(false);

                notification.setStatus(buildNotificationStatus());
                notification.setTimestamps(buildNotificationTimestamps());

                return notification;
        }

        private Notification.NotificationStatus buildNotificationStatus() {
                Notification.NotificationStatus status = new Notification.NotificationStatus();
                status.setSent(false);
                status.setRead(false);
                return status;
        }

        private Notification.NotificationTimestamps buildNotificationTimestamps() {
                Notification.NotificationTimestamps timestamps = new Notification.NotificationTimestamps();
                timestamps.setCreatedAt(Instant.now());
                return timestamps;
        }
}
