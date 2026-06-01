package infrastructure.sqlite;

import domain.cliente.Cliente;
import domain.cliente.ClienteId;
import domain.cliente.ClienteRepository;
import domain.shared.CPF;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class ClienteSqliteRepository implements ClienteRepository {

    private final OficinaDatabase database;

    public ClienteSqliteRepository(OficinaDatabase database) {
        this.database = Objects.requireNonNull(database, "database");
    }

    @Override
    public void salvar(Cliente cliente) {
        Objects.requireNonNull(cliente, "cliente");
        // UPSERT por id: conflito no id atualiza a linha; conflitos em outras
        // constraints UNIQUE (cpf) propagam como falha de constraint —
        // responsabilidade do caso de uso prevenir, não do repositório mascarar.
        String sql = """
            INSERT INTO cliente (id, nome, cpf, telefone) VALUES (?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                nome = excluded.nome,
                cpf = excluded.cpf,
                telefone = excluded.telefone
            """;
        try (PreparedStatement ps = database.conexao().prepareStatement(sql)) {
            ps.setString(1, cliente.id().toString());
            ps.setString(2, cliente.nome());
            ps.setString(3, cliente.cpf().numero());
            ps.setString(4, cliente.telefone());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new FalhaDePersistencia("falha ao salvar cliente", e);
        }
    }

    @Override
    public Optional<Cliente> buscarPorId(ClienteId id) {
        if (id == null) {
            return Optional.empty();
        }
        String sql = "SELECT id, nome, cpf, telefone FROM cliente WHERE id = ?";
        try (PreparedStatement ps = database.conexao().prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(montar(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new FalhaDePersistencia("falha ao buscar cliente por id", e);
        }
    }

    @Override
    public Optional<Cliente> buscarPorCpf(CPF cpf) {
        if (cpf == null) {
            return Optional.empty();
        }
        String sql = "SELECT id, nome, cpf, telefone FROM cliente WHERE cpf = ?";
        try (PreparedStatement ps = database.conexao().prepareStatement(sql)) {
            ps.setString(1, cpf.numero());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(montar(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new FalhaDePersistencia("falha ao buscar cliente por cpf", e);
        }
    }

    private Cliente montar(ResultSet rs) throws SQLException {
        return Cliente.reconstituir(
            ClienteId.de(UUID.fromString(rs.getString("id"))),
            rs.getString("nome"),
            CPF.de(rs.getString("cpf")),
            rs.getString("telefone")
        );
    }
}
