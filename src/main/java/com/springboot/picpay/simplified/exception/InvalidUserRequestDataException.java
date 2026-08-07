package com.springboot.picpay.simplified.exception;

public class InvalidUserRequestDataException extends RuntimeException {
    public InvalidUserRequestDataException(String message) {
        super(message);
    }
}
