package domain.veiculo;

import domain.cliente.ClienteId;
import java.util.List;
import java.util.Optional;

public interface VeiculoRepository {

    void salvar(Veiculo veiculo);

    Optional<Veiculo> buscarPorId(VeiculoId id);

    Optional<Veiculo> buscarPorPlaca(Placa placa);

    List<Veiculo> listarPorDono(ClienteId dono);
}
