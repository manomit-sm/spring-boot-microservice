package com.bsolz.util.http;

import com.bsolz.api.exceptions.InvalidInputException;
import com.bsolz.api.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@RestControllerAdvice
public class GlobalControllerExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public @ResponseBody HttpErrorInfo handleNotFoundException(NotFoundException ex) {
        return createHttpErrorInfo(HttpStatus.NOT_FOUND, ex);
    }

    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ExceptionHandler(InvalidInputException.class)
    public @ResponseBody HttpErrorInfo handleInvalidInputException(
            InvalidInputException ex) {

        return createHttpErrorInfo(UNPROCESSABLE_ENTITY, ex);
    }

    private HttpErrorInfo createHttpErrorInfo(
            HttpStatus httpStatus, Exception ex) {

        final String message = ex.getMessage();

        LOG.debug("Returning HTTP status: {} message: {}", httpStatus, message);
        return new HttpErrorInfo (httpStatus, message);
    }
}
