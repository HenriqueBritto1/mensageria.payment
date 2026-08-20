package br.com.mensageria.commons.exceptions;

public class PaymentError extends RuntimeException {
    public PaymentError(String message) {
        super(message);
    }
}
