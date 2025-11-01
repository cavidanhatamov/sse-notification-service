package com.learn.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.learn.dto.NotificationFilterDTO;
import com.learn.model.Notification;
import com.learn.specification.NotificationSpecification;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Specification-based implementation for dynamic notification queries
 */
@Repository
public class NotificationRepositoryCustomImpl implements NotificationRepositoryCustom {

    private final ReactiveMongoTemplate mongoTemplate;

    public NotificationRepositoryCustomImpl(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Flux<Notification> findNotificationsWithFilters(String userId, NotificationFilterDTO filter) {
        // Use specification to build dynamic criteria
        Criteria criteria = NotificationSpecification.buildCriteria(userId, filter);
        Query query = new Query(criteria);

        // Apply pagination and sorting
        Pageable pageable = createPageable(filter);
        query.with(pageable);

        return mongoTemplate.find(query, Notification.class);
    }

    @Override
    public Mono<Long> countNotificationsWithFilters(String userId, NotificationFilterDTO filter) {
        // Use specification to build dynamic criteria (same logic, just counting)
        Criteria criteria = NotificationSpecification.buildCriteria(userId, filter);
        Query query = new Query(criteria);

        return mongoTemplate.count(query, Notification.class);
    }

    /**
     * Create pageable with validated sort field
     */
    private Pageable createPageable(NotificationFilterDTO filter) {
        // Validate and get safe sort field
        String sortField = NotificationSpecification.validateSortField(filter.getSortBy());

        // Create sort direction
        Sort.Direction direction = "ASC".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Sort sort = Sort.by(direction, sortField);

        return PageRequest.of(filter.getPage(), filter.getSize(), sort);
    }
}
