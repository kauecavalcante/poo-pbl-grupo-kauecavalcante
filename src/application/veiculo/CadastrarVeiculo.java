package application.veiculo;

import application.excecao.ClienteNaoEncontrado;
import application.excecao.PlacaJaCadastrada;
import domain.cliente.ClienteId;
import domain.cliente.ClienteRepository;
import domain.veiculo.Placa;
import domain.veiculo.Veiculo;
import domain.veiculo.VeiculoId;
import domain.veiculo.VeiculoRepository;
import java.util.Objects;

public final class CadastrarVeiculo {

    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;

    public CadastrarVeiculo(VeiculoRepository veiculoRepository, ClienteRepository clienteRepository) {
        this.veiculoRepository = Objects.requireNonNull(veiculoRepository, "veiculoRepository");
        this.clienteRepository = Objects.requireNonNull(clienteRepository, "clienteRepository");
    }

    public VeiculoId executar(Placa placa, String marca, String modelo, int ano, ClienteId dono) {
        if (dono != null && clienteRepository.buscarPorId(dono).isEmpty()) {
            throw new ClienteNaoEncontrado(dono);
        }
        if (placa != null) {
            veiculoRepository.buscarPorPlaca(placa).ifPresent(existente -> {
                throw new PlacaJaCadastrada(placa);
            });
        }
        Veiculo veiculo = Veiculo.novo(placa, marca, modelo, ano, dono);
        veiculoRepository.salvar(veiculo);
        return veiculo.id();
    }
}
