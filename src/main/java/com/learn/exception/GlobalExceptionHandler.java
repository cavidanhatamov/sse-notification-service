package com.learn.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.server.ServerWebExchange;

import com.learn.exception.base.BaseErrorEnum;
import com.learn.exception.base.BaseErrorResponseDTO;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle TemplateNotFoundException
     */
    @ExceptionHandler(TemplateNotFoundException.class)
    public Mono<ResponseEntity<BaseErrorResponseDTO>> handleTemplateNotFoundException(
            TemplateNotFoundException ex, ServerWebExchange exchange) {
        log.error("Template not found: {}", ex.getTemplateId());
        
        Map<String, String> errorData = new HashMap<>();
        errorData.put("templateId", ex.getTemplateId());
        
        BaseErrorResponseDTO errorResponse = new BaseErrorResponseDTO(
                ex.getErrorCode().getCode(),
                ex.getErrorCode().getDescription(),
                exchange.getRequest().getPath().value(),
                LocalDateTime.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                errorData
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    /**
     * Handle NotificationNotFoundException
     */
    @ExceptionHandler(NotificationNotFoundException.class)
    public Mono<ResponseEntity<BaseErrorResponseDTO>> handleNotificationNotFoundException(
            NotificationNotFoundException ex, ServerWebExchange exchange) {
        log.error("Notification not found: {}", ex.getNotificationId());
        
        Map<String, String> errorData = new HashMap<>();
        errorData.put("notificationId", ex.getNotificationId());
        
        BaseErrorResponseDTO errorResponse = new BaseErrorResponseDTO(
                ex.getErrorCode().getCode(),
                ex.getErrorCode().getDescription(),
                exchange.getRequest().getPath().value(),
                LocalDateTime.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                errorData
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    /**
     * Handle KafkaPublishException
     */
    @ExceptionHandler(KafkaPublishException.class)
    public Mono<ResponseEntity<BaseErrorResponseDTO>> handleKafkaPublishException(
            KafkaPublishException ex, ServerWebExchange exchange) {
        log.error("Failed to publish to Kafka topic {}: {}", ex.getTopic(), ex.getMessage(), ex);
        
        Map<String, String> errorData = new HashMap<>();
        errorData.put("topic", ex.getTopic());
        
        BaseErrorResponseDTO errorResponse = new BaseErrorResponseDTO(
                ex.getErrorCode().getCode(),
                ex.getErrorCode().getDescription(),
                exchange.getRequest().getPath().value(),
                LocalDateTime.now().toString(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                errorData
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }

    /**
     * Handle validation errors (e.g., @Valid, @Pattern)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Mono<ResponseEntity<BaseErrorResponseDTO>> handleValidationException(MethodArgumentNotValidException ex,
            ServerWebExchange exchange) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Validation error: {}", errors);
        
        BaseErrorResponseDTO errorResponse = new BaseErrorResponseDTO(
                BaseErrorEnum.BASE_VALIDATION_ERROR.getErrorCode(),
                BaseErrorEnum.BASE_BUSINESS_ERROR.getMessage(),
                exchange.getRequest().getPath().value(),
                LocalDateTime.now().toString(),
                BaseErrorEnum.BASE_VALIDATION_ERROR.getHttpStatus(),
                errors
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    /**
     * Handle SSE timeout exceptions
     */
    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public Mono<ResponseEntity<BaseErrorResponseDTO>> handleAsyncTimeoutException(AsyncRequestTimeoutException exception,
            ServerWebExchange exchange) {
        log.warn("SSE stream timeout: {}", exception.getMessage());
        
        BaseErrorResponseDTO errorResponse = new BaseErrorResponseDTO(
                BaseErrorEnum.STREAM_CLOSED.getErrorCode(),
                BaseErrorEnum.STREAM_CLOSED.toString(),
                exchange.getRequest().getPath().value(),
                LocalDateTime.now().toString(),
                BaseErrorEnum.STREAM_CLOSED.getHttpStatus()
        );
        
        return Mono.just(ResponseEntity.status(HttpStatusCode.valueOf(BaseErrorEnum.STREAM_CLOSED.getHttpStatus()))
                .body(errorResponse));
    }

    /**
     * Handle all other unhandled exceptions
     */
    @ExceptionHandler(Throwable.class)
    public Mono<ResponseEntity<BaseErrorResponseDTO>> handleGeneralException(Exception ex,
            ServerWebExchange exchange) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        
        BaseErrorResponseDTO errorResponse = new BaseErrorResponseDTO(
                BaseErrorEnum.BASE_SERVER_ERROR.getErrorCode(),
                BaseErrorEnum.BASE_SERVER_ERROR.toString(),
                exchange.getRequest().getPath().value(),
                LocalDateTime.now().toString(),
                BaseErrorEnum.BASE_SERVER_ERROR.getHttpStatus()
        );
        
        return Mono.just(ResponseEntity.status(HttpStatusCode.valueOf(BaseErrorEnum.BASE_SERVER_ERROR.getHttpStatus()))
                .body(errorResponse));
    }
}
