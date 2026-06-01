package application.cliente;

import application.excecao.CpfJaCadastrado;
import domain.cliente.Cliente;
import domain.cliente.ClienteId;
import domain.cliente.ClienteRepository;
import domain.shared.CPF;
import java.util.Objects;

public final class CadastrarCliente {

    private final ClienteRepository clienteRepository;

    public CadastrarCliente(ClienteRepository clienteRepository) {
        this.clienteRepository = Objects.requireNonNull(clienteRepository, "clienteRepository");
    }

    public ClienteId executar(String nome, CPF cpf, String telefone) {
        clienteRepository.buscarPorCpf(cpf).ifPresent(existente -> {
            throw new CpfJaCadastrado(cpf);
        });
        Cliente cliente = Cliente.novo(nome, cpf, telefone);
        clienteRepository.salvar(cliente);
        return cliente.id();
    }
}
