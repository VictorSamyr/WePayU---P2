package br.ufal.ic.p2.wepayu.exceptions;

public class TaxaSindicalNulaException extends Exception {
    public TaxaSindicalNulaException() {
        super("Taxa sindical nao pode ser nula.");
    }

    public TaxaSindicalNulaException(String mensagem) {
        super(mensagem);
    }
}
