package br.ufal.ic.p2.wepayu.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa um empregado horista.
 */
public class EmpregadoHorista extends Empregado {
    private BigDecimal salarioHora;
    private final List<CartaoDePonto> cartoes = new ArrayList<>();

    public EmpregadoHorista(String id, String nome, String endereco, BigDecimal salarioHora, AgendaPagamento agenda) {
        super(id, nome, endereco, agenda);
        this.salarioHora = salarioHora;
    }

    public EmpregadoHorista(EmpregadoHorista outro) {
        super(outro);
        this.salarioHora = outro.salarioHora;
        this.cartoes.addAll(outro.cartoes);
    }

    @Override
    public String getTipo() {
        return "horista";
    }

    @Override
    public BigDecimal getSalario() {
        return salarioHora;
    }

    @Override
    public void setSalario(BigDecimal salario) {
        this.salarioHora = salario;
    }

    public void adicionarCartao(CartaoDePonto cartao) {
        cartoes.add(cartao);
    }

    public List<CartaoDePonto> getCartoes() {
        return Collections.unmodifiableList(cartoes);
    }

    public BigDecimal getHorasNormais(LocalDate inicio, LocalDate fimExclusivo) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartaoDePonto c : cartoes) {
            if (!c.getData().isBefore(inicio) && c.getData().isBefore(fimExclusivo)) {
                total = total.add(c.getHorasNormais());
            }
        }
        return total;
    }

    public BigDecimal getHorasExtras(LocalDate inicio, LocalDate fimExclusivo) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartaoDePonto c : cartoes) {
            if (!c.getData().isBefore(inicio) && c.getData().isBefore(fimExclusivo)) {
                total = total.add(c.getHorasExtras());
            }
        }
        return total;
    }

    @Override
    public BigDecimal calcularSalarioBruto(LocalDate data) {
        LocalDate inicio = getAgenda().getInicioPeriodo(data);
        LocalDate fimExclusivo = data.plusDays(1);
        BigDecimal normais = getHorasNormais(inicio, fimExclusivo);
        BigDecimal extras = getHorasExtras(inicio, fimExclusivo);
        BigDecimal valorNormais = normais.multiply(salarioHora);
        BigDecimal valorExtras = extras.multiply(salarioHora).multiply(new BigDecimal("1.5"));
        return valorNormais.add(valorExtras).setScale(2, RoundingMode.DOWN);
    }
}
