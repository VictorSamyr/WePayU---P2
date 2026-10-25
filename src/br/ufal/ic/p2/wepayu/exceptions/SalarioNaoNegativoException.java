package br.ufal.ic.p2.wepayu.exceptions;

public class SalarioNaoNegativoException extends Exception {
    public SalarioNaoNegativoException() {
        super("Salario deve ser nao-negativo.");
    }

    public SalarioNaoNegativoException(String mensagem) {
        super(mensagem);
    }
}
