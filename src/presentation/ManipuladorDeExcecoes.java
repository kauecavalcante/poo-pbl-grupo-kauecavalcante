package presentation;

import application.excecao.ClienteNaoEncontrado;
import application.excecao.CodigoDePecaJaCadastrado;
import application.excecao.CpfJaCadastrado;
import application.excecao.OrcamentoNaoEncontrado;
import application.excecao.OrdemDeServicoNaoEncontrada;
import application.excecao.PecaNaoEncontrada;
import application.excecao.PlacaJaCadastrada;
import application.excecao.VeiculoNaoEncontrado;
import application.excecao.VeiculoNaoPertenceAoCliente;
import infrastructure.sqlite.FalhaDePersistencia;
import io.javalin.Javalin;
import io.javalin.http.Context;
import presentation.dto.ErroResponse;

final class ManipuladorDeExcecoes {

    private ManipuladorDeExcecoes() {}

    static void registrar(Javalin app) {
        app.exception(IllegalArgumentException.class, (e, ctx) ->
            responder(ctx, 400, "REQUISICAO_INVALIDA", e.getMessage()));

        app.exception(ClienteNaoEncontrado.class, (e, ctx) ->
            responder(ctx, 404, "NAO_ENCONTRADO", e.getMessage()));
        app.exception(VeiculoNaoEncontrado.class, (e, ctx) ->
            responder(ctx, 404, "NAO_ENCONTRADO", e.getMessage()));
        app.exception(PecaNaoEncontrada.class, (e, ctx) ->
            responder(ctx, 404, "NAO_ENCONTRADO", e.getMessage()));
        app.exception(OrcamentoNaoEncontrado.class, (e, ctx) ->
            responder(ctx, 404, "NAO_ENCONTRADO", e.getMessage()));
        app.exception(OrdemDeServicoNaoEncontrada.class, (e, ctx) ->
            responder(ctx, 404, "NAO_ENCONTRADO", e.getMessage()));

        app.exception(CpfJaCadastrado.class, (e, ctx) ->
            responder(ctx, 409, "CONFLITO", e.getMessage()));
        app.exception(PlacaJaCadastrada.class, (e, ctx) ->
            responder(ctx, 409, "CONFLITO", e.getMessage()));
        app.exception(CodigoDePecaJaCadastrado.class, (e, ctx) ->
            responder(ctx, 409, "CONFLITO", e.getMessage()));
        app.exception(VeiculoNaoPertenceAoCliente.class, (e, ctx) ->
            responder(ctx, 409, "CONFLITO", e.getMessage()));
        app.exception(IllegalStateException.class, (e, ctx) ->
            responder(ctx, 409, "ESTADO_INVALIDO", e.getMessage()));

        app.exception(FalhaDePersistencia.class, (e, ctx) ->
            responder(ctx, 500, "ERRO_INTERNO", "Falha de persistência"));

        app.exception(Exception.class, (e, ctx) ->
            responder(ctx, 500, "ERRO_INTERNO", "Erro inesperado"));
    }

    private static void responder(Context ctx, int status, String erro, String mensagem) {
        ctx.status(status).json(new ErroResponse(erro, mensagem));
    }
}
