package br.ufal.ic.p2.wepayu.exceptions;

public class AgenciaNulaException extends Exception {
    public AgenciaNulaException() {
        super("Agencia nao pode ser nulo.");
    }

    public AgenciaNulaException(String mensagem) {
        super(mensagem);
    }
}
