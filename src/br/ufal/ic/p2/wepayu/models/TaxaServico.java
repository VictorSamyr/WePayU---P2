package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representa uma taxa de servico cobrada pelo sindicato.
 */
public class TaxaServico implements Serializable {
    private final LocalDate data;
    private final BigDecimal valor;

    public TaxaServico(LocalDate data, BigDecimal valor) {
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
