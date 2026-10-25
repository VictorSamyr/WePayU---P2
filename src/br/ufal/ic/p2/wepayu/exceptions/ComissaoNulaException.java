package br.ufal.ic.p2.wepayu.exceptions;

public class ComissaoNulaException extends Exception {
    public ComissaoNulaException() {
        super("Comissao nao pode ser nula.");
    }

    public ComissaoNulaException(String mensagem) {
        super(mensagem);
    }
}
