package br.ufal.ic.p2.wepayu.exceptions;

public class NomeNuloException extends Exception {
    public NomeNuloException() {
        super("Nome nao pode ser nulo.");
    }

    public NomeNuloException(String mensagem) {
        super(mensagem);
    }
}
