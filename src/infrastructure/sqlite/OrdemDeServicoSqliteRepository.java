package infrastructure.sqlite;

import domain.cliente.ClienteId;
import domain.orcamento.OrcamentoId;
import domain.ordemservico.AguardandoAprovacao;
import domain.ordemservico.Cancelada;
import domain.ordemservico.Concluida;
import domain.ordemservico.EmExecucao;
import domain.ordemservico.Entregue;
import domain.ordemservico.EstadoOS;
import domain.ordemservico.OrdemDeServico;
import domain.ordemservico.OrdemDeServicoId;
import domain.ordemservico.OrdemDeServicoRepository;
import domain.ordemservico.Recebida;
import domain.ordemservico.Rejeitada;
import domain.ordemservico.StatusOS;
import domain.veiculo.VeiculoId;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class OrdemDeServicoSqliteRepository implements OrdemDeServicoRepository {

    private final OficinaDatabase database;

    public OrdemDeServicoSqliteRepository(OficinaDatabase database) {
        this.database = Objects.requireNonNull(database, "database");
    }

    @Override
    public void salvar(OrdemDeServico os) {
        Objects.requireNonNull(os, "ordemDeServico");
        String sql = """
            INSERT INTO ordem_servico (
                id, cliente_id, veiculo_id, orcamento_id, diagnostico,
                status, motivo_rejeicao, motivo_cancelamento,
                data_abertura, data_conclusao, data_entrega
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                cliente_id = excluded.cliente_id,
                veiculo_id = excluded.veiculo_id,
                orcamento_id = excluded.orcamento_id,
                diagnostico = excluded.diagnostico,
                status = excluded.status,
                motivo_rejeicao = excluded.motivo_rejeicao,
                motivo_cancelamento = excluded.motivo_cancelamento,
                data_abertura = excluded.data_abertura,
                data_conclusao = excluded.data_conclusao,
                data_entrega = excluded.data_entrega
            """;
        try (PreparedStatement ps = database.conexao().prepareStatement(sql)) {
            ps.setString(1, os.id().toString());
            ps.setString(2, os.clienteId().toString());
            ps.setString(3, os.veiculoId().toString());
            setStringOuNulo(ps, 4, os.orcamentoId().map(OrcamentoId::toString).orElse(null));
            setStringOuNulo(ps, 5, os.diagnostico().orElse(null));
            ps.setString(6, os.status().name());
            setStringOuNulo(ps, 7, os.motivoRejeicao().orElse(null));
            setStringOuNulo(ps, 8, os.motivoCancelamento().orElse(null));
            ps.setString(9, os.dataAbertura().toString());
            setStringOuNulo(ps, 10, os.dataConclusao().map(LocalDate::toString).orElse(null));
            setStringOuNulo(ps, 11, os.dataEntrega().map(LocalDate::toString).orElse(null));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new FalhaDePersistencia("falha ao salvar ordem de serviço", e);
        }
    }

    @Override
    public Optional<OrdemDeServico> buscarPorId(OrdemDeServicoId id) {
        if (id == null) {
            return Optional.empty();
        }
        String sql = """
            SELECT id, cliente_id, veiculo_id, orcamento_id, diagnostico,
                   status, motivo_rejeicao, motivo_cancelamento,
                   data_abertura, data_conclusao, data_entrega
            FROM ordem_servico WHERE id = ?
            """;
        try (PreparedStatement ps = database.conexao().prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(montar(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new FalhaDePersistencia("falha ao buscar ordem de serviço", e);
        }
    }

    private OrdemDeServico montar(ResultSet rs) throws SQLException {
        OrdemDeServicoId id = OrdemDeServicoId.de(UUID.fromString(rs.getString("id")));
        ClienteId clienteId = ClienteId.de(UUID.fromString(rs.getString("cliente_id")));
        VeiculoId veiculoId = VeiculoId.de(UUID.fromString(rs.getString("veiculo_id")));
        String orcamentoIdStr = rs.getString("orcamento_id");
        OrcamentoId orcamentoId = orcamentoIdStr == null ? null : OrcamentoId.de(UUID.fromString(orcamentoIdStr));
        String diagnostico = rs.getString("diagnostico");
        StatusOS status = StatusOS.valueOf(rs.getString("status"));
        String motivoRejeicao = rs.getString("motivo_rejeicao");
        String motivoCancelamento = rs.getString("motivo_cancelamento");
        EstadoOS estado = reconstruirEstado(status, motivoRejeicao, motivoCancelamento);
        LocalDate dataAbertura = LocalDate.parse(rs.getString("data_abertura"));
        LocalDate dataConclusao = lerDataOpcional(rs.getString("data_conclusao"));
        LocalDate dataEntrega = lerDataOpcional(rs.getString("data_entrega"));
        return OrdemDeServico.reconstituir(
            id, clienteId, veiculoId, orcamentoId, diagnostico,
            estado, dataAbertura, dataConclusao, dataEntrega
        );
    }

    // Switch exaustivo sobre o StatusOS para reconstruir a classe concreta
    // do estado. Estados terminais com payload (Rejeitada/Cancelada) puxam
    // o motivo da coluna correspondente; demais estados ignoram-no.
    private static EstadoOS reconstruirEstado(StatusOS status, String motivoRejeicao, String motivoCancelamento) {
        return switch (status) {
            case RECEBIDA -> new Recebida();
            case AGUARDANDO_APROVACAO -> new AguardandoAprovacao();
            case EM_EXECUCAO -> new EmExecucao();
            case CONCLUIDA -> new Concluida();
            case ENTREGUE -> new Entregue();
            case REJEITADA -> new Rejeitada(motivoRejeicao);
            case CANCELADA -> new Cancelada(motivoCancelamento);
        };
    }

    private static LocalDate lerDataOpcional(String iso) {
        return iso == null ? null : LocalDate.parse(iso);
    }

    private static void setStringOuNulo(PreparedStatement ps, int indice, String valor) throws SQLException {
        if (valor == null) {
            ps.setNull(indice, Types.VARCHAR);
        } else {
            ps.setString(indice, valor);
        }
    }
}
