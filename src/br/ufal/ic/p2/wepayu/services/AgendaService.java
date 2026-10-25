package br.ufal.ic.p2.wepayu.services;

import br.ufal.ic.p2.wepayu.exceptions.AgendaInvalidaException;
import br.ufal.ic.p2.wepayu.exceptions.AgendaJaExisteException;
import br.ufal.ic.p2.wepayu.exceptions.AgendaNaoDisponivelException;
import br.ufal.ic.p2.wepayu.models.AgendaPagamento;
import br.ufal.ic.p2.wepayu.models.EstadoSistema;

/**
 * Servico para criacao, validacao e recuperacao de agendas de pagamento (US9 e US10).
 */
public class AgendaService {
    public AgendaPagamento buscarAgenda(EstadoSistema estado, String descricao) throws AgendaNaoDisponivelException {
        AgendaPagamento agenda = estado.getAgendas().get(descricao);
        if (agenda == null) {
            throw new AgendaNaoDisponivelException();
        }
        return agenda;
    }

    public void criarAgendaDePagamentos(EstadoSistema estado, String descricao)
            throws AgendaInvalidaException, AgendaJaExisteException {
        AgendaPagamento nova = AgendaPagamento.parse(descricao);
        if (estado.getAgendas().containsKey(descricao)) {
            throw new AgendaJaExisteException();
        }
        estado.getAgendas().put(descricao, nova);
    }

    public AgendaPagamento getAgendaDefault(EstadoSistema estado, String tipo) {
        return switch (tipo) {
            case "horista" -> estado.getAgendas().get("semanal 5");
            case "comissionado" -> estado.getAgendas().get("semanal 2 5");
            default -> estado.getAgendas().get("mensal $");
        };
    }
}
