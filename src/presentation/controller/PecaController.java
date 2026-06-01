package presentation.controller;

import application.excecao.PecaNaoEncontrada;
import application.peca.CadastrarPeca;
import domain.peca.Peca;
import domain.peca.PecaId;
import domain.peca.PecaRepository;
import domain.shared.Preco;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.Objects;
import presentation.dto.CadastrarPecaRequest;
import presentation.dto.PecaResponse;

public final class PecaController {

    private final CadastrarPeca cadastrarPeca;
    private final PecaRepository pecaRepository;

    public PecaController(CadastrarPeca cadastrarPeca, PecaRepository pecaRepository) {
        this.cadastrarPeca = Objects.requireNonNull(cadastrarPeca, "cadastrarPeca");
        this.pecaRepository = Objects.requireNonNull(pecaRepository, "pecaRepository");
    }

    public void registrar(Javalin app) {
        app.post("/pecas", this::cadastrar);
        app.get("/pecas/{id}", this::buscar);
    }

    private void cadastrar(Context ctx) {
        CadastrarPecaRequest req = ctx.bodyAsClass(CadastrarPecaRequest.class);
        Preco preco = Preco.deReais(req.precoReais());
        PecaId id = cadastrarPeca.executar(req.codigo(), req.descricao(), preco, req.estoqueInicial());
        ctx.status(201).header("Location", "/pecas/" + id).json(new IdResponse(id.toString()));
    }

    private void buscar(Context ctx) {
        PecaId id = parseId(ctx);
        Peca peca = pecaRepository.buscarPorId(id)
            .orElseThrow(() -> new PecaNaoEncontrada(id));
        ctx.json(new PecaResponse(
            peca.id().toString(),
            peca.codigo(),
            peca.descricao(),
            peca.preco().valor().valor().toPlainString(),
            peca.estoque()
        ));
    }

    private static PecaId parseId(Context ctx) {
        try {
            return PecaId.de(ctx.pathParam("id"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("id de peça inválido: " + ctx.pathParam("id"));
        }
    }

    private record IdResponse(String id) {}
}
