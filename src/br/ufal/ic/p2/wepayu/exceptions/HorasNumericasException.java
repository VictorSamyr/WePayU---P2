package br.ufal.ic.p2.wepayu.exceptions;

public class HorasNumericasException extends Exception {
    public HorasNumericasException() {
        super("Horas devem ser numericas.");
    }

    public HorasNumericasException(String mensagem) {
        super(mensagem);
    }
}
