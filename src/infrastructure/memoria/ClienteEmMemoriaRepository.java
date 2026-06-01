package infrastructure.memoria;

import domain.cliente.Cliente;
import domain.cliente.ClienteId;
import domain.cliente.ClienteRepository;
import domain.shared.CPF;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ClienteEmMemoriaRepository implements ClienteRepository {

    private final Map<ClienteId, Cliente> dados = new ConcurrentHashMap<>();

    @Override
    public void salvar(Cliente cliente) {
        Objects.requireNonNull(cliente, "cliente");
        dados.put(cliente.id(), cliente);
    }

    @Override
    public Optional<Cliente> buscarPorId(ClienteId id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public Optional<Cliente> buscarPorCpf(CPF cpf) {
        if (cpf == null) {
            return Optional.empty();
        }
        return dados.values().stream()
            .filter(c -> c.cpf().equals(cpf))
            .findFirst();
    }
}
