package com.learn.exception;

import lombok.Getter;

/**
 * Enum for all error codes in the application
 * Each error has a code and description for client-side handling
 */
@Getter
public enum ErrorCode {
    // Template related errors
    TEMPLATE_NOT_FOUND("TEMPLATE_001", "Template not found"),
    TEMPLATE_INVALID("TEMPLATE_002", "Template is invalid"),

    // Notification related errors
    NOTIFICATION_NOT_FOUND("NOTIFICATION_001", "Notification not found"),
    NOTIFICATION_INVALID("NOTIFICATION_002", "Notification is invalid"),

    // User related errors
    USER_NOT_FOUND("USER_001", "User not found"),
    USER_INVALID("USER_002", "User is invalid"),

    // Kafka related errors
    KAFKA_PUBLISH_FAILED("KAFKA_001", "Failed to publish message to Kafka");

    private final String code;
    private final String description;

    ErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

}
