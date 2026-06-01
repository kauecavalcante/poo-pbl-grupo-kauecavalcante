package application.ordemservico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.excecao.OrcamentoNaoEncontrado;
import application.excecao.OrdemDeServicoNaoEncontrada;
import domain.cliente.ClienteId;
import domain.orcamento.Orcamento;
import domain.orcamento.OrcamentoId;
import domain.orcamento.OrcamentoRepository;
import domain.orcamento.StatusOrcamento;
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
class AprovarRejeitarOrcamentoDaOSTest {

    @Mock private OrdemDeServicoRepository osRepository;
    @Mock private OrcamentoRepository orcamentoRepository;

    private static OrdemDeServico osComOrcamentoAguardando(OrcamentoId orcamentoId) {
        OrdemDeServico os = OrdemDeServico.abrir(ClienteId.novo(), VeiculoId.novo());
        os.registrarDiagnostico("Suspensão dianteira com folga");
        os.anexarOrcamento(orcamentoId);
        return os;
    }

    private static Orcamento orcamentoEnviado() {
        Orcamento o = Orcamento.novo();
        o.adicionarItemDeMaoDeObra("Troca de óleo", Preco.deReais("100.00"), 1);
        o.enviar();
        return o;
    }

    @Nested
    @DisplayName("AprovarOrcamentoDaOS")
    class AprovarTest {

        @Test
        @DisplayName("aprovação feliz transita ambos os agregados e salva")
        void aprovacaoFeliz() {
            Orcamento orcamento = orcamentoEnviado();
            OrdemDeServico os = osComOrcamentoAguardando(orcamento.id());
            when(osRepository.buscarPorId(os.id())).thenReturn(Optional.of(os));
            when(orcamentoRepository.buscarPorId(orcamento.id())).thenReturn(Optional.of(orcamento));

            AprovarOrcamentoDaOS sut = new AprovarOrcamentoDaOS(osRepository, orcamentoRepository);
            sut.executar(os.id());

            assertEquals(StatusOrcamento.APROVADO, orcamento.status());
            assertEquals(StatusOS.EM_EXECUCAO, os.status());
            verify(orcamentoRepository).salvar(orcamento);
            verify(osRepository).salvar(os);
        }

        @Test
        @DisplayName("OS não encontrada lança OrdemDeServicoNaoEncontrada")
        void osNaoEncontrada() {
            OrdemDeServicoId id = OrdemDeServicoId.novo();
            when(osRepository.buscarPorId(id)).thenReturn(Optional.empty());

            AprovarOrcamentoDaOS sut = new AprovarOrcamentoDaOS(osRepository, orcamentoRepository);
            assertThrows(OrdemDeServicoNaoEncontrada.class, () -> sut.executar(id));
            verify(orcamentoRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("OS sem orçamento anexado lança IllegalStateException")
        void osSemOrcamento() {
            OrdemDeServico osSemOrcamento = OrdemDeServico.abrir(ClienteId.novo(), VeiculoId.novo());
            when(osRepository.buscarPorId(osSemOrcamento.id())).thenReturn(Optional.of(osSemOrcamento));

            AprovarOrcamentoDaOS sut = new AprovarOrcamentoDaOS(osRepository, orcamentoRepository);
            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> sut.executar(osSemOrcamento.id())
            );
            assertEquals("OS não possui orçamento anexado", ex.getMessage());
            verify(orcamentoRepository, never()).salvar(any());
            verify(osRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("orçamento não encontrado no repositório lança OrcamentoNaoEncontrado")
        void orcamentoNaoEncontrado() {
            OrcamentoId orcamentoId = OrcamentoId.novo();
            OrdemDeServico os = osComOrcamentoAguardando(orcamentoId);
            when(osRepository.buscarPorId(os.id())).thenReturn(Optional.of(os));
            when(orcamentoRepository.buscarPorId(orcamentoId)).thenReturn(Optional.empty());

            AprovarOrcamentoDaOS sut = new AprovarOrcamentoDaOS(osRepository, orcamentoRepository);
            assertThrows(OrcamentoNaoEncontrado.class, () -> sut.executar(os.id()));
        }

        @Test
        @DisplayName("construtor rejeita osRepository nulo")
        void rejeitaOsRepoNulo() {
            assertThrows(NullPointerException.class,
                () -> new AprovarOrcamentoDaOS(null, orcamentoRepository));
        }

        @Test
        @DisplayName("construtor rejeita orcamentoRepository nulo")
        void rejeitaOrcamentoRepoNulo() {
            assertThrows(NullPointerException.class,
                () -> new AprovarOrcamentoDaOS(osRepository, null));
        }
    }

    @Nested
    @DisplayName("RejeitarOrcamentoDaOS")
    class RejeitarTest {

        @Test
        @DisplayName("rejeição feliz registra motivo em ambos os agregados")
        void rejeicaoFeliz() {
            Orcamento orcamento = orcamentoEnviado();
            OrdemDeServico os = osComOrcamentoAguardando(orcamento.id());
            when(osRepository.buscarPorId(os.id())).thenReturn(Optional.of(os));
            when(orcamentoRepository.buscarPorId(orcamento.id())).thenReturn(Optional.of(orcamento));

            RejeitarOrcamentoDaOS sut = new RejeitarOrcamentoDaOS(osRepository, orcamentoRepository);
            sut.executar(os.id(), "Cliente desistiu da compra");

            assertEquals(StatusOrcamento.REJEITADO, orcamento.status());
            assertEquals(StatusOS.REJEITADA, os.status());
            assertEquals(Optional.of("Cliente desistiu da compra"), os.motivoRejeicao());
            verify(orcamentoRepository).salvar(orcamento);
            verify(osRepository).salvar(os);
        }

        @Test
        @DisplayName("motivo inválido propaga IllegalArgumentException do domínio")
        void motivoInvalidoPropagaExcecaoDeDominio() {
            Orcamento orcamento = orcamentoEnviado();
            OrdemDeServico os = osComOrcamentoAguardando(orcamento.id());
            when(osRepository.buscarPorId(os.id())).thenReturn(Optional.of(os));
            when(orcamentoRepository.buscarPorId(orcamento.id())).thenReturn(Optional.of(orcamento));

            RejeitarOrcamentoDaOS sut = new RejeitarOrcamentoDaOS(osRepository, orcamentoRepository);
            assertThrows(IllegalArgumentException.class, () -> sut.executar(os.id(), "AB"));
            verify(orcamentoRepository, never()).salvar(any());
            verify(osRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("OS sem orçamento anexado lança IllegalStateException")
        void osSemOrcamento() {
            OrdemDeServico osSemOrcamento = OrdemDeServico.abrir(ClienteId.novo(), VeiculoId.novo());
            when(osRepository.buscarPorId(osSemOrcamento.id())).thenReturn(Optional.of(osSemOrcamento));

            RejeitarOrcamentoDaOS sut = new RejeitarOrcamentoDaOS(osRepository, orcamentoRepository);
            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> sut.executar(osSemOrcamento.id(), "motivo qualquer")
            );
            assertEquals("OS não possui orçamento anexado", ex.getMessage());
        }
    }
}
