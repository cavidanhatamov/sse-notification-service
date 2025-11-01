package com.learn.exception.base;

import lombok.Getter;

/**
 * Enum representing a set of base error types.
 * Each error includes a specific error code, message, and corresponding HTTP status code.
 * Implements the {@link BaseErrorService} interface.
 */
@Getter
public enum BaseErrorEnum implements BaseErrorService {
    /**
     * Business-related error.
     */
    BASE_BUSINESS_ERROR("BASE-BUSINESS-ERROR-001", "BASE_BUSINESS_ERROR", 400),

    /**
     * Technical error.
     */
    BASE_TECH_ERROR("BASE-TECH-ERROR-001", "BASE_TECH_ERROR", 500),

    /**
     * Gateway timeout error.
     */
    BASE_GATEWAY_TIMEOUT("BASE-GATEWAY-TIMEOUT-001", "BASE_GATEWAY_TIMEOUT", 504),

    /**
     * Server-related error.
     */
    BASE_SERVER_ERROR("BASE-SERVER-ERROR-001", "BASE_SERVER_ERROR", 503),

    /**
     * Server-related error.
     */
    STREAM_CLOSED("BASE-SERVER-ERROR-002", "STREAM_CLOSED", 503),

    /**
     * Validation-related error.
     */
    BASE_VALIDATION_ERROR("BASE-VALIDATION-ERROR-001", "BASE_VALIDATION_ERROR", 400);


    /**
     * Error message.
     * -- GETTER --
     *  Gets the error message.
     *
     * @return The error message.

     */
    final String message;

    /**
     * HTTP status code corresponding to the error.
     * -- GETTER --
     *  Gets the HTTP status code.
     *
     * @return The HTTP status code.

     */
    final int httpStatus;

    /**
     * Unique error code.
     * -- GETTER --
     *  Gets the unique error code.
     *
     * @return The error code.

     */
    final String errorCode;

    /**
     * Constructor for the BaseErrorEnum.
     *
     * @param errorCode  The unique error code.
     * @param message    The error message.
     * @param httpStatus The HTTP status code associated with the error.
     */
    BaseErrorEnum(String errorCode, String message, int httpStatus) {
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatus = httpStatus;
    }

}

