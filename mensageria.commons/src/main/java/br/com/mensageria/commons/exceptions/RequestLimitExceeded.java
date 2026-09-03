package br.com.mensageria.commons.exceptions;

public class RequestLimitExceeded extends RuntimeException {
    public RequestLimitExceeded(String message) {
        super(message);
    }
}
