package presentation.controller;

import application.cliente.CadastrarCliente;
import application.excecao.ClienteNaoEncontrado;
import domain.cliente.Cliente;
import domain.cliente.ClienteId;
import domain.cliente.ClienteRepository;
import domain.shared.CPF;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.Objects;
import java.util.UUID;
import presentation.dto.CadastrarClienteRequest;
import presentation.dto.ClienteResponse;

public final class ClienteController {

    private final CadastrarCliente cadastrarCliente;
    private final ClienteRepository clienteRepository;

    public ClienteController(CadastrarCliente cadastrarCliente, ClienteRepository clienteRepository) {
        this.cadastrarCliente = Objects.requireNonNull(cadastrarCliente, "cadastrarCliente");
        this.clienteRepository = Objects.requireNonNull(clienteRepository, "clienteRepository");
    }

    public void registrar(Javalin app) {
        app.post("/clientes", this::cadastrar);
        app.get("/clientes/{id}", this::buscar);
    }

    private void cadastrar(Context ctx) {
        CadastrarClienteRequest req = ctx.bodyAsClass(CadastrarClienteRequest.class);
        ClienteId id = cadastrarCliente.executar(req.nome(), CPF.de(req.cpf()), req.telefone());
        ctx.status(201).header("Location", "/clientes/" + id).json(new IdResponse(id.toString()));
    }

    private void buscar(Context ctx) {
        ClienteId id = parseId(ctx);
        Cliente cliente = clienteRepository.buscarPorId(id)
            .orElseThrow(() -> new ClienteNaoEncontrado(id));
        ctx.json(new ClienteResponse(
            cliente.id().toString(),
            cliente.nome(),
            cliente.cpf().toString(),
            cliente.telefone()
        ));
    }

    private static ClienteId parseId(Context ctx) {
        try {
            return ClienteId.de(UUID.fromString(ctx.pathParam("id")));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("id de cliente inválido: " + ctx.pathParam("id"));
        }
    }

    // Resposta mínima de criação — apenas o id, sem redirecionar
    private record IdResponse(String id) {}
}
