package br.ufal.ic.p2.wepayu.exceptions;

public class EmpregadoNaoEhSindicalizadoException extends Exception {
    public EmpregadoNaoEhSindicalizadoException() {
        super("Empregado nao eh sindicalizado.");
    }

    public EmpregadoNaoEhSindicalizadoException(String mensagem) {
        super(mensagem);
    }
}
