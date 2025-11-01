package com.learn.model;

import java.time.Instant;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    private String templateId;

    private String userId;

    private String channel;
    private String subject; // Deprecated - kept for backward compatibility
    private String content; // Deprecated - kept for backward compatibility
    private Map<String, RenderedContent> renderedContent; // Multi-language content
    private Map<String, Object> params;

    private Map<String, Object> metadata;

    private NotificationStatus status;

    private NotificationTimestamps timestamps;

    private String priority;
    private String sourceSystem;
    private boolean disabled = false; // For soft delete functionality

    // Constructors
    public Notification() {
    }

    public Notification(String id, String templateId, String userId, String channel,
            String subject, String content, Map<String, Object> params,
            Map<String, Object> metadata, NotificationStatus status,
            NotificationTimestamps timestamps, String priority, String sourceSystem, boolean disabled) {
        this.id = id;
        this.templateId = templateId;
        this.userId = userId;
        this.channel = channel;
        this.subject = subject;
        this.content = content;
        this.params = params;
        this.metadata = metadata;
        this.status = status;
        this.timestamps = timestamps;
        this.priority = priority;
        this.sourceSystem = sourceSystem;
        this.disabled = disabled;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Map<String, RenderedContent> getRenderedContent() {
        return renderedContent;
    }

    public void setRenderedContent(Map<String, RenderedContent> renderedContent) {
        this.renderedContent = renderedContent;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public NotificationTimestamps getTimestamps() {
        return timestamps;
    }

    public void setTimestamps(NotificationTimestamps timestamps) {
        this.timestamps = timestamps;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    // Inner classes

    /**
     * Rendered content for a specific language
     */
    public static class RenderedContent {
        private String subject;
        private String content;

        public RenderedContent() {
        }

        public RenderedContent(String subject, String content) {
            this.subject = subject;
            this.content = content;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public static class NotificationStatus {
        private boolean sent;
        private boolean read;

        public NotificationStatus() {
        }

        public NotificationStatus(boolean sent, boolean read) {
            this.sent = sent;
            this.read = read;
        }

        // Getters and Setters
        public boolean isSent() {
            return sent;
        }

        public void setSent(boolean sent) {
            this.sent = sent;
        }

        public boolean isRead() {
            return read;
        }

        public void setRead(boolean read) {
            this.read = read;
        }
    }

    public static class NotificationTimestamps {
        private Instant createdAt;
        private Instant sentAt;
        private Instant readAt;

        public NotificationTimestamps() {
        }

        public NotificationTimestamps(Instant createdAt, Instant sentAt, Instant readAt) {
            this.createdAt = createdAt;
            this.sentAt = sentAt;
            this.readAt = readAt;
        }

        // Getters and Setters
        public Instant getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }

        public Instant getSentAt() {
            return sentAt;
        }

        public void setSentAt(Instant sentAt) {
            this.sentAt = sentAt;
        }

        public Instant getReadAt() {
            return readAt;
        }

        public void setReadAt(Instant readAt) {
            this.readAt = readAt;
        }
    }
}
