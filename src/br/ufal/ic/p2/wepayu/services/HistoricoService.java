package br.ufal.ic.p2.wepayu.services;

import br.ufal.ic.p2.wepayu.exceptions.NaoHaComandoDesfazerException;
import br.ufal.ic.p2.wepayu.exceptions.NaoHaComandoRefazerException;
import br.ufal.ic.p2.wepayu.exceptions.SistemaEncerradoException;
import br.ufal.ic.p2.wepayu.models.EstadoSistema;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Gerencia o historico de estados para Undo/Redo e o ciclo de vida do sistema.
 */
public class HistoricoService {
    private final Deque<EstadoSistema> desfazer = new ArrayDeque<>();
    private final Deque<EstadoSistema> refazer = new ArrayDeque<>();
    private boolean encerrado;

    public void verificarAtivo() throws SistemaEncerradoException {
        if (encerrado) {
            throw new SistemaEncerradoException();
        }
    }

    public void encerrar() throws SistemaEncerradoException {
        verificarAtivo();
        encerrado = true;
    }

    public void registrarAlteracao(EstadoSistema estadoAtual) {
        desfazer.push(estadoAtual.copiar());
        refazer.clear();
    }

    public EstadoSistema desfazer(EstadoSistema estadoAtual) throws Exception {
        verificarAtivo();
        if (desfazer.isEmpty()) {
            throw new NaoHaComandoDesfazerException();
        }
        refazer.push(estadoAtual.copiar());
        return desfazer.pop();
    }

    public EstadoSistema refazer(EstadoSistema estadoAtual) throws Exception {
        verificarAtivo();
        if (refazer.isEmpty()) {
            throw new NaoHaComandoRefazerException();
        }
        desfazer.push(estadoAtual.copiar());
        return refazer.pop();
    }
}
