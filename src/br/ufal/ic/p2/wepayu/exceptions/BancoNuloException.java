package br.ufal.ic.p2.wepayu.exceptions;

public class BancoNuloException extends Exception {
    public BancoNuloException() {
        super("Banco nao pode ser nulo.");
    }

    public BancoNuloException(String mensagem) {
        super(mensagem);
    }
}
