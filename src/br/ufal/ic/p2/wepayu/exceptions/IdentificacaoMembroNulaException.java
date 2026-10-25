package br.ufal.ic.p2.wepayu.exceptions;

public class IdentificacaoMembroNulaException extends Exception {
    public IdentificacaoMembroNulaException() {
        super("Identificacao do membro nao pode ser nula.");
    }

    public IdentificacaoMembroNulaException(String mensagem) {
        super(mensagem);
    }
}
