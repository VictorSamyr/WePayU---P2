package br.ufal.ic.p2.wepayu.exceptions;

public class AgendaInvalidaException extends Exception {
    public AgendaInvalidaException() {
        super("Descricao de agenda invalida");
    }

    public AgendaInvalidaException(String mensagem) {
        super(mensagem);
    }
}
