package application.excecao;

import domain.ordemservico.OrdemDeServicoId;

public class OrdemDeServicoNaoEncontrada extends RuntimeException {

    public OrdemDeServicoNaoEncontrada(OrdemDeServicoId id) {
        super("ordem de serviço não encontrada: " + id);
    }
}
