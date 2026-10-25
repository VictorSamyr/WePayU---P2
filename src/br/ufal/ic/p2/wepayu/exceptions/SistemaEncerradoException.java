package br.ufal.ic.p2.wepayu.exceptions;

public class SistemaEncerradoException extends Exception {
    public SistemaEncerradoException() {
        super("Nao pode dar comandos depois de encerrarSistema.");
    }

    public SistemaEncerradoException(String mensagem) {
        super(mensagem);
    }
}
