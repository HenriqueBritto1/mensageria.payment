package br.com.mensageria.api.infra.exception;

import br.com.mensageria.commons.exceptions.InsuficientBalanceException;
import br.com.mensageria.commons.exceptions.PagamentoNaoEncontrado;
import br.com.mensageria.commons.exceptions.ResponseError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InsuficientBalanceException.class)
    public ResponseEntity<ResponseError> handleException(InsuficientBalanceException e){
        return ResponseEntity.badRequest().body(
                new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage())
        );
    }
    @ExceptionHandler(IllegalAccessException.class)
    public ResponseEntity<ResponseError> handleException(IllegalAccessException e){
        return ResponseEntity.badRequest().body(
                new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage())
        );
    }
}
