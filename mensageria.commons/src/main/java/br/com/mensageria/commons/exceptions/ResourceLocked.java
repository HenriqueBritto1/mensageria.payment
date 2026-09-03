package br.com.mensageria.commons.exceptions;

public class ResourceLocked extends RuntimeException {
    public ResourceLocked(String message) {
        super(message);
    }
}
