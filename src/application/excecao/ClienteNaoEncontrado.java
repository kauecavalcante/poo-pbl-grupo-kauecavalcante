package application.excecao;

import domain.cliente.ClienteId;

public class ClienteNaoEncontrado extends RuntimeException {

    public ClienteNaoEncontrado(ClienteId id) {
        super("cliente não encontrado: " + id);
    }
}
