package br.com.mensageria.processor.infra.exception;

import br.com.mensageria.commons.exceptions.InsuficientBalanceException;
import br.com.mensageria.commons.exceptions.PaymentNotFound;
import br.com.mensageria.commons.exceptions.ResponseError;
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


}
