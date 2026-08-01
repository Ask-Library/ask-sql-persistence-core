package pe.ask.persistence.core.exception;

import pe.ask.exception.core.catalog.BaseExceptionCatalog;

import java.util.Map;

/**
 * Catalog of errors for the persistence core module.
 * <p>
 * Defines specific error codes, names, messages, and HTTP status codes
 * that can be used throughout the persistence layer.
 * </p>
 *
 * @author Allan Sagastegui
 */
public enum ErrorCatalog implements BaseExceptionCatalog {
    /**
     * Error indicating that mapping between domain and entity failed.
     */
    MAP_FAILED(
            "ERR-PERSISTENCE-001",
            "MAP_FAILED",
            "An unexpected error occurred while mapping between the domain object and the persistence entity.",
            500,
            null
    ),
    ILLEGAL_STATE(
            "ERR-PERSISTENCE-002",
            "ILLEGAL STATE",
            "The operation could not be completed because the application is in an invalid state.",
            500,
            null
    );

    private final String errorCode;
    private final String exceptionName;
    private final String message;
    private final int status;
    private final Map<String, String> errors;

    ErrorCatalog(String errorCode, String exceptionName, String message, int status, Map<String, String> errors) {
        this.errorCode = errorCode;
        this.exceptionName = exceptionName;
        this.message = message;
        this.status = status;
        this.errors = errors;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getExceptionName() {
        return exceptionName;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public Map<String, String> getErrors() {
        return errors;
    }
}