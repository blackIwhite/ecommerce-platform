package com.ecommerce.common.web.handler;

import com.ecommerce.common.core.exception.BusinessException;
import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.core.result.ResultCode;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler for all REST controllers.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MeterRegistry meterRegistry;

    /**
     * Handle business exceptions.
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("Business exception at {}: code={}, message={}", request.getRequestURI(), e.getCode(), e.getMessage());
        Counter.builder("error.business")
                .tag("code", String.valueOf(e.getCode()))
                .description("Business exceptions by error code")
                .register(meterRegistry)
                .increment();
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * Handle @RequestBody validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("Parameter validation failed: {}", errorMessage);
        Counter.builder("error.validation")
                .tag("type", "MethodArgumentNotValid")
                .description("Validation errors")
                .register(meterRegistry)
                .increment();
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), errorMessage);
    }

    /**
     * Handle @RequestParam / @PathVariable validation errors.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("Constraint violation: {}", errorMessage);
        Counter.builder("error.validation")
                .tag("type", "ConstraintViolation")
                .description("Validation errors")
                .register(meterRegistry)
                .increment();
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), errorMessage);
    }

    /**
     * Catch-all for unhandled exceptions.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("Unhandled exception at {}: {}", request.getRequestURI(), e.getMessage(), e);
        Counter.builder("error.unhandled")
                .tag("exception", e.getClass().getSimpleName())
                .description("Unhandled exceptions")
                .register(meterRegistry)
                .increment();
        return Result.fail(ResultCode.INTERNAL_ERROR);
    }
}
