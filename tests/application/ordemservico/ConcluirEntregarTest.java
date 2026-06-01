package application.ordemservico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.excecao.OrdemDeServicoNaoEncontrada;
import domain.cliente.ClienteId;
import domain.orcamento.Orcamento;
import domain.orcamento.OrcamentoId;
import domain.ordemservico.OrdemDeServico;
import domain.ordemservico.OrdemDeServicoId;
import domain.ordemservico.OrdemDeServicoRepository;
import domain.ordemservico.StatusOS;
import domain.shared.Preco;
import domain.veiculo.VeiculoId;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConcluirEntregarTest {

    @Mock private OrdemDeServicoRepository osRepository;

    private static OrdemDeServico osEmExecucao() {
        Orcamento o = Orcamento.novo();
        o.adicionarItemDeMaoDeObra("Troca de óleo", Preco.deReais("100.00"), 1);
        o.enviar();
        o.aprovar();
        OrcamentoId orcamentoId = o.id();

        OrdemDeServico os = OrdemDeServico.abrir(ClienteId.novo(), VeiculoId.novo());
        os.registrarDiagnostico("Suspensão dianteira com folga");
        os.anexarOrcamento(orcamentoId);
        os.aprovar();
        return os;
    }

    @Nested
    @DisplayName("ConcluirServico")
    class ConcluirTest {

        @Test
        @DisplayName("conclusão feliz transita para CONCLUIDA e persiste")
        void conclusaoFeliz() {
            OrdemDeServico os = osEmExecucao();
            when(osRepository.buscarPorId(os.id())).thenReturn(Optional.of(os));

            ConcluirServico sut = new ConcluirServico(osRepository);
            sut.executar(os.id());

            assertEquals(StatusOS.CONCLUIDA, os.status());
            verify(osRepository).salvar(os);
        }

        @Test
        @DisplayName("OS não encontrada lança OrdemDeServicoNaoEncontrada")
        void osNaoEncontrada() {
            OrdemDeServicoId id = OrdemDeServicoId.novo();
            when(osRepository.buscarPorId(id)).thenReturn(Optional.empty());

            ConcluirServico sut = new ConcluirServico(osRepository);
            assertThrows(OrdemDeServicoNaoEncontrada.class, () -> sut.executar(id));
            verify(osRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("estado errado propaga IllegalStateException do domínio sem salvar")
        void estadoErrado() {
            OrdemDeServico osRecebida = OrdemDeServico.abrir(ClienteId.novo(), VeiculoId.novo());
            when(osRepository.buscarPorId(osRecebida.id())).thenReturn(Optional.of(osRecebida));

            ConcluirServico sut = new ConcluirServico(osRepository);
            assertThrows(IllegalStateException.class, () -> sut.executar(osRecebida.id()));
            verify(osRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("construtor rejeita repositório nulo")
        void rejeitaRepoNulo() {
            assertThrows(NullPointerException.class, () -> new ConcluirServico(null));
        }
    }

    @Nested
    @DisplayName("EntregarVeiculo")
    class EntregarTest {

        @Test
        @DisplayName("entrega feliz transita para ENTREGUE e persiste")
        void entregaFeliz() {
            OrdemDeServico os = osEmExecucao();
            os.concluir();
            when(osRepository.buscarPorId(os.id())).thenReturn(Optional.of(os));

            EntregarVeiculo sut = new EntregarVeiculo(osRepository);
            sut.executar(os.id());

            assertEquals(StatusOS.ENTREGUE, os.status());
            verify(osRepository).salvar(os);
        }

        @Test
        @DisplayName("OS não encontrada lança OrdemDeServicoNaoEncontrada")
        void osNaoEncontrada() {
            OrdemDeServicoId id = OrdemDeServicoId.novo();
            when(osRepository.buscarPorId(id)).thenReturn(Optional.empty());

            EntregarVeiculo sut = new EntregarVeiculo(osRepository);
            assertThrows(OrdemDeServicoNaoEncontrada.class, () -> sut.executar(id));
            verify(osRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("estado errado propaga IllegalStateException do domínio sem salvar")
        void estadoErrado() {
            OrdemDeServico osEmExec = osEmExecucao();
            when(osRepository.buscarPorId(osEmExec.id())).thenReturn(Optional.of(osEmExec));

            EntregarVeiculo sut = new EntregarVeiculo(osRepository);
            assertThrows(IllegalStateException.class, () -> sut.executar(osEmExec.id()));
            verify(osRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("construtor rejeita repositório nulo")
        void rejeitaRepoNulo() {
            assertThrows(NullPointerException.class, () -> new EntregarVeiculo(null));
        }
    }
}
