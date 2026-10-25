package br.ufal.ic.p2.wepayu.exceptions;

public class TaxaSindicalNumericaException extends Exception {
    public TaxaSindicalNumericaException() {
        super("Taxa sindical deve ser numerica.");
    }

    public TaxaSindicalNumericaException(String mensagem) {
        super(mensagem);
    }
}
