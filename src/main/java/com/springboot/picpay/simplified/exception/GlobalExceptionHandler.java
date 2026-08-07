package com.springboot.picpay.simplified.exception;

import com.springboot.picpay.simplified.dto.response.ErroResponseDTO;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<ErroResponseDTO> handleNotFound (UsuarioNotFoundException ex, HttpServletRequest request) {
        ErroResponseDTO body = new ErroResponseDTO(
                Instant.now().toString(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler({
            IneligibleSenderException.class,
            InvalidTransactionValueException.class,
            SelfTransferNotAllowedException.class
    })
    public ResponseEntity<ErroResponseDTO> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        ErroResponseDTO body = new ErroResponseDTO(
                Instant.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(UnauthorizedTransactionException.class)
    public ResponseEntity<ErroResponseDTO> handleUnauthorized(UnauthorizedTransactionException ex, HttpServletRequest request) {
        ErroResponseDTO body = new ErroResponseDTO(
                Instant.now().toString(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(UnavailableAuthorizerException.class)
    public ResponseEntity<ErroResponseDTO> handleUnavailable (UnavailableAuthorizerException ex, HttpServletRequest request) {
        ErroResponseDTO body = new ErroResponseDTO(
                Instant.now().toString(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    @ExceptionHandler(InvalidUserRequestDataException.class)
    public ResponseEntity<ErroResponseDTO> handleConflict(InvalidUserRequestDataException ex, HttpServletRequest request) {
        ErroResponseDTO body = new ErroResponseDTO(
                Instant.now().toString(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> handleGeneric (Exception ex, HttpServletRequest request) {
        ErroResponseDTO body = new ErroResponseDTO(
                Instant.now().toString(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
