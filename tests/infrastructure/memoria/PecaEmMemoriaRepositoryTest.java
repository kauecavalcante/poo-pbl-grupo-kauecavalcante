package infrastructure.memoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.peca.Peca;
import domain.peca.PecaId;
import domain.shared.Preco;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PecaEmMemoriaRepositoryTest {

    private static final Preco PRECO = Preco.deReais("199.90");

    @Test
    @DisplayName("salvar e buscarPorId devolve a entidade exata salva")
    void salvarEBuscar() {
        PecaEmMemoriaRepository repo = new PecaEmMemoriaRepository();
        Peca p = Peca.nova("FLT-1001", "Filtro de óleo", PRECO, 10);
        repo.salvar(p);
        assertSame(p, repo.buscarPorId(p.id()).orElseThrow());
    }

    @Test
    @DisplayName("buscarPorId com ID inexistente retorna Optional.empty")
    void buscarPorIdInexistente() {
        PecaEmMemoriaRepository repo = new PecaEmMemoriaRepository();
        assertEquals(Optional.empty(), repo.buscarPorId(PecaId.novo()));
    }

    @Test
    @DisplayName("buscarPorId com null retorna Optional.empty")
    void buscarPorIdNulo() {
        PecaEmMemoriaRepository repo = new PecaEmMemoriaRepository();
        assertEquals(Optional.empty(), repo.buscarPorId(null));
    }

    @Test
    @DisplayName("salvar com mesmo ID substitui a versão anterior")
    void atualizar() {
        PecaEmMemoriaRepository repo = new PecaEmMemoriaRepository();
        PecaId id = PecaId.novo();
        Peca antes = Peca.reconstituir(id, "FLT-1001", "Filtro de óleo", PRECO, 10);
        Peca depois = Peca.reconstituir(id, "FLT-1001", "Filtro premium", Preco.deReais("250.00"), 5);
        repo.salvar(antes);
        repo.salvar(depois);
        assertSame(depois, repo.buscarPorId(id).orElseThrow());
    }

    @Test
    @DisplayName("salvar(null) lança NullPointerException com mensagem peca")
    void salvarNulo() {
        PecaEmMemoriaRepository repo = new PecaEmMemoriaRepository();
        NullPointerException ex = assertThrows(NullPointerException.class, () -> repo.salvar(null));
        assertEquals("peca", ex.getMessage());
    }

    @Test
    @DisplayName("buscarPorCodigo encontra peça cadastrada")
    void buscarPorCodigoFeliz() {
        PecaEmMemoriaRepository repo = new PecaEmMemoriaRepository();
        Peca p = Peca.nova("FLT-1001", "Filtro de óleo", PRECO, 10);
        repo.salvar(p);
        assertSame(p, repo.buscarPorCodigo("FLT-1001").orElseThrow());
    }

    @Test
    @DisplayName("buscarPorCodigo com código não cadastrado retorna Optional.empty")
    void buscarPorCodigoInexistente() {
        PecaEmMemoriaRepository repo = new PecaEmMemoriaRepository();
        repo.salvar(Peca.nova("FLT-1001", "Filtro de óleo", PRECO, 10));
        assertEquals(Optional.empty(), repo.buscarPorCodigo("OUTRO-CODIGO"));
    }

    @Test
    @DisplayName("buscarPorCodigo com null retorna Optional.empty")
    void buscarPorCodigoNulo() {
        PecaEmMemoriaRepository repo = new PecaEmMemoriaRepository();
        assertTrue(repo.buscarPorCodigo(null).isEmpty());
    }
}
