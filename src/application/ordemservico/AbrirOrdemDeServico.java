package application.ordemservico;

import application.excecao.ClienteNaoEncontrado;
import application.excecao.VeiculoNaoEncontrado;
import application.excecao.VeiculoNaoPertenceAoCliente;
import domain.cliente.ClienteId;
import domain.cliente.ClienteRepository;
import domain.ordemservico.OrdemDeServico;
import domain.ordemservico.OrdemDeServicoId;
import domain.ordemservico.OrdemDeServicoRepository;
import domain.veiculo.Veiculo;
import domain.veiculo.VeiculoId;
import domain.veiculo.VeiculoRepository;
import java.util.Objects;

public final class AbrirOrdemDeServico {

    private final OrdemDeServicoRepository osRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;

    public AbrirOrdemDeServico(OrdemDeServicoRepository osRepository,
                               ClienteRepository clienteRepository,
                               VeiculoRepository veiculoRepository) {
        this.osRepository = Objects.requireNonNull(osRepository, "osRepository");
        this.clienteRepository = Objects.requireNonNull(clienteRepository, "clienteRepository");
        this.veiculoRepository = Objects.requireNonNull(veiculoRepository, "veiculoRepository");
    }

    public OrdemDeServicoId executar(ClienteId clienteId, VeiculoId veiculoId) {
        if (clienteId != null && clienteRepository.buscarPorId(clienteId).isEmpty()) {
            throw new ClienteNaoEncontrado(clienteId);
        }
        if (veiculoId != null) {
            Veiculo veiculo = veiculoRepository.buscarPorId(veiculoId)
                .orElseThrow(() -> new VeiculoNaoEncontrado(veiculoId));
            if (clienteId != null && !veiculo.dono().equals(clienteId)) {
                throw new VeiculoNaoPertenceAoCliente(veiculoId, clienteId);
            }
        }
        OrdemDeServico os = OrdemDeServico.abrir(clienteId, veiculoId);
        osRepository.salvar(os);
        return os.id();
    }
}
