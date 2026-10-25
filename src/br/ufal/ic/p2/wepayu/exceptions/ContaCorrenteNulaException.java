package br.ufal.ic.p2.wepayu.exceptions;

public class ContaCorrenteNulaException extends Exception {
    public ContaCorrenteNulaException() {
        super("Conta corrente nao pode ser nulo.");
    }

    public ContaCorrenteNulaException(String mensagem) {
        super(mensagem);
    }
}
