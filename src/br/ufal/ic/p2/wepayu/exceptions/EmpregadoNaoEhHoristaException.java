package br.ufal.ic.p2.wepayu.exceptions;

public class EmpregadoNaoEhHoristaException extends Exception {
    public EmpregadoNaoEhHoristaException() {
        super("Empregado nao eh horista.");
    }

    public EmpregadoNaoEhHoristaException(String mensagem) {
        super(mensagem);
    }
}
