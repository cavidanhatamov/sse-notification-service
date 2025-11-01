package com.learn.consumer;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.learn.dto.NotificationRequestDTO;
import com.learn.service.NotificationService;

/**
 * Kafka consumer for processing notification creation requests.
 *
 * <p>
 * This is the <b>SINGLE POINT OF PROCESSING</b> for all notifications,
 * regardless of origin (REST API or external Kafka publishers).
 *
 * <h3>Flow:</h3>
 * <ol>
 * <li>Notification request published to a Kafka topic
 * "notification-requests"</li>
 * <li>This consumer receives and processes the request</li>
 * <li>Renders template in all languages</li>
 * <li>Saves notification to MongoDB</li>
 * <li>MongoDB Change Streams detect the insert</li>
 * <li>SSE clients receive the notification via a subscribed endpoint</li>
 * </ol>
 *
 * <h3>Sources of Notification Requests:</h3>
 * <ul>
 * <li><b>REST API:</b> POST /send → publishes to Kafka → this consumer</li>
 * <li><b>External Systems:</b> Direct publish to Kafka → this consumer</li>
 * </ul>
 *
 * <h3>Benefits of Single Consumer Approach:</h3>
 * <ul>
 * <li><b>Single Responsibility:</b> All processing logic in one place</li>
 * <li><b>Consistency:</b> Same behavior for all notification sources</li>
 * <li><b>Scalability:</b> Easy to scale by adding consumer instances</li>
 * <li><b>Monitoring:</b> Single point to monitor/debug processing</li>
 * <li><b>Retry Logic:</b> Unified error handling and retry mechanism</li>
 * </ul>
 */
@Component
public class NotificationConsumer {

        private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

        private final NotificationService notificationService;

        public NotificationConsumer(NotificationService notificationService) {
                this.notificationService = notificationService;
        }

        /**
         * Processes ALL notification creation requests from Kafka.
         *
         * <p>
         * This method handles rendering, saving, and triggering SSE delivery.
         *
         * <p>
         * Headers extracted from Kafka message:
         * <ul>
         * <li><b>notificationId:</b> Pre-generated notification ID from REST API
         * (optional - if not present, will generate new ObjectId)</li>
         * </ul>
         *
         * <p>
         * Language is taken from the request DTO body. Template content is rendered in
         * ALL languages and stored in MongoDB. SSE delivery returns content based on
         *  the client's Accept-Language header.
         *
         * @param request        Notification request from a Kafka topic (includes
         *                       language in DTO)
         * @param notificationId Pre-generated notification ID from REST API (optional -
         *                       generates if null)
         */
        @KafkaListener(topics = "${app.kafka.topics.notification-requests}", groupId = "${app.kafka.consumer.group-id}")
        public void consumeNotificationRequest(
                        @Payload NotificationRequestDTO request,
                        @Header(value = "notificationId", required = false) String notificationId) {

                // Generate notification ID if not provided (for external Kafka publishers)
                String finalNotificationId = (notificationId != null && !notificationId.isEmpty())
                                ? notificationId
                                : new ObjectId().toHexString();

                log.debug("Kafka: Received notification request for user: {} with template: {} (ID: {})",
                                request.getUserId(), request.getTemplateId(), finalNotificationId);

                try {
                        // Process notification: render template in ALL languages → save to MongoDB with ID
                        notificationService.processAndSaveNotification(request, finalNotificationId)
                                        .doOnSuccess(
                                                        savedId -> log.debug(
                                                                        "Kafka: Notification created successfully: {} for user: {} (rendered in all languages)",
                                                                        savedId, request.getUserId()))
                                        .doOnError(error -> log.error(
                                                        "Kafka: Failed to process notification request for user {}: {}",
                                                        request.getUserId(), error.getMessage(), error))
                                        .subscribe(); // Subscribe to trigger the reactive chain

                } catch (Exception e) {
                        log.error("Kafka: Exception while processing notification request for user {}: {}",
                                        request.getUserId(), e.getMessage(), e);
                        // Kafka will handle retry based on consumer configuration
                        throw e;
                }
        }
}
