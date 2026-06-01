package domain.ordemservico;

import domain.cliente.ClienteId;
import domain.orcamento.OrcamentoId;
import domain.veiculo.VeiculoId;
import java.time.LocalDate;
import java.util.Optional;

public final class OrdemDeServico {

    private final OrdemDeServicoId id;
    private final ClienteId clienteId;
    private final VeiculoId veiculoId;
    private OrcamentoId orcamentoId;
    private String diagnostico;
    private EstadoOS estado;
    private final LocalDate dataAbertura;
    private LocalDate dataConclusao;
    private LocalDate dataEntrega;

    private OrdemDeServico(OrdemDeServicoId id, ClienteId clienteId, VeiculoId veiculoId,
                           OrcamentoId orcamentoId, String diagnostico, EstadoOS estado,
                           LocalDate dataAbertura, LocalDate dataConclusao, LocalDate dataEntrega) {
        this.id = id;
        this.clienteId = clienteId;
        this.veiculoId = veiculoId;
        this.orcamentoId = orcamentoId;
        this.diagnostico = diagnostico;
        this.estado = estado;
        this.dataAbertura = dataAbertura;
        this.dataConclusao = dataConclusao;
        this.dataEntrega = dataEntrega;
    }

    public static OrdemDeServico abrir(ClienteId clienteId, VeiculoId veiculoId) {
        if (clienteId == null) {
            throw new IllegalArgumentException("cliente da OS não pode ser nulo");
        }
        if (veiculoId == null) {
            throw new IllegalArgumentException("veículo da OS não pode ser nulo");
        }
        return new OrdemDeServico(
            OrdemDeServicoId.novo(),
            clienteId,
            veiculoId,
            null,
            null,
            new Recebida(),
            LocalDate.now(),
            null,
            null
        );
    }

    public OrdemDeServicoId id() {
        return id;
    }

    public ClienteId clienteId() {
        return clienteId;
    }

    public VeiculoId veiculoId() {
        return veiculoId;
    }

    public StatusOS status() {
        return estado.status();
    }

    public LocalDate dataAbertura() {
        return dataAbertura;
    }

    // Campos opcionais armazenados como nullable — referência de campo é o
    // modelo idiomático para "valor pode não existir ainda". O wrapping em
    // Optional fica para o ponto de acesso público, onde sinaliza ao
    // chamador que o valor pode estar ausente sem expor null.
    public Optional<OrcamentoId> orcamentoId() {
        return Optional.ofNullable(orcamentoId);
    }

    public Optional<String> diagnostico() {
        return Optional.ofNullable(diagnostico);
    }

    public Optional<LocalDate> dataConclusao() {
        return Optional.ofNullable(dataConclusao);
    }

    public Optional<LocalDate> dataEntrega() {
        return Optional.ofNullable(dataEntrega);
    }

    public Optional<String> motivoRejeicao() {
        return estado instanceof Rejeitada r ? Optional.of(r.motivo()) : Optional.empty();
    }

    public Optional<String> motivoCancelamento() {
        return estado instanceof Cancelada c ? Optional.of(c.motivo()) : Optional.empty();
    }

    public void registrarDiagnostico(String diagnostico) {
        if (status() != StatusOS.RECEBIDA) {
            throw new IllegalStateException("diagnóstico só pode ser registrado em OS no estado RECEBIDA");
        }
        this.diagnostico = validarDiagnostico(diagnostico);
    }

    public void anexarOrcamento(OrcamentoId orcamentoId) {
        if (status() != StatusOS.RECEBIDA) {
            throw new IllegalStateException("orçamento só pode ser anexado em OS no estado RECEBIDA");
        }
        if (orcamentoId == null) {
            throw new IllegalArgumentException("orcamentoId não pode ser nulo");
        }
        if (this.diagnostico == null) {
            throw new IllegalStateException("diagnóstico precisa estar registrado antes de anexar orçamento");
        }
        this.orcamentoId = orcamentoId;
        this.estado = this.estado.aoEnviarParaAprovacao();
    }

    public void aprovar() {
        this.estado = this.estado.aoAprovar();
    }

    public void rejeitar(String motivo) {
        this.estado = this.estado.aoRejeitar(motivo);
    }

    public void concluir() {
        this.estado = this.estado.aoConcluir();
        this.dataConclusao = LocalDate.now();
    }

    public void entregar() {
        this.estado = this.estado.aoEntregar();
        this.dataEntrega = LocalDate.now();
    }

    private static String validarDiagnostico(String diagnostico) {
        if (diagnostico == null || diagnostico.trim().isEmpty()) {
            throw new IllegalArgumentException("diagnóstico não pode ser vazio");
        }
        String normalizado = diagnostico.trim();
        if (normalizado.length() < 5) {
            throw new IllegalArgumentException("diagnóstico deve ter ao menos 5 caracteres");
        }
        return normalizado;
    }
}
