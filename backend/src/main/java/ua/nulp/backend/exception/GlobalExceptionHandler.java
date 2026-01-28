package ua.nulp.backend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // =================================================================================
    // 400 - BAD REQUEST
    // =================================================================================

    // 1. Помилки валідації @Valid (наприклад, пустий email)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Failed", errors);
    }

    // 2. Логічні помилки (наприклад, "Паролі не співпадають")
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // =================================================================================
    // 401 - UNAUTHORIZED
    // =================================================================================
    // Невірний логін/пароль або відсутній токен
    @ExceptionHandler({BadCredentialsException.class, InsufficientAuthenticationException.class})
    public ResponseEntity<ErrorResponse> handleUnauthorized(Exception ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password / Token missing");
    }

    // =================================================================================
    // 403 - FORBIDDEN
    // =================================================================================
    // Юзер залогінений, але лізе куди не можна (USER -> ADMIN панель)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Access denied. Insufficient permissions.");
    }

    // =================================================================================
    // 404 - NOT FOUND
    // =================================================================================
    // Не знайдено в базі
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // =================================================================================
    // 409 - CONFLICT
    // =================================================================================
    // Дублікат даних (Email зайнятий)
    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ResourceConflictException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // =================================================================================
    // 415 - UNSUPPORTED MEDIA TYPE
    // =================================================================================
    // Клієнт шле XML замість JSON, або картинку неправильного формату
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content type '" + ex.getContentType() + "' not supported.");
    }

    // =================================================================================
    // 500 - INTERNAL SERVER ERROR
    // =================================================================================
    // Пастка для всього іншого (NullPointer, DB connection failed)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        log.error("Critical unexpected error: ", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
    }

    // === Helper Method ===
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
        return buildResponse(status, message, null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, Map<String, String> validationErrors) {
        ErrorResponse response = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .timestamp(Instant.now())
                .validationErrors(validationErrors)
                .build();
        return new ResponseEntity<>(response, status);
    }
}