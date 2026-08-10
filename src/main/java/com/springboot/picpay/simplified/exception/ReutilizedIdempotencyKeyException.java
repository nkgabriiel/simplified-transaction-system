package com.springboot.picpay.simplified.exception;

public class ReutilizedIdempotencyKeyException extends RuntimeException {
    public ReutilizedIdempotencyKeyException(String message) {
        super(message);
    }
}
