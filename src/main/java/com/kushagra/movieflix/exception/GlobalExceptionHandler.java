package com.kushagra.movieflix.exception;

import com.kushagra.movieflix.dto.CustomResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(exception = NotFoundException.class)
    public ResponseEntity<CustomResponse> NotFoundExceptionHandler(NotFoundException e) {
        log.info("Entered into NotFoundExceptionHandler method in GlobalExceptionHandler");

        CustomResponse customResponse= CustomResponse.builder()
                .message(e.getMessage())
                .result(null)
                .status("failure")
                .statusCode(String.valueOf(HttpStatus.NOT_FOUND.value()))
                .build();

        return new ResponseEntity<>(customResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(exception = BadRequestException.class)
    public ResponseEntity<CustomResponse> BadRequestExceptionHandler(BadRequestException e) {
        log.info("Entered into BadRequestExceptionHandler method in GlobalExceptionHandler");

        CustomResponse customResponse= CustomResponse.builder()
                .message(e.getMessage())
                .result(null)
                .status("failure")
                .statusCode(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                .build();

        return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(AccessDeniedException.class)
    public CustomResponse handleAccessDenied(AccessDeniedException ex) {
        log.info("Entered into handleAccessDenied in GlobalExceptionHandler");
        return CustomResponse.builder()
                .status("failure")
                .statusCode(String.valueOf(HttpStatus.FORBIDDEN.value()))
                .message("Access denied: You don’t have permission to perform this action")
                .result(null)
                .build();
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public CustomResponse handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                   HttpServletRequest request) {
        log.info("Entered into handleMethodNotSupported in GlobalExceptionHandler");
        return CustomResponse.builder()
                .status("failure")
                .statusCode(String.valueOf(HttpStatus.METHOD_NOT_ALLOWED.value()))
                .message("Method not allowed: " + ex.getMethod() +
                        " is not supported for " + request.getRequestURI())
                .result(null)
                .build();
    }

    @ExceptionHandler(BadCredentialsException.class)
    public CustomResponse handleBadCredentials(BadCredentialsException ex) {
        log.info("Entered into handleBadCredentials in GlobalExceptionHandler");

        return CustomResponse.builder()
                .status("failure")
                .statusCode(String.valueOf(HttpStatus.UNAUTHORIZED.value()))
                .message("Invalid username or password")
                .result(null)
                .build();
    }

    @ExceptionHandler(Exception.class)
    public CustomResponse handleGenericException(Exception ex) {
        log.info("Entered into handleGenericException in GlobalExceptionHandler");
        log.info(ex.getMessage());
        return CustomResponse.builder()
                .status("failure")
                .statusCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .message(ex.getMessage())
                .result(null)
                .build();
    }
}
