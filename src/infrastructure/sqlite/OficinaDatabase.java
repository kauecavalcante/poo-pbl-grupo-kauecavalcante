package infrastructure.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class OficinaDatabase implements AutoCloseable {

    private static final String SCHEMA = """
        CREATE TABLE IF NOT EXISTS cliente (
            id TEXT PRIMARY KEY,
            nome TEXT NOT NULL,
            cpf TEXT NOT NULL UNIQUE,
            telefone TEXT NOT NULL
        );
        CREATE TABLE IF NOT EXISTS veiculo (
            id TEXT PRIMARY KEY,
            placa TEXT NOT NULL UNIQUE,
            marca TEXT NOT NULL,
            modelo TEXT NOT NULL,
            ano INTEGER NOT NULL,
            dono_id TEXT NOT NULL,
            FOREIGN KEY (dono_id) REFERENCES cliente(id)
        );
        CREATE TABLE IF NOT EXISTS peca (
            id TEXT PRIMARY KEY,
            codigo TEXT NOT NULL UNIQUE,
            descricao TEXT NOT NULL,
            preco_centavos INTEGER NOT NULL,
            preco_moeda TEXT NOT NULL,
            estoque INTEGER NOT NULL
        );
        CREATE TABLE IF NOT EXISTS orcamento (
            id TEXT PRIMARY KEY,
            status TEXT NOT NULL,
            motivo_rejeicao TEXT
        );
        CREATE TABLE IF NOT EXISTS item_orcamento (
            id TEXT PRIMARY KEY,
            orcamento_id TEXT NOT NULL,
            tipo TEXT NOT NULL,
            peca_id TEXT,
            descricao TEXT NOT NULL,
            preco_centavos INTEGER NOT NULL,
            preco_moeda TEXT NOT NULL,
            quantidade INTEGER NOT NULL,
            FOREIGN KEY (orcamento_id) REFERENCES orcamento(id)
        );
        CREATE TABLE IF NOT EXISTS ordem_servico (
            id TEXT PRIMARY KEY,
            cliente_id TEXT NOT NULL,
            veiculo_id TEXT NOT NULL,
            orcamento_id TEXT,
            diagnostico TEXT,
            status TEXT NOT NULL,
            motivo_rejeicao TEXT,
            motivo_cancelamento TEXT,
            data_abertura TEXT NOT NULL,
            data_conclusao TEXT,
            data_entrega TEXT,
            FOREIGN KEY (cliente_id) REFERENCES cliente(id),
            FOREIGN KEY (veiculo_id) REFERENCES veiculo(id),
            FOREIGN KEY (orcamento_id) REFERENCES orcamento(id)
        );
        """;

    private final Connection conexao;

    public OficinaDatabase(String urlJdbc) {
        try {
            this.conexao = DriverManager.getConnection(urlJdbc);
            try (Statement stmt = conexao.createStatement()) {
                // PRAGMA fora do schema porque precisa ser definido por conexão,
                // não vale como DDL idempotente no banco.
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            criarSchema();
        } catch (SQLException e) {
            throw new FalhaDePersistencia("falha ao abrir conexão SQLite", e);
        }
    }

    public Connection conexao() {
        return conexao;
    }

    @Override
    public void close() {
        try {
            if (!conexao.isClosed()) {
                conexao.close();
            }
        } catch (SQLException e) {
            throw new FalhaDePersistencia("falha ao fechar conexão SQLite", e);
        }
    }

    private void criarSchema() throws SQLException {
        try (Statement stmt = conexao.createStatement()) {
            for (String ddl : SCHEMA.split(";")) {
                String comando = ddl.trim();
                if (!comando.isEmpty()) {
                    stmt.execute(comando);
                }
            }
        }
    }
}
