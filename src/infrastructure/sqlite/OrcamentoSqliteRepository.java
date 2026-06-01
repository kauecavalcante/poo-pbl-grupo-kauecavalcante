package infrastructure.sqlite;

import domain.orcamento.Aprovado;
import domain.orcamento.Enviado;
import domain.orcamento.EstadoOrcamento;
import domain.orcamento.ItemDeOrcamento;
import domain.orcamento.ItemDeOrcamentoId;
import domain.orcamento.Orcamento;
import domain.orcamento.OrcamentoId;
import domain.orcamento.OrcamentoRepository;
import domain.orcamento.Rascunho;
import domain.orcamento.Rejeitado;
import domain.orcamento.StatusOrcamento;
import domain.orcamento.TipoItem;
import domain.peca.PecaId;
import domain.shared.Dinheiro;
import domain.shared.Preco;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class OrcamentoSqliteRepository implements OrcamentoRepository {

    private final OficinaDatabase database;

    public OrcamentoSqliteRepository(OficinaDatabase database) {
        this.database = Objects.requireNonNull(database, "database");
    }

    @Override
    public void salvar(Orcamento orcamento) {
        Objects.requireNonNull(orcamento, "orcamento");
        // Estratégia delete-and-recreate dos itens dentro de uma transação:
        // simples de implementar, fácil de raciocinar sobre, e atômica do
        // ponto de vista do banco. Custo é re-INSERT do mesmo conjunto a
        // cada salvar — aceitável para orçamentos com dezenas de itens.
        Connection conn = database.conexao();
        try {
            conn.setAutoCommit(false);
            try {
                salvarCabecalho(conn, orcamento);
                deletarItens(conn, orcamento.id());
                inserirItens(conn, orcamento);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new FalhaDePersistencia("falha ao salvar orçamento", e);
        }
    }

    @Override
    public Optional<Orcamento> buscarPorId(OrcamentoId id) {
        if (id == null) {
            return Optional.empty();
        }
        String sqlCabecalho = "SELECT id, status, motivo_rejeicao FROM orcamento WHERE id = ?";
        try (PreparedStatement ps = database.conexao().prepareStatement(sqlCabecalho)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                StatusOrcamento status = StatusOrcamento.valueOf(rs.getString("status"));
                String motivoRejeicao = rs.getString("motivo_rejeicao");
                EstadoOrcamento estado = reconstruirEstado(status, motivoRejeicao);
                List<ItemDeOrcamento> itens = carregarItens(id);
                return Optional.of(Orcamento.reconstituir(id, itens, estado));
            }
        } catch (SQLException e) {
            throw new FalhaDePersistencia("falha ao buscar orçamento", e);
        }
    }

    private void salvarCabecalho(Connection conn, Orcamento orcamento) throws SQLException {
        String sql = """
            INSERT INTO orcamento (id, status, motivo_rejeicao) VALUES (?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                status = excluded.status,
                motivo_rejeicao = excluded.motivo_rejeicao
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orcamento.id().toString());
            ps.setString(2, orcamento.status().name());
            String motivo = orcamento.motivoRejeicao().orElse(null);
            if (motivo == null) {
                ps.setNull(3, java.sql.Types.VARCHAR);
            } else {
                ps.setString(3, motivo);
            }
            ps.executeUpdate();
        }
    }

    private void deletarItens(Connection conn, OrcamentoId id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM item_orcamento WHERE orcamento_id = ?")) {
            ps.setString(1, id.toString());
            ps.executeUpdate();
        }
    }

    private void inserirItens(Connection conn, Orcamento orcamento) throws SQLException {
        if (orcamento.ehVazio()) {
            return;
        }
        String sql = """
            INSERT INTO item_orcamento
                (id, orcamento_id, tipo, peca_id, descricao, preco_centavos, preco_moeda, quantidade)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (ItemDeOrcamento item : orcamento.itens()) {
                ps.setString(1, item.id().toString());
                ps.setString(2, orcamento.id().toString());
                ps.setString(3, item.tipo().name());
                String pecaId = item.pecaId().map(PecaId::toString).orElse(null);
                if (pecaId == null) {
                    ps.setNull(4, java.sql.Types.VARCHAR);
                } else {
                    ps.setString(4, pecaId);
                }
                ps.setString(5, item.descricao());
                Dinheiro dinheiro = item.precoUnitario().valor();
                ps.setLong(6, paraCentavos(dinheiro));
                ps.setString(7, dinheiro.moeda().getCurrencyCode());
                ps.setInt(8, item.quantidade());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private List<ItemDeOrcamento> carregarItens(OrcamentoId id) throws SQLException {
        String sql = """
            SELECT id, tipo, peca_id, descricao, preco_centavos, preco_moeda, quantidade
            FROM item_orcamento
            WHERE orcamento_id = ?
            """;
        try (PreparedStatement ps = database.conexao().prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                List<ItemDeOrcamento> itens = new ArrayList<>();
                while (rs.next()) {
                    itens.add(montarItem(rs));
                }
                return itens;
            }
        }
    }

    private ItemDeOrcamento montarItem(ResultSet rs) throws SQLException {
        ItemDeOrcamentoId itemId = ItemDeOrcamentoId.de(UUID.fromString(rs.getString("id")));
        TipoItem tipo = TipoItem.valueOf(rs.getString("tipo"));
        String pecaIdStr = rs.getString("peca_id");
        PecaId pecaId = pecaIdStr == null ? null : PecaId.de(UUID.fromString(pecaIdStr));
        Preco preco = Preco.de(Dinheiro.de(
            BigDecimal.valueOf(rs.getLong("preco_centavos"), 2),
            Currency.getInstance(rs.getString("preco_moeda"))
        ));
        return ItemDeOrcamento.reconstituir(
            itemId, tipo, pecaId, rs.getString("descricao"), preco, rs.getInt("quantidade")
        );
    }

    // Switch exaustivo sobre o status para devolver a classe concreta do
    // estado. Possível graças ao sealed EstadoOrcamento — o compilador
    // garante que adicionar um novo status exigirá um novo case aqui.
    private static EstadoOrcamento reconstruirEstado(StatusOrcamento status, String motivoRejeicao) {
        return switch (status) {
            case RASCUNHO -> new Rascunho();
            case ENVIADO -> new Enviado();
            case APROVADO -> new Aprovado();
            case REJEITADO -> new Rejeitado(motivoRejeicao);
        };
    }

    private static long paraCentavos(Dinheiro dinheiro) {
        return dinheiro.valor().multiply(BigDecimal.valueOf(100)).longValueExact();
    }
}
