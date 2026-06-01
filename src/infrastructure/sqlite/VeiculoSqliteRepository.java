package infrastructure.sqlite;

import domain.cliente.ClienteId;
import domain.veiculo.Placa;
import domain.veiculo.Veiculo;
import domain.veiculo.VeiculoId;
import domain.veiculo.VeiculoRepository;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class VeiculoSqliteRepository implements VeiculoRepository {

    private final OficinaDatabase database;

    public VeiculoSqliteRepository(OficinaDatabase database) {
        this.database = Objects.requireNonNull(database, "database");
    }

    @Override
    public void salvar(Veiculo veiculo) {
        Objects.requireNonNull(veiculo, "veiculo");
        String sql = """
            INSERT INTO veiculo (id, placa, marca, modelo, ano, dono_id) VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                placa = excluded.placa,
                marca = excluded.marca,
                modelo = excluded.modelo,
                ano = excluded.ano,
                dono_id = excluded.dono_id
            """;
        try (PreparedStatement ps = database.conexao().prepareStatement(sql)) {
            ps.setString(1, veiculo.id().toString());
            ps.setString(2, veiculo.placa().valor());
            ps.setString(3, veiculo.marca());
            ps.setString(4, veiculo.modelo());
            ps.setInt(5, veiculo.ano());
            ps.setString(6, veiculo.dono().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new FalhaDePersistencia("falha ao salvar veículo", e);
        }
    }

    @Override
    public Optional<Veiculo> buscarPorId(VeiculoId id) {
        if (id == null) {
            return Optional.empty();
        }
        return buscarUnico("SELECT id, placa, marca, modelo, ano, dono_id FROM veiculo WHERE id = ?", id.toString());
    }

    @Override
    public Optional<Veiculo> buscarPorPlaca(Placa placa) {
        if (placa == null) {
            return Optional.empty();
        }
        return buscarUnico("SELECT id, placa, marca, modelo, ano, dono_id FROM veiculo WHERE placa = ?", placa.valor());
    }

    @Override
    public List<Veiculo> listarPorDono(ClienteId dono) {
        if (dono == null) {
            return List.of();
        }
        String sql = "SELECT id, placa, marca, modelo, ano, dono_id FROM veiculo WHERE dono_id = ?";
        try (PreparedStatement ps = database.conexao().prepareStatement(sql)) {
            ps.setString(1, dono.toString());
            try (ResultSet rs = ps.executeQuery()) {
                List<Veiculo> veiculos = new ArrayList<>();
                while (rs.next()) {
                    veiculos.add(montar(rs));
                }
                return veiculos;
            }
        } catch (SQLException e) {
            throw new FalhaDePersistencia("falha ao listar veículos por dono", e);
        }
    }

    private Optional<Veiculo> buscarUnico(String sql, String parametro) {
        try (PreparedStatement ps = database.conexao().prepareStatement(sql)) {
            ps.setString(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(montar(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new FalhaDePersistencia("falha ao buscar veículo", e);
        }
    }

    private Veiculo montar(ResultSet rs) throws SQLException {
        return Veiculo.reconstituir(
            VeiculoId.de(UUID.fromString(rs.getString("id"))),
            Placa.de(rs.getString("placa")),
            rs.getString("marca"),
            rs.getString("modelo"),
            rs.getInt("ano"),
            ClienteId.de(UUID.fromString(rs.getString("dono_id")))
        );
    }
}
