package com.learn.repository;

import com.learn.dto.NotificationFilterDTO;
import com.learn.model.Notification;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Custom repository interface for complex notification queries
 */
public interface NotificationRepositoryCustom {

    /**
     * Find notifications with filters and pagination
     * 
     * @param userId User ID to filter by
     * @param filter Filter criteria
     * @return Flux of filtered notifications
     */
    Flux<Notification> findNotificationsWithFilters(String userId, NotificationFilterDTO filter);

    /**
     * Count notifications with filters
     * 
     * @param userId User ID to filter by
     * @param filter Filter criteria
     * @return Count of matching notifications
     */
    Mono<Long> countNotificationsWithFilters(String userId, NotificationFilterDTO filter);
}
