package com.learn.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when publishing a message to Kafka fails.
 * Returns HTTP 500 Internal Server Error.
 */
@Getter
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class KafkaPublishException extends RuntimeException {

    private final ErrorCode errorCode = ErrorCode.KAFKA_PUBLISH_FAILED;
    private final String topic;

    public KafkaPublishException(String topic, Throwable cause) {
        super(ErrorCode.KAFKA_PUBLISH_FAILED.getDescription(), cause);
        this.topic = topic;
    }

}

