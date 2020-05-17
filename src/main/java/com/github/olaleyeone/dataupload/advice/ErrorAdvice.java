package com.github.olaleyeone.dataupload.advice;

import com.olaleyeone.rest.ApiResponse;
import com.olaleyeone.rest.exception.ErrorResponse;
import com.olaleyeone.rest.exception.NotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class ErrorAdvice {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handle(NotFoundException e) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
    }

    @ExceptionHandler(ErrorResponse.class)
    public ResponseEntity<?> handle(ErrorResponse e) {
        return ResponseEntity.status(e.getHttpStatus()).body(e.getResponse());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex,
            WebRequest request) {
        List<String> errors = new ArrayList<String>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(violation.getPropertyPath() + " : " + violation.getMessage());
        }
        ApiResponse<List<String>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage(!errors.isEmpty() ? errors.get(0) : "Invalid request");
        apiResponse.setData(errors);
        return new ResponseEntity<>(apiResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleConstraintViolation(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        ApiResponse<List<String>> apiResponse = new ApiResponse<>();
        apiResponse.setMessage(request.getContentLength() == 0 ? "Missing request body" : "Could not parse request body");
        return new ResponseEntity<>(apiResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
}
