package br.ufal.ic.p2.wepayu.exceptions;

public class HorasPositivasException extends Exception {
    public HorasPositivasException() {
        super("Horas devem ser positivas.");
    }

    public HorasPositivasException(String mensagem) {
        super(mensagem);
    }
}
