package br.ufal.ic.p2.wepayu.exceptions;

public class SalarioNumericoException extends Exception {
    public SalarioNumericoException() {
        super("Salario deve ser numerico.");
    }

    public SalarioNumericoException(String mensagem) {
        super(mensagem);
    }
}
