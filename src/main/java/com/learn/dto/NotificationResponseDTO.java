package com.learn.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.learn.model.Notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO that contains both notifications and count
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDTO {

    private List<NotificationDTO> notifications;
    private long totalCount;
    private int currentPage;
    private int pageSize;
    private long totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    // Custom constructor with automatic calculation of pagination fields
    public NotificationResponseDTO(List<NotificationDTO> notifications, long totalCount,
            int currentPage, int pageSize) {
        this.notifications = notifications;
        this.totalCount = totalCount;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPages = (totalCount + pageSize - 1) / pageSize; // Ceiling division
        this.hasNext = currentPage < totalPages - 1;
        this.hasPrevious = currentPage > 0;
    }

    // Static factory methods for common use cases

    /**
     * Create response DTO with notifications in specific language
     */
    public static NotificationResponseDTO of(List<Notification> notifications, long totalCount,
            int currentPage, int pageSize, String language) {
        List<NotificationDTO> notificationDTOs = notifications.stream()
                .map(notification -> NotificationDTO.fromNotification(notification, language))
                .collect(Collectors.toList());
        return new NotificationResponseDTO(notificationDTOs, totalCount, currentPage, pageSize);
    }

    /**
     * Create response DTO (defaults to English)
     * 
     * @deprecated Use of(notifications, totalCount, currentPage, pageSize,
     *             language) instead
     */
    @Deprecated
    public static NotificationResponseDTO of(List<Notification> notifications, long totalCount,
            int currentPage, int pageSize) {
        return of(notifications, totalCount, currentPage, pageSize, "en");
    }

    public static NotificationResponseDTO empty(int currentPage, int pageSize) {
        return new NotificationResponseDTO(List.of(), 0L, currentPage, pageSize);
    }

    // Custom toString for better logging
    @Override
    public String toString() {
        return "NotificationResponseDTO{" +
                "notificationCount=" + (notifications != null ? notifications.size() : 0) +
                ", totalCount=" + totalCount +
                ", currentPage=" + currentPage +
                ", pageSize=" + pageSize +
                ", totalPages=" + totalPages +
                ", hasNext=" + hasNext +
                ", hasPrevious=" + hasPrevious +
                '}';
    }
}
