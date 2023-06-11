package ru.practicum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final ValidationException exception) {
        return createApiError(HttpStatus.BAD_REQUEST, "Incorrectly made request.", exception);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException exception) {
        return createApiError(HttpStatus.NOT_FOUND, "The required object was not found.", exception);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final ConflictException exception) {
        return createApiError(HttpStatus.CONFLICT, "Integrity constraint has been violated.", exception);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(final Throwable exception) {
        return createApiError(HttpStatus.INTERNAL_SERVER_ERROR, "", exception);
    }

    private ApiError createApiError(HttpStatus status, String reason, Throwable exception) {
        return ApiError.builder()
                .status(status)
                .reason(reason)
                .message(exception.getLocalizedMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

}