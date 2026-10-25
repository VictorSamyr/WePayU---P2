package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.exceptions.AgendaInvalidaException;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Representa uma agenda de pagamento (padrao ou customizada).
 */
public class AgendaPagamento implements Serializable {
    private static final LocalDate DATA_BASE_SEMANAL = LocalDate.of(2005, 1, 1);

    private final String descricao;
    private final boolean ehMensal;
    private final boolean ultimoDiaUtil;
    private final int diaDoMes;
    private final int intervaloSemanas;
    private final int diaDaSemana;

    private AgendaPagamento(String descricao, boolean ehMensal, boolean ultimoDiaUtil,
                            int diaDoMes, int intervaloSemanas, int diaDaSemana) {
        this.descricao = descricao;
        this.ehMensal = ehMensal;
        this.ultimoDiaUtil = ultimoDiaUtil;
        this.diaDoMes = diaDoMes;
        this.intervaloSemanas = intervaloSemanas;
        this.diaDaSemana = diaDaSemana;
    }

    public static AgendaPagamento parse(String descricao) throws AgendaInvalidaException {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new AgendaInvalidaException();
        }
        String[] partes = descricao.trim().split("\\s+");
        if (partes.length < 2 || partes.length > 3) {
            throw new AgendaInvalidaException();
        }

        if ("mensal".equals(partes[0])) {
            if (partes.length != 2) {
                throw new AgendaInvalidaException();
            }
            if ("$".equals(partes[1])) {
                return new AgendaPagamento(descricao, true, true, 0, 0, 0);
            }
            try {
                int dia = Integer.parseInt(partes[1]);
                if (dia < 1 || dia > 28) {
                    throw new AgendaInvalidaException();
                }
                return new AgendaPagamento(descricao, true, false, dia, 0, 0);
            } catch (NumberFormatException e) {
                throw new AgendaInvalidaException();
            }
        } else if ("semanal".equals(partes[0])) {
            if (partes.length == 2) {
                try {
                    int diaSemana = Integer.parseInt(partes[1]);
                    if (diaSemana < 1 || diaSemana > 7) {
                        throw new AgendaInvalidaException();
                    }
                    return new AgendaPagamento(descricao, false, false, 0, 1, diaSemana);
                } catch (NumberFormatException e) {
                    throw new AgendaInvalidaException();
                }
            } else {
                try {
                    int semanas = Integer.parseInt(partes[1]);
                    int diaSemana = Integer.parseInt(partes[2]);
                    if (semanas < 1 || semanas > 52 || diaSemana < 1 || diaSemana > 7) {
                        throw new AgendaInvalidaException();
                    }
                    return new AgendaPagamento(descricao, false, false, 0, semanas, diaSemana);
                } catch (NumberFormatException e) {
                    throw new AgendaInvalidaException();
                }
            }
        }

        throw new AgendaInvalidaException();
    }

    public boolean deveReceber(LocalDate data) {
        if (ehMensal) {
            if (ultimoDiaUtil) {
                return data.equals(ultimoDiaUtilDoMes(data));
            }
            return data.getDayOfMonth() == diaDoMes;
        } else {
            if (data.getDayOfWeek().getValue() != diaDaSemana) {
                return false;
            }
            long semanas = ChronoUnit.DAYS.between(DATA_BASE_SEMANAL, data) / 7 + 1;
            return semanas % intervaloSemanas == 0;
        }
    }

    public LocalDate getInicioPeriodo(LocalDate data) {
        if (ehMensal) {
            if (ultimoDiaUtil) {
                return data.withDayOfMonth(1);
            }
            return data.minusMonths(1).plusDays(1);
        } else {
            return data.minusDays(intervaloSemanas * 7L - 1);
        }
    }

    public static LocalDate ultimoDiaUtilDoMes(LocalDate data) {
        LocalDate ultimo = data.withDayOfMonth(data.lengthOfMonth());
        if (ultimo.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return ultimo.minusDays(1);
        }
        if (ultimo.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return ultimo.minusDays(2);
        }
        return ultimo;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean ehMensal() {
        return ehMensal;
    }

    public int getIntervaloSemanas() {
        return intervaloSemanas;
    }

    public int getDiaDaSemana() {
        return diaDaSemana;
    }

    public int getDiaDoMes() {
        return diaDoMes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgendaPagamento that = (AgendaPagamento) o;
        return Objects.equals(descricao, that.descricao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(descricao);
    }
}
