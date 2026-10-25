package br.ufal.ic.p2.wepayu.exceptions;

public class IdentificacaoEmpregadoNulaException extends Exception {
    public IdentificacaoEmpregadoNulaException() {
        super("Identificacao do empregado nao pode ser nula.");
    }

    public IdentificacaoEmpregadoNulaException(String mensagem) {
        super(mensagem);
    }
}
