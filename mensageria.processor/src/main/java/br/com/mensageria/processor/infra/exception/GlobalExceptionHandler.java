package br.com.mensageria.processor.infra.exception;

import br.com.mensageria.commons.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InsuficientBalanceException.class)
    public ResponseEntity<ResponseError> handleInsuficientBalanceException(InsuficientBalanceException e) {
        return ResponseEntity.badRequest().body(new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
    }

    @ExceptionHandler(PaymentNotFound.class)
    public ResponseEntity<ResponseError> handlePagamentoNaoEncontrado(PaymentNotFound e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseError(HttpStatus.NOT_FOUND.value(), e.getMessage())
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseError> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage())
        );
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ResponseError> handleInvalidTokenException(InvalidTokenException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ResponseError(HttpStatus.UNAUTHORIZED.value(), e.getMessage())
        );
    }

    @ExceptionHandler(ApiRuleException.class)
    public ResponseEntity<ResponseError> handleApiRuleException(ApiRuleException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ResponseError(HttpStatus.CONFLICT.value(), e.getMessage())
        );
    }

    @ExceptionHandler(PermissionNotAllowed.class)
    public ResponseEntity<ResponseError> handlePermissionNotAllowed(PermissionNotAllowed e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ResponseError(HttpStatus.FORBIDDEN.value(), e.getMessage())
        );
    }

    @ExceptionHandler(RequestLimitExceeded.class)
    public ResponseEntity<ResponseError> handleRequestLimitExceeded(RequestLimitExceeded e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                new ResponseError(HttpStatus.TOO_MANY_REQUESTS.value(), e.getMessage())
        );
    }
}
