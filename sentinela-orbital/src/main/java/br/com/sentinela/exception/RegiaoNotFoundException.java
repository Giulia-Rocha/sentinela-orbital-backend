package br.com.sentinela.exception;

public class RegiaoNotFoundException extends RuntimeException {

    public RegiaoNotFoundException(Long id) {
        super("Região não encontrada: " + id);
    }

    public RegiaoNotFoundException(String mensagem) {
        super(mensagem);
    }
}
