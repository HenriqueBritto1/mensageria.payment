package br.com.mensageria.commons.exceptions;

public class ApiRuleException extends RuntimeException {
    public ApiRuleException(String message) {
        super(message);
    }
}
