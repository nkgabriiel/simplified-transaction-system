package com.springboot.picpay.simplified.exception;

import java.math.BigDecimal;

public class InvalidTransactionValueException extends RuntimeException {
    public InvalidTransactionValueException(BigDecimal value) {
        super("Insira um valor válido para a transferência: " + value);
    }
}
