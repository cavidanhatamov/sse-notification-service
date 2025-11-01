package com.learn.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested template does not exist.
 * Returns HTTP 400 Bad Request.
 */
@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TemplateNotFoundException extends RuntimeException {

    private final ErrorCode errorCode = ErrorCode.TEMPLATE_NOT_FOUND;
    private final String templateId;

    public TemplateNotFoundException(String templateId) {
        super(ErrorCode.TEMPLATE_NOT_FOUND.getDescription());
        this.templateId = templateId;
    }

}
