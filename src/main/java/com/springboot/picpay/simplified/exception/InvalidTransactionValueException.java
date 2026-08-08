package com.springboot.picpay.simplified.exception;

import java.math.BigDecimal;

public class InvalidTransactionValueException extends RuntimeException {
    public InvalidTransactionValueException(String message) {
        super(message);
    }
}
