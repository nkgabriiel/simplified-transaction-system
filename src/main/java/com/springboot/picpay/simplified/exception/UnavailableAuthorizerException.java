package com.springboot.picpay.simplified.exception;

public class UnavailableAuthorizerException extends RuntimeException {
    public UnavailableAuthorizerException(String message, Throwable cause) {
        super(message, cause);
    }
}
