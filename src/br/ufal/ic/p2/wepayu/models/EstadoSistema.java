package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** Guarda os dados do sistema que precisam ser salvos. */
public class EstadoSistema implements Serializable {
    private final Map<String, Empregado> empregados = new LinkedHashMap<>();
    private final Map<LocalDate, String> folhasGeradas = new LinkedHashMap<>();
    private int proximoId = 1;

    public EstadoSistema() {
    }

    public EstadoSistema(EstadoSistema outro) {
        for (Map.Entry<String, Empregado> item : outro.empregados.entrySet()) {
            empregados.put(item.getKey(), new Empregado(item.getValue()));
        }
        folhasGeradas.putAll(outro.folhasGeradas);
        proximoId = outro.proximoId;
    }

    public Map<String, Empregado> getEmpregados() { return empregados; }
    public Map<LocalDate, String> getFolhasGeradas() { return folhasGeradas; }

    public String novoId() {
        return String.valueOf(proximoId++);
    }

    public EstadoSistema copiar() { return new EstadoSistema(this); }
}
