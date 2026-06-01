package domain.cliente;

import domain.shared.CPF;
import java.util.Optional;

public interface ClienteRepository {

    void salvar(Cliente cliente);

    Optional<Cliente> buscarPorId(ClienteId id);

    Optional<Cliente> buscarPorCpf(CPF cpf);
}
