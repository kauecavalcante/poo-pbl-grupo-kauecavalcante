package infrastructure.memoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import domain.orcamento.Orcamento;
import domain.orcamento.OrcamentoId;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrcamentoEmMemoriaRepositoryTest {

    @Test
    @DisplayName("salvar e buscarPorId devolve a entidade exata salva")
    void salvarEBuscar() {
        OrcamentoEmMemoriaRepository repo = new OrcamentoEmMemoriaRepository();
        Orcamento o = Orcamento.novo();
        repo.salvar(o);
        assertSame(o, repo.buscarPorId(o.id()).orElseThrow());
    }

    @Test
    @DisplayName("buscarPorId com ID inexistente retorna Optional.empty")
    void buscarPorIdInexistente() {
        OrcamentoEmMemoriaRepository repo = new OrcamentoEmMemoriaRepository();
        assertEquals(Optional.empty(), repo.buscarPorId(OrcamentoId.novo()));
    }

    @Test
    @DisplayName("buscarPorId com null retorna Optional.empty")
    void buscarPorIdNulo() {
        OrcamentoEmMemoriaRepository repo = new OrcamentoEmMemoriaRepository();
        assertEquals(Optional.empty(), repo.buscarPorId(null));
    }

    @Test
    @DisplayName("salvar com mesmo ID substitui a versão anterior")
    void atualizar() {
        OrcamentoEmMemoriaRepository repo = new OrcamentoEmMemoriaRepository();
        Orcamento o = Orcamento.novo();
        Orcamento atualizado = Orcamento.reconstituir(
            o.id(), java.util.List.of(), new domain.orcamento.Rascunho()
        );
        repo.salvar(o);
        repo.salvar(atualizado);
        assertSame(atualizado, repo.buscarPorId(o.id()).orElseThrow());
    }

    @Test
    @DisplayName("salvar(null) lança NullPointerException com mensagem orcamento")
    void salvarNulo() {
        OrcamentoEmMemoriaRepository repo = new OrcamentoEmMemoriaRepository();
        NullPointerException ex = assertThrows(NullPointerException.class, () -> repo.salvar(null));
        assertEquals("orcamento", ex.getMessage());
    }
}
