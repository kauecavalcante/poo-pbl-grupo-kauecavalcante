package infrastructure.memoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import domain.cliente.ClienteId;
import domain.ordemservico.OrdemDeServico;
import domain.ordemservico.OrdemDeServicoId;
import domain.veiculo.VeiculoId;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrdemDeServicoEmMemoriaRepositoryTest {

    @Test
    @DisplayName("salvar e buscarPorId devolve a entidade exata salva")
    void salvarEBuscar() {
        OrdemDeServicoEmMemoriaRepository repo = new OrdemDeServicoEmMemoriaRepository();
        OrdemDeServico os = OrdemDeServico.abrir(ClienteId.novo(), VeiculoId.novo());
        repo.salvar(os);
        assertSame(os, repo.buscarPorId(os.id()).orElseThrow());
    }

    @Test
    @DisplayName("buscarPorId com ID inexistente retorna Optional.empty")
    void buscarPorIdInexistente() {
        OrdemDeServicoEmMemoriaRepository repo = new OrdemDeServicoEmMemoriaRepository();
        assertEquals(Optional.empty(), repo.buscarPorId(OrdemDeServicoId.novo()));
    }

    @Test
    @DisplayName("buscarPorId com null retorna Optional.empty")
    void buscarPorIdNulo() {
        OrdemDeServicoEmMemoriaRepository repo = new OrdemDeServicoEmMemoriaRepository();
        assertEquals(Optional.empty(), repo.buscarPorId(null));
    }

    @Test
    @DisplayName("salvar atualiza estado em memória — última versão prevalece")
    void atualizar() {
        OrdemDeServicoEmMemoriaRepository repo = new OrdemDeServicoEmMemoriaRepository();
        OrdemDeServico os = OrdemDeServico.abrir(ClienteId.novo(), VeiculoId.novo());
        repo.salvar(os);
        os.registrarDiagnostico("Suspensão com folga");
        repo.salvar(os);
        assertEquals(Optional.of("Suspensão com folga"),
            repo.buscarPorId(os.id()).orElseThrow().diagnostico());
    }

    @Test
    @DisplayName("salvar(null) lança NullPointerException com mensagem ordemDeServico")
    void salvarNulo() {
        OrdemDeServicoEmMemoriaRepository repo = new OrdemDeServicoEmMemoriaRepository();
        NullPointerException ex = assertThrows(NullPointerException.class, () -> repo.salvar(null));
        assertEquals("ordemDeServico", ex.getMessage());
    }
}
