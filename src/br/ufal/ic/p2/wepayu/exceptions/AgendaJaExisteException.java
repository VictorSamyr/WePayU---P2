package br.ufal.ic.p2.wepayu.exceptions;

public class AgendaJaExisteException extends Exception {
    public AgendaJaExisteException() {
        super("Agenda de pagamentos ja existe");
    }

    public AgendaJaExisteException(String mensagem) {
        super(mensagem);
    }
}
