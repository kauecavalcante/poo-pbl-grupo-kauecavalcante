package application.excecao;

import domain.cliente.ClienteId;
import domain.veiculo.VeiculoId;

public class VeiculoNaoPertenceAoCliente extends RuntimeException {

    public VeiculoNaoPertenceAoCliente(VeiculoId veiculoId, ClienteId clienteId) {
        super("veículo " + veiculoId + " não pertence ao cliente " + clienteId);
    }
}
