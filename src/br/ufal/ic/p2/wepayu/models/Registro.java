package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Guarda a data e o valor de um lancamento. */
public class Registro implements Serializable {
    private final LocalDate data;
    private final BigDecimal valor;

    public Registro(LocalDate data, BigDecimal valor) {
        this.data = data;
        this.valor = valor;
    }

    public LocalDate getData() { return data; }
    public BigDecimal getValor() { return valor; }
}
