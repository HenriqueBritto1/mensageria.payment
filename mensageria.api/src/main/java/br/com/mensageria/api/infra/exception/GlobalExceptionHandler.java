package br.com.mensageria.api.infra.exception;

import br.com.mensageria.commons.exceptions.InsuficientBalanceException;
import br.com.mensageria.commons.exceptions.PaymentError;
import br.com.mensageria.commons.exceptions.PaymentNotFound;
import br.com.mensageria.commons.exceptions.ResponseError;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InsuficientBalanceException.class)
    @ApiResponse(
            responseCode = "400",
            description = "Saldo insuficiente",
            content = @Content(schema = @Schema(implementation = ResponseError.class))
    )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ResponseError> handleException(InsuficientBalanceException e){
        return ResponseEntity.badRequest().body(
                new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage())
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ApiResponse(
            responseCode = "400",
            description = "Argumentos inválidos",
            content = @Content(schema = @Schema(implementation = ResponseError.class))
    )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ResponseError> handleException(IllegalArgumentException e){
        return ResponseEntity.badRequest().body(
                new ResponseError(HttpStatus.BAD_REQUEST.value(), e.getMessage())
        );
    }

    @ExceptionHandler(PaymentError.class)
    @ApiResponse(
            responseCode = "500",
            description = "Erro interno com pagamento",
            content = @Content(schema = @Schema(implementation = ResponseError.class))
    )
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ResponseError> handleException(PaymentError e){
        return ResponseEntity.internalServerError().body(
                new ResponseError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage())
        );
    }

    @ExceptionHandler(PaymentNotFound.class)
    @ApiResponse(
            responseCode = "404",
            description = "Pagamento não encontrado",
            content = @Content(schema = @Schema(implementation = ResponseError.class))
    )
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ResponseError> handleException(PaymentNotFound e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseError(HttpStatus.NOT_FOUND.value(), e.getMessage())
        );
    }
}
