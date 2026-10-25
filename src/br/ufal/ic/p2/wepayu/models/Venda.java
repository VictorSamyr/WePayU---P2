package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representa um comprovante de venda submetido por um empregado comissionado.
 */
public class Venda implements Serializable {
    private final LocalDate data;
    private final BigDecimal valor;

    public Venda(LocalDate data, BigDecimal valor) {
        this.data = data;
        this.valor = valor;
    }

    public LocalDate getData() {
        return data;
    }

    public BigDecimal getValor() {
        return valor;
    }
}
