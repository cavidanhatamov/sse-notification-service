package com.learn.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.StringUtils;

import com.learn.dto.NotificationFilterDTO;

/**
 * Specification pattern for dynamic notification queries
 */
public class NotificationSpecification {

    /**
     * Build dynamic criteria based on filter parameters
     */
    public static Criteria buildCriteria(String userId, NotificationFilterDTO filter) {
        List<Criteria> criteriaList = new ArrayList<>();

        // Always filter by userId (required)
        criteriaList.add(Criteria.where("userId").is(userId));

        // Always exclude disabled notifications unless specifically requested
        criteriaList.add(Criteria.where("disabled").is(false));

        // Apply optional filters
        if (filter != null) {
            // Read/Unread filter
            if (filter.getRead() != null) {
                criteriaList.add(Criteria.where("status.read").is(filter.getRead()));
            }

            // Channel filter
            if (StringUtils.hasText(filter.getChannel())) {
                criteriaList.add(Criteria.where("channel").is(filter.getChannel()));
            }

            // Priority filter
            if (StringUtils.hasText(filter.getPriority())) {
                criteriaList.add(Criteria.where("priority").is(filter.getPriority()));
            }
        }

        // Combine all criteria with AND operation
        return new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
    }

    /**
     * Get criteria for unread notifications only
     */
    public static Criteria unreadNotifications(String userId) {
        return new Criteria().andOperator(
                Criteria.where("userId").is(userId),
                Criteria.where("disabled").is(false),
                Criteria.where("status.read").is(false));
    }

    /**
     * Get criteria for read notifications only
     */
    public static Criteria readNotifications(String userId) {
        return new Criteria().andOperator(
                Criteria.where("userId").is(userId),
                Criteria.where("disabled").is(false),
                Criteria.where("status.read").is(true));
    }

    /**
     * Get criteria for notifications by channel
     */
    public static Criteria notificationsByChannel(String userId, String channel) {
        return new Criteria().andOperator(
                Criteria.where("userId").is(userId),
                Criteria.where("disabled").is(false),
                Criteria.where("channel").is(channel));
    }

    /**
     * Get criteria for notifications by priority
     */
    public static Criteria notificationsByPriority(String userId, String priority) {
        return new Criteria().andOperator(
                Criteria.where("userId").is(userId),
                Criteria.where("disabled").is(false),
                Criteria.where("priority").is(priority));
    }

    /**
     * Validate and get sort field
     */
    public static String validateSortField(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return "timestamps.createdAt";
        }

        // Allowed sort fields
        switch (sortBy.toLowerCase()) {
            case "createdat":
            case "created":
            case "timestamps.createdat":
                return "timestamps.createdAt";
            case "sentat":
            case "sent":
            case "timestamps.sentat":
                return "timestamps.sentAt";
            case "readat":
            case "read":
            case "timestamps.readat":
                return "timestamps.readAt";
            case "priority":
                return "priority";
            case "channel":
                return "channel";
            case "subject":
                return "subject";
            default:
                return "timestamps.createdAt"; // Default fallback
        }
    }
}
