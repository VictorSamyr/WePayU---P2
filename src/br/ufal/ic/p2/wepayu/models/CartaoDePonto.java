package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representa um cartao de ponto submetido por um empregado horista.
 */
public class CartaoDePonto implements Serializable {
    private final LocalDate data;
    private final BigDecimal horas;

    public CartaoDePonto(LocalDate data, BigDecimal horas) {
        this.data = data;
        this.horas = horas;
    }

    public LocalDate getData() {
        return data;
    }

    public BigDecimal getHoras() {
        return horas;
    }

    public BigDecimal getHorasNormais() {
        return horas.min(new BigDecimal("8"));
    }

    public BigDecimal getHorasExtras() {
        return horas.subtract(new BigDecimal("8")).max(BigDecimal.ZERO);
    }
}
