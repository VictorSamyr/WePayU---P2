package br.ufal.ic.p2.wepayu.exceptions;

public class AgendaNaoDisponivelException extends Exception {
    public AgendaNaoDisponivelException() {
        super("Agenda de pagamento nao esta disponivel");
    }

    public AgendaNaoDisponivelException(String mensagem) {
        super(mensagem);
    }
}
