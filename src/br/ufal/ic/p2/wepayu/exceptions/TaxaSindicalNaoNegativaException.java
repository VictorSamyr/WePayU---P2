package br.ufal.ic.p2.wepayu.exceptions;

public class TaxaSindicalNaoNegativaException extends Exception {
    public TaxaSindicalNaoNegativaException() {
        super("Taxa sindical deve ser nao-negativa.");
    }

    public TaxaSindicalNaoNegativaException(String mensagem) {
        super(mensagem);
    }
}
