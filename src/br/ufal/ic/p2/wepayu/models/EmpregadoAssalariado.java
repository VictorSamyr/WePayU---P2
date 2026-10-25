package br.ufal.ic.p2.wepayu.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Representa um empregado assalariado.
 */
public class EmpregadoAssalariado extends Empregado {
    private BigDecimal salarioMensal;

    public EmpregadoAssalariado(String id, String nome, String endereco, BigDecimal salarioMensal, AgendaPagamento agenda) {
        super(id, nome, endereco, agenda);
        this.salarioMensal = salarioMensal;
    }

    public EmpregadoAssalariado(EmpregadoAssalariado outro) {
        super(outro);
        this.salarioMensal = outro.salarioMensal;
    }

    @Override
    public String getTipo() {
        return "assalariado";
    }

    @Override
    public BigDecimal getSalario() {
        return salarioMensal;
    }

    @Override
    public void setSalario(BigDecimal salario) {
        this.salarioMensal = salario;
    }

    @Override
    public BigDecimal calcularSalarioBruto(LocalDate data) {
        if (getAgenda().ehMensal()) {
            return salarioMensal.setScale(2, RoundingMode.DOWN);
        } else {
            int semanas = getAgenda().getIntervaloSemanas();
            return salarioMensal.multiply(BigDecimal.valueOf(12))
                    .multiply(BigDecimal.valueOf(semanas))
                    .divide(BigDecimal.valueOf(52), 2, RoundingMode.DOWN);
        }
    }
}
