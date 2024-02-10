package com.bsolz.util.http;

import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

public record HttpErrorInfo(ZonedDateTime timestamp,HttpStatus httpStatus, String message) {

    public HttpErrorInfo(HttpStatus httpStatus, String message) {
        this(ZonedDateTime.now(), httpStatus, message);
    }
}
