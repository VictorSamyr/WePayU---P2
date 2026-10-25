package br.ufal.ic.p2.wepayu.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa um empregado comissionado (que tambem eh assalariado).
 */
public class EmpregadoComissionado extends EmpregadoAssalariado {
    private BigDecimal taxaComissao;
    private final List<Venda> vendas = new ArrayList<>();

    public EmpregadoComissionado(String id, String nome, String endereco,
                                 BigDecimal salarioMensal, BigDecimal taxaComissao,
                                 AgendaPagamento agenda) {
        super(id, nome, endereco, salarioMensal, agenda);
        this.taxaComissao = taxaComissao;
    }

    public EmpregadoComissionado(EmpregadoComissionado outro) {
        super(outro);
        this.taxaComissao = outro.taxaComissao;
        this.vendas.addAll(outro.vendas);
    }

    @Override
    public String getTipo() {
        return "comissionado";
    }

    public BigDecimal getComissao() {
        return taxaComissao;
    }

    public void setComissao(BigDecimal taxaComissao) {
        this.taxaComissao = taxaComissao;
    }

    public void adicionarVenda(Venda venda) {
        vendas.add(venda);
    }

    public List<Venda> getVendas() {
        return Collections.unmodifiableList(vendas);
    }

    public BigDecimal getVendasNoPeriodo(LocalDate inicio, LocalDate fimExclusivo) {
        BigDecimal total = BigDecimal.ZERO;
        for (Venda v : vendas) {
            if (!v.getData().isBefore(inicio) && v.getData().isBefore(fimExclusivo)) {
                total = total.add(v.getValor());
            }
        }
        return total.setScale(2, RoundingMode.DOWN);
    }

    public BigDecimal calcularSalarioFixo(LocalDate data) {
        if (getAgenda().ehMensal()) {
            return getSalario().setScale(2, RoundingMode.DOWN);
        } else {
            int semanas = getAgenda().getIntervaloSemanas();
            return getSalario().multiply(BigDecimal.valueOf(12))
                    .multiply(BigDecimal.valueOf(semanas))
                    .divide(BigDecimal.valueOf(52), 10, RoundingMode.HALF_EVEN)
                    .setScale(2, RoundingMode.DOWN);
        }
    }

    public BigDecimal calcularComissao(LocalDate data) {
        LocalDate inicio = getAgenda().getInicioPeriodo(data);
        LocalDate fimExclusivo = data.plusDays(1);
        BigDecimal totalVendas = getVendasNoPeriodo(inicio, fimExclusivo);
        return totalVendas.multiply(taxaComissao).setScale(2, RoundingMode.DOWN);
    }

    @Override
    public BigDecimal calcularSalarioBruto(LocalDate data) {
        BigDecimal fixo = calcularSalarioFixo(data);
        BigDecimal comissao = calcularComissao(data);
        return fixo.add(comissao).setScale(2, RoundingMode.DOWN);
    }
}
