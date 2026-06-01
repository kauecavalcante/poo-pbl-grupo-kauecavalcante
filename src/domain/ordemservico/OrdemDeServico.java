package domain.ordemservico;

import domain.cliente.ClienteId;
import domain.orcamento.OrcamentoId;
import domain.veiculo.VeiculoId;
import java.time.LocalDate;
import java.util.Objects;
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

    public static OrdemDeServico reconstituir(OrdemDeServicoId id, ClienteId clienteId, VeiculoId veiculoId,
                                              OrcamentoId orcamentoId, String diagnostico, EstadoOS estado,
                                              LocalDate dataAbertura, LocalDate dataConclusao, LocalDate dataEntrega) {
        if (id == null) {
            throw new IllegalArgumentException("id da ordem de serviço não pode ser nulo");
        }
        if (clienteId == null) {
            throw new IllegalArgumentException("cliente da OS não pode ser nulo");
        }
        if (veiculoId == null) {
            throw new IllegalArgumentException("veículo da OS não pode ser nulo");
        }
        if (estado == null) {
            throw new IllegalArgumentException("estado da OS não pode ser nulo");
        }
        if (dataAbertura == null) {
            throw new IllegalArgumentException("data de abertura não pode ser nula");
        }
        String diagnosticoNormalizado = diagnostico == null ? null : validarDiagnostico(diagnostico);
        validarCoerenciaEstadoDatas(estado.status(), dataConclusao, dataEntrega);
        return new OrdemDeServico(id, clienteId, veiculoId, orcamentoId, diagnosticoNormalizado, estado,
            dataAbertura, dataConclusao, dataEntrega);
    }

    // Coerência cruzada: o estado restringe quais datas finais podem existir.
    // ENTREGUE exige ambas; CONCLUIDA exige só dataConclusao; estados anteriores
    // não podem ter nenhuma. CANCELADA é tolerante a dataConclusao (pode ter
    // sido cancelada após Concluida), mas nunca a dataEntrega.
    private static void validarCoerenciaEstadoDatas(StatusOS status, LocalDate dataConclusao, LocalDate dataEntrega) {
        switch (status) {
            case ENTREGUE -> {
                if (dataConclusao == null) {
                    throw new IllegalArgumentException("OS no estado ENTREGUE precisa ter dataConclusao");
                }
                if (dataEntrega == null) {
                    throw new IllegalArgumentException("OS no estado ENTREGUE precisa ter dataEntrega");
                }
            }
            case CONCLUIDA -> {
                if (dataConclusao == null) {
                    throw new IllegalArgumentException("OS no estado CONCLUIDA precisa ter dataConclusao");
                }
                if (dataEntrega != null) {
                    throw new IllegalArgumentException("OS no estado CONCLUIDA não pode ter dataEntrega");
                }
            }
            case CANCELADA -> {
                if (dataEntrega != null) {
                    throw new IllegalArgumentException("OS no estado CANCELADA não pode ter dataEntrega");
                }
            }
            case REJEITADA, RECEBIDA, AGUARDANDO_APROVACAO, EM_EXECUCAO -> {
                if (dataConclusao != null) {
                    throw new IllegalArgumentException("OS no estado " + status + " não pode ter dataConclusao");
                }
                if (dataEntrega != null) {
                    throw new IllegalArgumentException("OS no estado " + status + " não pode ter dataEntrega");
                }
            }
        }
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

    public void cancelar(String motivo) {
        this.estado = this.estado.aoCancelar(motivo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrdemDeServico outra)) {
            return false;
        }
        return id.equals(outra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("OrdemDeServico{id=").append(id)
            .append(", status=").append(estado.status())
            .append(", clienteId=").append(clienteId)
            .append(", veiculoId=").append(veiculoId)
            .append(", dataAbertura=").append(dataAbertura);
        if (orcamentoId != null) {
            sb.append(", orcamentoId=").append(orcamentoId);
        }
        return sb.append("}").toString();
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
