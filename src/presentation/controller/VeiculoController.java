package presentation.controller;

import application.excecao.VeiculoNaoEncontrado;
import application.veiculo.CadastrarVeiculo;
import domain.cliente.ClienteId;
import domain.veiculo.Placa;
import domain.veiculo.Veiculo;
import domain.veiculo.VeiculoId;
import domain.veiculo.VeiculoRepository;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.Objects;
import presentation.dto.CadastrarVeiculoRequest;
import presentation.dto.VeiculoResponse;

public final class VeiculoController {

    private final CadastrarVeiculo cadastrarVeiculo;
    private final VeiculoRepository veiculoRepository;

    public VeiculoController(CadastrarVeiculo cadastrarVeiculo, VeiculoRepository veiculoRepository) {
        this.cadastrarVeiculo = Objects.requireNonNull(cadastrarVeiculo, "cadastrarVeiculo");
        this.veiculoRepository = Objects.requireNonNull(veiculoRepository, "veiculoRepository");
    }

    public void registrar(Javalin app) {
        app.post("/veiculos", this::cadastrar);
        app.get("/veiculos/{id}", this::buscar);
    }

    private void cadastrar(Context ctx) {
        CadastrarVeiculoRequest req = ctx.bodyAsClass(CadastrarVeiculoRequest.class);
        ClienteId donoId = ClienteId.de(req.donoId());
        VeiculoId id = cadastrarVeiculo.executar(
            Placa.de(req.placa()), req.marca(), req.modelo(), req.ano(), donoId
        );
        ctx.status(201).header("Location", "/veiculos/" + id).json(new IdResponse(id.toString()));
    }

    private void buscar(Context ctx) {
        VeiculoId id = parseId(ctx);
        Veiculo veiculo = veiculoRepository.buscarPorId(id)
            .orElseThrow(() -> new VeiculoNaoEncontrado(id));
        ctx.json(new VeiculoResponse(
            veiculo.id().toString(),
            veiculo.placa().valor(),
            veiculo.marca(),
            veiculo.modelo(),
            veiculo.ano(),
            veiculo.dono().toString()
        ));
    }

    private static VeiculoId parseId(Context ctx) {
        try {
            return VeiculoId.de(ctx.pathParam("id"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("id de veículo inválido: " + ctx.pathParam("id"));
        }
    }

    private record IdResponse(String id) {}
}
