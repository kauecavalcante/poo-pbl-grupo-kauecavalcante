package presentation.controller;

import application.excecao.OrdemDeServicoNaoEncontrada;
import application.ordemservico.AbrirOrdemDeServico;
import application.ordemservico.AprovarOrcamentoDaOS;
import application.ordemservico.CancelarOrdemDeServico;
import application.ordemservico.ConcluirServico;
import application.ordemservico.EntregarVeiculo;
import application.ordemservico.ItemDeMaoDeObraInput;
import application.ordemservico.ItemDePecaInput;
import application.ordemservico.MontarOrcamentoDaOS;
import application.ordemservico.RegistrarDiagnosticoNaOS;
import application.ordemservico.RejeitarOrcamentoDaOS;
import domain.cliente.ClienteId;
import domain.orcamento.OrcamentoId;
import domain.ordemservico.OrdemDeServico;
import domain.ordemservico.OrdemDeServicoId;
import domain.ordemservico.OrdemDeServicoRepository;
import domain.peca.PecaId;
import domain.shared.Preco;
import domain.veiculo.VeiculoId;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import presentation.dto.AbrirOrdemRequest;
import presentation.dto.DiagnosticoRequest;
import presentation.dto.ItemDeMaoDeObraInputDto;
import presentation.dto.ItemDePecaInputDto;
import presentation.dto.MontarOrcamentoRequest;
import presentation.dto.MotivoRequest;
import presentation.dto.OrdemDeServicoResponse;

public final class OrdemDeServicoController {

    private final AbrirOrdemDeServico abrirOS;
    private final RegistrarDiagnosticoNaOS registrarDiagnostico;
    private final MontarOrcamentoDaOS montarOrcamento;
    private final AprovarOrcamentoDaOS aprovar;
    private final RejeitarOrcamentoDaOS rejeitar;
    private final ConcluirServico concluir;
    private final EntregarVeiculo entregar;
    private final CancelarOrdemDeServico cancelar;
    private final OrdemDeServicoRepository osRepository;

    public OrdemDeServicoController(AbrirOrdemDeServico abrirOS,
                                    RegistrarDiagnosticoNaOS registrarDiagnostico,
                                    MontarOrcamentoDaOS montarOrcamento,
                                    AprovarOrcamentoDaOS aprovar,
                                    RejeitarOrcamentoDaOS rejeitar,
                                    ConcluirServico concluir,
                                    EntregarVeiculo entregar,
                                    CancelarOrdemDeServico cancelar,
                                    OrdemDeServicoRepository osRepository) {
        this.abrirOS = Objects.requireNonNull(abrirOS, "abrirOS");
        this.registrarDiagnostico = Objects.requireNonNull(registrarDiagnostico, "registrarDiagnostico");
        this.montarOrcamento = Objects.requireNonNull(montarOrcamento, "montarOrcamento");
        this.aprovar = Objects.requireNonNull(aprovar, "aprovar");
        this.rejeitar = Objects.requireNonNull(rejeitar, "rejeitar");
        this.concluir = Objects.requireNonNull(concluir, "concluir");
        this.entregar = Objects.requireNonNull(entregar, "entregar");
        this.cancelar = Objects.requireNonNull(cancelar, "cancelar");
        this.osRepository = Objects.requireNonNull(osRepository, "osRepository");
    }

    public void registrar(Javalin app) {
        app.post("/ordens", this::abrir);
        app.get("/ordens/{id}", this::buscar);
        app.post("/ordens/{id}/diagnostico", this::diagnostico);
        app.post("/ordens/{id}/orcamento", this::orcamento);
        app.post("/ordens/{id}/aprovar", this::aprovar);
        app.post("/ordens/{id}/rejeitar", this::rejeitar);
        app.post("/ordens/{id}/concluir", this::concluir);
        app.post("/ordens/{id}/entregar", this::entregar);
        app.post("/ordens/{id}/cancelar", this::cancelar);
    }

    private void abrir(Context ctx) {
        AbrirOrdemRequest req = ctx.bodyAsClass(AbrirOrdemRequest.class);
        ClienteId clienteId = ClienteId.de(req.clienteId());
        VeiculoId veiculoId = VeiculoId.de(req.veiculoId());
        OrdemDeServicoId id = abrirOS.executar(clienteId, veiculoId);
        ctx.status(201).header("Location", "/ordens/" + id).json(new IdResponse(id.toString()));
    }

    private void buscar(Context ctx) {
        OrdemDeServicoId id = parseId(ctx);
        OrdemDeServico os = osRepository.buscarPorId(id)
            .orElseThrow(() -> new OrdemDeServicoNaoEncontrada(id));
        ctx.json(toResponse(os));
    }

    private void diagnostico(Context ctx) {
        OrdemDeServicoId id = parseId(ctx);
        DiagnosticoRequest req = ctx.bodyAsClass(DiagnosticoRequest.class);
        registrarDiagnostico.executar(id, req.diagnostico());
        ctx.status(204);
    }

    private void orcamento(Context ctx) {
        OrdemDeServicoId id = parseId(ctx);
        MontarOrcamentoRequest req = ctx.bodyAsClass(MontarOrcamentoRequest.class);
        List<ItemDePecaInput> pecas = req.pecas() == null ? List.of() :
            req.pecas().stream()
                .map(dto -> new ItemDePecaInput(PecaId.de(dto.pecaId()), dto.quantidade()))
                .toList();
        List<ItemDeMaoDeObraInput> maos = req.maosDeObra() == null ? List.of() :
            req.maosDeObra().stream()
                .map(dto -> new ItemDeMaoDeObraInput(dto.descricao(), Preco.deReais(dto.precoReais()), dto.quantidade()))
                .toList();
        OrcamentoId orcId = montarOrcamento.executar(id, pecas, maos);
        ctx.status(201).json(new IdResponse(orcId.toString()));
    }

    private void aprovar(Context ctx) {
        aprovar.executar(parseId(ctx));
        ctx.status(204);
    }

    private void rejeitar(Context ctx) {
        MotivoRequest req = ctx.bodyAsClass(MotivoRequest.class);
        rejeitar.executar(parseId(ctx), req.motivo());
        ctx.status(204);
    }

    private void concluir(Context ctx) {
        concluir.executar(parseId(ctx));
        ctx.status(204);
    }

    private void entregar(Context ctx) {
        entregar.executar(parseId(ctx));
        ctx.status(204);
    }

    private void cancelar(Context ctx) {
        MotivoRequest req = ctx.bodyAsClass(MotivoRequest.class);
        cancelar.executar(parseId(ctx), req.motivo());
        ctx.status(204);
    }

    private static OrdemDeServicoResponse toResponse(OrdemDeServico os) {
        return new OrdemDeServicoResponse(
            os.id().toString(),
            os.clienteId().toString(),
            os.veiculoId().toString(),
            os.status().name(),
            os.orcamentoId().map(OrcamentoId::toString).orElse(null),
            os.diagnostico().orElse(null),
            os.motivoRejeicao().orElse(null),
            os.motivoCancelamento().orElse(null),
            localDateStr(os.dataAbertura()),
            os.dataConclusao().map(OrdemDeServicoController::localDateStr).orElse(null),
            os.dataEntrega().map(OrdemDeServicoController::localDateStr).orElse(null)
        );
    }

    private static String localDateStr(LocalDate date) {
        return date == null ? null : date.toString();
    }

    private static OrdemDeServicoId parseId(Context ctx) {
        try {
            return OrdemDeServicoId.de(ctx.pathParam("id"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("id de ordem inválido: " + ctx.pathParam("id"));
        }
    }

    private record IdResponse(String id) {}
}
