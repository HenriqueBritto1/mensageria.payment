package br.com.mensageria.commons.exceptions;

public class PagamentoNaoEncontrado extends RuntimeException {
    public PagamentoNaoEncontrado(String message) {
        super(message);
    }
}
