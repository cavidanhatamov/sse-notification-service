package com.learn.dto;

import java.time.Instant;
import java.util.Map;

import com.learn.model.Notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard DTO for notification responses
 * Used in both API responses and SSE streaming for consistency
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private String id;
    private String subject;
    private String content;
    private String channel;
    private String priority;
    private boolean read;
    private Instant createdAt;
    private Map<String, Object> metadata;

    /**
     * Create DTO from Notification entity with specific language
     * 
     * @param notification Notification entity
     * @param language     Language code (e.g., "en", "az")
     * @return NotificationDTO with content in requested language
     */
    public static NotificationDTO fromNotification(Notification notification, String language) {
        String subject;
        String content;

        // Try to get rendered content for requested language
        if (notification.getRenderedContent() != null && !notification.getRenderedContent().isEmpty()) {
            com.learn.model.Notification.RenderedContent rendered = notification.getRenderedContent().get(language);

            // Fallback to English if requested language not available
            if (rendered == null && notification.getRenderedContent().containsKey("en")) {
                rendered = notification.getRenderedContent().get("en");
            }

            // Fallback to first available language
            if (rendered == null && !notification.getRenderedContent().isEmpty()) {
                rendered = notification.getRenderedContent().values().iterator().next();
            }

            if (rendered != null) {
                subject = rendered.getSubject();
                content = rendered.getContent();
            } else {
                // Ultimate fallback to deprecated fields
                subject = notification.getSubject();
                content = notification.getContent();
            }
        } else {
            // Backward compatibility: use deprecated subject/content fields
            subject = notification.getSubject();
            content = notification.getContent();
        }

        return new NotificationDTO(
                notification.getId(),
                subject,
                content,
                notification.getChannel(),
                notification.getPriority(),
                notification.getStatus() != null && notification.getStatus().isRead(),
                notification.getTimestamps() != null ? notification.getTimestamps().getCreatedAt() : null,
                notification.getMetadata());
    }

    /**
     * Create DTO from Notification entity (defaults to English)
     * 
     * @deprecated Use fromNotification(notification, language) instead
     */
    @Deprecated
    public static NotificationDTO fromNotification(Notification notification) {
        return fromNotification(notification, "en");
    }
}
