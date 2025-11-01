package com.learn.service;

import com.learn.dto.NotificationDTO;
import com.learn.dto.NotificationFilterDTO;
import com.learn.dto.NotificationRequestDTO;
import com.learn.dto.NotificationResponseDTO;
import com.learn.model.Notification;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service for managing notification operations including CRUD, filtering, and
 * real-time streaming.
 */
public interface NotificationService {

    /**
     * Streams real-time notifications for a user via SSE (default language).
     *
     * @param userId the user identifier
     * @return Flux of notification DTOs
     */
    Flux<NotificationDTO> getNotificationStream(String userId);

    /**
     * Streams real-time notifications for a user via SSE in specified language.
     *
     * @param userId   the user identifier
     * @param language the language code (en, az, ru)
     * @return Flux of notification DTOs
     */
    Flux<NotificationDTO> getNotificationStream(String userId, String language);

    /**
     * Marks a notification as read.
     *
     * @param notificationId the notification identifier
     * @return Mono that completes when operation finishes
     */
    Mono<Void> markNotificationAsRead(String notificationId);

    /**
     * Marks all notifications as read for a user.
     *
     * @param userId the user identifier
     * @return Mono that completes when operation finishes
     */
    Mono<Void> markAllNotificationsAsRead(String userId);

    /**
     * Soft deletes all notifications for a user.
     *
     * @param userId the user identifier
     * @return Mono that completes when operation finishes
     */
    Mono<Void> disableAllNotifications(String userId);

    /**
     * Permanently deletes all disabled notifications for a user.
     *
     * @param userId the user identifier
     * @return Mono that completes when operation finishes
     */
    Mono<Void> deleteAllDisabledNotifications(String userId);

    /**
     * Saves a notification to the database.
     *
     * @param notification the notification entity
     * @return Mono with the notification ID
     */
    Mono<String> saveNotification(Notification notification);

    /**
     * Retrieves a notification by ID (default language).
     *
     * @param notificationId the notification identifier
     * @return Mono with notification DTO
     */
    Mono<NotificationDTO> getNotificationById(String notificationId);

    /**
     * Retrieves a notification by ID in specified language.
     *
     * @param notificationId the notification identifier
     * @param language       the language code (en, az, ru)
     * @return Mono with notification DTO
     */
    Mono<NotificationDTO> getNotificationById(String notificationId, String language);

    /**
     * Retrieves filtered and paginated notifications (default language).
     *
     * @param userId the user identifier
     * @param filter the filter criteria
     * @return Mono with paginated notification response
     */
    Mono<NotificationResponseDTO> getFilteredNotifications(String userId, NotificationFilterDTO filter);

    /**
     * Retrieves filtered and paginated notifications in specified language.
     *
     * @param userId   the user identifier
     * @param filter   the filter criteria
     * @param language the language code (en, az, ru)
     * @return Mono with paginated notification response
     */
    Mono<NotificationResponseDTO> getFilteredNotifications(String userId, NotificationFilterDTO filter,
            String language);

    /**
     * Processes a notification request: maps DTO, renders template, and saves to
     * database.
     *
     * @param notificationRequest the notification request DTO (includes language)
     * @return Mono with the notification ID
     */
    Mono<String> processAndSaveNotification(NotificationRequestDTO notificationRequest);

    /**
     * Processes a notification request with a pre-generated ID: maps DTO, renders
     * template, and saves to database with the specified ID.
     *
     * @param notificationRequest the notification request DTO (includes language)
     * @param notificationId      pre-generated notification ID (if null, generates
     *                            new ID)
     * @return Mono with the notification ID
     */
    Mono<String> processAndSaveNotification(NotificationRequestDTO notificationRequest, String notificationId);

    /**
     * Publishes a notification request to Kafka for asynchronous processing.
     *
     * <p>
     * This method generates a notification ID, publishes the request to Kafka with
     * the ID as a header, and returns the ID immediately. The actual processing
     * (template rendering in ALL languages + DB save) is handled by
     * {@link com.learn.consumer.NotificationConsumer}.
     *
     * @param notificationRequest the notification request DTO
     * @return Mono with the generated notification ID
     */
    Mono<String> publishNotificationRequest(NotificationRequestDTO notificationRequest);
}
