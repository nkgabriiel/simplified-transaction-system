package com.springboot.picpay.simplified.exception;

public class IneligibleSenderException extends RuntimeException {
    public IneligibleSenderException(String message) {
        super(message);
    }
}
