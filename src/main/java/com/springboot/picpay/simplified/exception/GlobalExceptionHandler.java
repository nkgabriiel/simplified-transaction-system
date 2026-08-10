package com.springboot.picpay.simplified.exception;

import com.springboot.picpay.simplified.dto.response.ErroResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<ErroResponseDTO> handleNotFound (UsuarioNotFoundException ex, HttpServletRequest request) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErroResponseDTO.of(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler({
            IneligibleSenderException.class,
            InvalidTransactionValueException.class,
            SelfTransferNotAllowedException.class,
            InvalidDocumentException.class
    })
    public ResponseEntity<ErroResponseDTO> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErroResponseDTO.of(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(UnauthorizedTransactionException.class)
    public ResponseEntity<ErroResponseDTO> handleUnauthorized(UnauthorizedTransactionException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErroResponseDTO.of(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(UnavailableAuthorizerException.class)
    public ResponseEntity<ErroResponseDTO> handleUnavailable (UnavailableAuthorizerException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErroResponseDTO.of(HttpStatus.SERVICE_UNAVAILABLE, ex.getLocalizedMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErroResponseDTO.of(HttpStatus.BAD_REQUEST, message, request.getRequestURI()));
    }

    @ExceptionHandler(InvalidUserRequestDataException.class)
    public ResponseEntity<ErroResponseDTO> handleConflict(InvalidUserRequestDataException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErroResponseDTO.of(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErroResponseDTO> handleConcurrentConflict(ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErroResponseDTO.of(HttpStatus.CONFLICT, "O registro foi alterado por outra operação. Tente novamente.", request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> handleGeneric (Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErroResponseDTO.of(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor, tente novamente", request.getRequestURI()));
    }
}
