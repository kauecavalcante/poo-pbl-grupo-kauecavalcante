package infrastructure.sqlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OficinaDatabaseTest {

    private static final String EM_MEMORIA = "jdbc:sqlite::memory:";

    @Test
    @DisplayName("abre conexão em memória e expõe getter")
    void abreConexaoEmMemoria() throws Exception {
        try (OficinaDatabase db = new OficinaDatabase(EM_MEMORIA)) {
            assertFalse(db.conexao().isClosed());
        }
    }

    @Test
    @DisplayName("criação do schema cria todas as 6 tabelas")
    void schemaCriaTodasAsTabelas() throws Exception {
        try (OficinaDatabase db = new OficinaDatabase(EM_MEMORIA)) {
            Set<String> tabelas = new HashSet<>();
            try (Statement stmt = db.conexao().createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table'")) {
                while (rs.next()) {
                    tabelas.add(rs.getString(1));
                }
            }
            assertTrue(tabelas.contains("cliente"));
            assertTrue(tabelas.contains("veiculo"));
            assertTrue(tabelas.contains("peca"));
            assertTrue(tabelas.contains("orcamento"));
            assertTrue(tabelas.contains("item_orcamento"));
            assertTrue(tabelas.contains("ordem_servico"));
        }
    }

    @Test
    @DisplayName("abrir o mesmo arquivo duas vezes não quebra (CREATE IF NOT EXISTS)")
    void schemaEhIdempotente(@TempDir Path tempDir) {
        String url = "jdbc:sqlite:" + tempDir.resolve("oficina.db").toAbsolutePath();
        new OficinaDatabase(url).close();
        // Segunda abertura sobre arquivo existente deve rodar schema sem erros.
        new OficinaDatabase(url).close();
    }

    @Test
    @DisplayName("foreign keys são habilitadas na conexão")
    void foreignKeysHabilitadas() throws Exception {
        try (OficinaDatabase db = new OficinaDatabase(EM_MEMORIA)) {
            try (Statement stmt = db.conexao().createStatement();
                 ResultSet rs = stmt.executeQuery("PRAGMA foreign_keys")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
            }
        }
    }

    @Test
    @DisplayName("close fecha a conexão")
    void closeFechaConexao() throws Exception {
        OficinaDatabase db = new OficinaDatabase(EM_MEMORIA);
        db.close();
        assertTrue(db.conexao().isClosed());
    }

    @Test
    @DisplayName("URL inválida lança FalhaDePersistencia")
    void urlInvalidaLancaFalha() {
        assertThrows(
            FalhaDePersistencia.class,
            () -> new OficinaDatabase("jdbc:nada-de-mundo:nada")
        );
    }
}
