package br.ufal.ic.p2.wepayu.persistence;

import br.ufal.ic.p2.wepayu.exceptions.PersistenciaException;
import br.ufal.ic.p2.wepayu.models.EstadoSistema;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Servico responsavel pelo carregamento e gravacao do estado do sistema em arquivo.
 */
public class PersistenciaService {
    public EstadoSistema carregar(Path arquivo) {
        if (!Files.exists(arquivo)) {
            return new EstadoSistema();
        }
        try (ObjectInputStream entrada = new ObjectInputStream(Files.newInputStream(arquivo))) {
            return (EstadoSistema) entrada.readObject();
        } catch (Exception e) {
            return new EstadoSistema();
        }
    }

    public void salvar(Path arquivo, EstadoSistema estado) throws PersistenciaException {
        try (ObjectOutputStream saida = new ObjectOutputStream(Files.newOutputStream(arquivo))) {
            saida.writeObject(estado);
        } catch (IOException e) {
            throw new PersistenciaException();
        }
    }
}
