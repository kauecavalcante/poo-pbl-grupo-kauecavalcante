package infrastructure.sqlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.peca.Peca;
import domain.peca.PecaId;
import domain.shared.Preco;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PecaSqliteRepositoryTest {

    private OficinaDatabase database;
    private PecaSqliteRepository repo;

    @BeforeEach
    void setup() {
        database = new OficinaDatabase("jdbc:sqlite::memory:");
        repo = new PecaSqliteRepository(database);
    }

    @AfterEach
    void teardown() {
        database.close();
    }

    @Test
    @DisplayName("salvar e buscar por id devolve peça equivalente")
    void roundtripPorId() {
        Peca original = Peca.nova("FLT-1001", "Filtro de óleo", Preco.deReais("199.90"), 10);
        repo.salvar(original);

        Peca recuperada = repo.buscarPorId(original.id()).orElseThrow();
        assertEquals(original.id(), recuperada.id());
        assertEquals("FLT-1001", recuperada.codigo());
        assertEquals("Filtro de óleo", recuperada.descricao());
        assertEquals(Preco.deReais("199.90"), recuperada.preco());
        assertEquals(10, recuperada.estoque());
    }

    @Test
    @DisplayName("preço com casas decimais não-triviais é preservado em roundtrip")
    void precoComDecimaisPreservado() {
        Peca p = Peca.nova("OLEO-5W30", "Óleo 5W30", Preco.deReais("89.99"), 20);
        repo.salvar(p);
        Peca recuperada = repo.buscarPorId(p.id()).orElseThrow();
        assertEquals(Preco.deReais("89.99"), recuperada.preco());
    }

    @Test
    @DisplayName("salvar duas vezes com mesmo id atualiza a versão persistida")
    void salvarSobrescreve() {
        PecaId id = PecaId.novo();
        repo.salvar(Peca.reconstituir(id, "FLT-1001", "Filtro de óleo", Preco.deReais("199.90"), 10));
        repo.salvar(Peca.reconstituir(id, "FLT-1001", "Filtro premium", Preco.deReais("249.50"), 5));

        Peca recuperada = repo.buscarPorId(id).orElseThrow();
        assertEquals("Filtro premium", recuperada.descricao());
        assertEquals(Preco.deReais("249.50"), recuperada.preco());
        assertEquals(5, recuperada.estoque());
    }

    @Test
    @DisplayName("buscar por id inexistente retorna Optional.empty")
    void buscarPorIdInexistente() {
        assertEquals(Optional.empty(), repo.buscarPorId(PecaId.novo()));
    }

    @Test
    @DisplayName("buscar por id null retorna Optional.empty")
    void buscarPorIdNulo() {
        assertEquals(Optional.empty(), repo.buscarPorId(null));
    }

    @Test
    @DisplayName("buscar por código encontra peça cadastrada")
    void buscarPorCodigoFeliz() {
        Peca p = Peca.nova("FLT-1001", "Filtro de óleo", Preco.deReais("199.90"), 10);
        repo.salvar(p);
        assertEquals(p.id(), repo.buscarPorCodigo("FLT-1001").orElseThrow().id());
    }

    @Test
    @DisplayName("buscar por código inexistente retorna Optional.empty")
    void buscarPorCodigoInexistente() {
        assertEquals(Optional.empty(), repo.buscarPorCodigo("OUTRO-CODIGO"));
    }

    @Test
    @DisplayName("buscar por código null retorna Optional.empty")
    void buscarPorCodigoNulo() {
        assertTrue(repo.buscarPorCodigo(null).isEmpty());
    }

    @Test
    @DisplayName("salvar duas peças com mesmo código viola UNIQUE e lança FalhaDePersistencia")
    void codigoDuplicadoLancaFalha() {
        repo.salvar(Peca.nova("FLT-1001", "Filtro de óleo", Preco.deReais("199.90"), 10));
        Peca segundo = Peca.nova("FLT-1001", "Outra descrição", Preco.deReais("100.00"), 5);
        assertThrows(FalhaDePersistencia.class, () -> repo.salvar(segundo));
    }

    @Test
    @DisplayName("salvar(null) lança NullPointerException")
    void salvarNulo() {
        NullPointerException ex = assertThrows(NullPointerException.class, () -> repo.salvar(null));
        assertEquals("peca", ex.getMessage());
    }

    @Test
    @DisplayName("preço zero é preservado em roundtrip")
    void precoZero() {
        Peca p = Peca.nova("BRINDE-001", "Brinde de cortesia", Preco.zero(), 100);
        repo.salvar(p);
        assertEquals(Preco.zero(), repo.buscarPorId(p.id()).orElseThrow().preco());
    }
}
