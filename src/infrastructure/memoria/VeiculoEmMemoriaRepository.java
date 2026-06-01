package infrastructure.memoria;

import domain.cliente.ClienteId;
import domain.veiculo.Placa;
import domain.veiculo.Veiculo;
import domain.veiculo.VeiculoId;
import domain.veiculo.VeiculoRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class VeiculoEmMemoriaRepository implements VeiculoRepository {

    private final Map<VeiculoId, Veiculo> dados = new ConcurrentHashMap<>();

    @Override
    public void salvar(Veiculo veiculo) {
        Objects.requireNonNull(veiculo, "veiculo");
        dados.put(veiculo.id(), veiculo);
    }

    @Override
    public Optional<Veiculo> buscarPorId(VeiculoId id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public Optional<Veiculo> buscarPorPlaca(Placa placa) {
        if (placa == null) {
            return Optional.empty();
        }
        return dados.values().stream()
            .filter(v -> v.placa().equals(placa))
            .findFirst();
    }

    @Override
    public List<Veiculo> listarPorDono(ClienteId dono) {
        if (dono == null) {
            return List.of();
        }
        return dados.values().stream()
            .filter(v -> v.dono().equals(dono))
            .collect(Collectors.toList());
    }
}
