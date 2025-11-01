package com.learn.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested notification does not exist.
 * Returns HTTP 400 Bad Request.
 */
@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NotificationNotFoundException extends RuntimeException {

    private final ErrorCode errorCode = ErrorCode.NOTIFICATION_NOT_FOUND;
    private final String notificationId;

    public NotificationNotFoundException(String notificationId) {
        super(ErrorCode.NOTIFICATION_NOT_FOUND.getDescription());
        this.notificationId = notificationId;
    }

}
