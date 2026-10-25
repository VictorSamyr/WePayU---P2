package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.exceptions.AgendaInvalidaException;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Snapshot do estado do sistema para Undo/Redo e Persistencia.
 */
public class EstadoSistema implements Serializable {
    private final Map<String, Empregado> empregados = new LinkedHashMap<>();
    private final Map<String, AgendaPagamento> agendas = new LinkedHashMap<>();
    private final Map<LocalDate, String> folhasGeradas = new LinkedHashMap<>();
    private int proximoId = 1;

    public EstadoSistema() {
        try {
            agendas.put("semanal 5", AgendaPagamento.parse("semanal 5"));
            agendas.put("mensal $", AgendaPagamento.parse("mensal $"));
            agendas.put("semanal 2 5", AgendaPagamento.parse("semanal 2 5"));
        } catch (AgendaInvalidaException e) {
            // As agendas default sao garantidamente validas.
        }
    }

    public EstadoSistema(EstadoSistema outro) {
        for (Map.Entry<String, Empregado> entry : outro.empregados.entrySet()) {
            Empregado e = entry.getValue();
            if (e instanceof EmpregadoHorista horista) {
                empregados.put(entry.getKey(), new EmpregadoHorista(horista));
            } else if (e instanceof EmpregadoComissionado comissionado) {
                empregados.put(entry.getKey(), new EmpregadoComissionado(comissionado));
            } else if (e instanceof EmpregadoAssalariado assalariado) {
                empregados.put(entry.getKey(), new EmpregadoAssalariado(assalariado));
            }
        }
        agendas.putAll(outro.agendas);
        folhasGeradas.putAll(outro.folhasGeradas);
        proximoId = outro.proximoId;
    }

    public Map<String, Empregado> getEmpregados() {
        return empregados;
    }

    public Map<String, AgendaPagamento> getAgendas() {
        return agendas;
    }

    public Map<LocalDate, String> getFolhasGeradas() {
        return folhasGeradas;
    }

    public String novoId() {
        return String.valueOf(proximoId++);
    }

    public EstadoSistema copiar() {
        return new EstadoSistema(this);
    }
}
