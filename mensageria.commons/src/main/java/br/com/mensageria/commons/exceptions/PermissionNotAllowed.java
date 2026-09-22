package br.com.mensageria.commons.exceptions;

public class PermissionNotAllowed extends RuntimeException {
    public PermissionNotAllowed(String message) {
        super(message);
    }
}
