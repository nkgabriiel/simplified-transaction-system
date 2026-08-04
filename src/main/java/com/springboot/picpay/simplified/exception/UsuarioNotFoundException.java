package com.springboot.picpay.simplified.exception;

public class UsuarioNotFoundException extends RuntimeException {
    public UsuarioNotFoundException(Long id) {
        super("Usuário não foi encontrado com o id: " + id);
    }
}
