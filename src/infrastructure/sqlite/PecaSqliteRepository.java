package infrastructure.sqlite;

import domain.peca.Peca;
import domain.peca.PecaId;
import domain.peca.PecaRepository;
import domain.shared.Dinheiro;
import domain.shared.Preco;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Currency;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class PecaSqliteRepository implements PecaRepository {

    private final OficinaDatabase database;

    public PecaSqliteRepository(OficinaDatabase database) {
        this.database = Objects.requireNonNull(database, "database");
    }

    @Override
    public void salvar(Peca peca) {
        Objects.requireNonNull(peca, "peca");
        String sql = """
            INSERT INTO peca (id, codigo, descricao, preco_centavos, preco_moeda, estoque)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                codigo = excluded.codigo,
                descricao = excluded.descricao,
                preco_centavos = excluded.preco_centavos,
                preco_moeda = excluded.preco_moeda,
                estoque = excluded.estoque
            """;
        try (PreparedStatement ps = database.conexao().prepareStatement(sql)) {
            ps.setString(1, peca.id().toString());
            ps.setString(2, peca.codigo());
            ps.setString(3, peca.descricao());
            ps.setLong(4, paraCentavos(peca.preco().valor()));
            ps.setString(5, peca.preco().valor().moeda().getCurrencyCode());
            ps.setInt(6, peca.estoque());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new FalhaDePersistencia("falha ao salvar peça", e);
        }
    }

    @Override
    public Optional<Peca> buscarPorId(PecaId id) {
        if (id == null) {
            return Optional.empty();
        }
        return buscarUnico(
            "SELECT id, codigo, descricao, preco_centavos, preco_moeda, estoque FROM peca WHERE id = ?",
            id.toString()
        );
    }

    @Override
    public Optional<Peca> buscarPorCodigo(String codigo) {
        if (codigo == null) {
            return Optional.empty();
        }
        return buscarUnico(
            "SELECT id, codigo, descricao, preco_centavos, preco_moeda, estoque FROM peca WHERE codigo = ?",
            codigo
        );
    }

    private Optional<Peca> buscarUnico(String sql, String parametro) {
        try (PreparedStatement ps = database.conexao().prepareStatement(sql)) {
            ps.setString(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(montar(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new FalhaDePersistencia("falha ao buscar peça", e);
        }
    }

    private Peca montar(ResultSet rs) throws SQLException {
        Preco preco = lerPreco(rs.getLong("preco_centavos"), rs.getString("preco_moeda"));
        return Peca.reconstituir(
            PecaId.de(UUID.fromString(rs.getString("id"))),
            rs.getString("codigo"),
            rs.getString("descricao"),
            preco,
            rs.getInt("estoque")
        );
    }

    // Mapping monetário: armazenamos centavos em INTEGER (sem perda de
    // precisão, sem double) e o código ISO da moeda em TEXT separado.
    // Reconstrução usa BigDecimal.valueOf(unscaled, scale) — equivalente
    // a centavos / 100 em precisão decimal.
    private static long paraCentavos(Dinheiro dinheiro) {
        return dinheiro.valor().multiply(BigDecimal.valueOf(100)).longValueExact();
    }

    private static Preco lerPreco(long centavos, String moeda) {
        Dinheiro dinheiro = Dinheiro.de(BigDecimal.valueOf(centavos, 2), Currency.getInstance(moeda));
        return Preco.de(dinheiro);
    }
}
