package application.excecao;

import domain.veiculo.VeiculoId;

public class VeiculoNaoEncontrado extends RuntimeException {

    public VeiculoNaoEncontrado(VeiculoId id) {
        super("veículo não encontrado: " + id);
    }
}
