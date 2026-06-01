package application.ordemservico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.excecao.OrdemDeServicoNaoEncontrada;
import application.excecao.PecaNaoEncontrada;
import domain.cliente.ClienteId;
import domain.orcamento.Orcamento;
import domain.orcamento.OrcamentoId;
import domain.orcamento.OrcamentoRepository;
import domain.orcamento.StatusOrcamento;
import domain.ordemservico.OrdemDeServico;
import domain.ordemservico.OrdemDeServicoId;
import domain.ordemservico.OrdemDeServicoRepository;
import domain.ordemservico.StatusOS;
import domain.peca.Peca;
import domain.peca.PecaId;
import domain.peca.PecaRepository;
import domain.shared.Preco;
import domain.veiculo.VeiculoId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MontarOrcamentoDaOSTest {

    private static final Peca FILTRO = Peca.nova("FLT-1001", "Filtro de óleo", Preco.deReais("50.00"), 10);
    private static final Peca OLEO = Peca.nova("OLEO-5W30", "Óleo 5W30", Preco.deReais("80.00"), 20);

    @Mock private OrdemDeServicoRepository osRepository;
    @Mock private OrcamentoRepository orcamentoRepository;
    @Mock private PecaRepository pecaRepository;

    private OrdemDeServico osPreparada() {
        OrdemDeServico os = OrdemDeServico.abrir(ClienteId.novo(), VeiculoId.novo());
        os.registrarDiagnostico("Suspensão dianteira com folga");
        return os;
    }

    @Test
    @DisplayName("montagem feliz cria orçamento, anexa à OS e retorna o id do orçamento")
    void montagemFeliz() {
        OrdemDeServico os = osPreparada();
        when(osRepository.buscarPorId(os.id())).thenReturn(Optional.of(os));
        when(pecaRepository.buscarPorId(FILTRO.id())).thenReturn(Optional.of(FILTRO));

        MontarOrcamentoDaOS sut = new MontarOrcamentoDaOS(osRepository, orcamentoRepository, pecaRepository);
        OrcamentoId orcamentoId = sut.executar(
            os.id(),
            List.of(new ItemDePecaInput(FILTRO.id(), 2)),
            List.of(new ItemDeMaoDeObraInput("Troca de filtro", Preco.deReais("100.00"), 1))
        );

        assertNotNull(orcamentoId);
        assertEquals(Optional.of(orcamentoId), os.orcamentoId());
        assertEquals(StatusOS.AGUARDANDO_APROVACAO, os.status());
    }

    @Test
    @DisplayName("preço do item de peça vem do catálogo, não do input")
    void precoDoItemDePecaVemDoCatalogo() {
        OrdemDeServico os = osPreparada();
        when(osRepository.buscarPorId(os.id())).thenReturn(Optional.of(os));
        when(pecaRepository.buscarPorId(FILTRO.id())).thenReturn(Optional.of(FILTRO));

        MontarOrcamentoDaOS sut = new MontarOrcamentoDaOS(osRepository, orcamentoRepository, pecaRepository);
        sut.executar(
            os.id(),
            List.of(new ItemDePecaInput(FILTRO.id(), 3)),
            List.of()
        );

        ArgumentCaptor<Orcamento> captor = ArgumentCaptor.forClass(Orcamento.class);
        verify(orcamentoRepository).salvar(captor.capture());
        Orcamento salvo = captor.getValue();
        // 50.00 (do catálogo) × 3 = 150.00
        assertEquals(Preco.deReais("150.00"), salvo.total());
        assertEquals("Filtro de óleo", salvo.itens().get(0).descricao());
    }

    @Test
    @DisplayName("orçamento misto soma peças e mão de obra corretamente")
    void orcamentoMisturadoSomaCorretamente() {
        OrdemDeServico os = osPreparada();
        when(osRepository.buscarPorId(os.id())).thenReturn(Optional.of(os));
        when(pecaRepository.buscarPorId(FILTRO.id())).thenReturn(Optional.of(FILTRO));
        when(pecaRepository.buscarPorId(OLEO.id())).thenReturn(Optional.of(OLEO));

        MontarOrcamentoDaOS sut = new MontarOrcamentoDaOS(osRepository, orcamentoRepository, pecaRepository);
        sut.executar(
            os.id(),
            List.of(
                new ItemDePecaInput(FILTRO.id(), 2),
                new ItemDePecaInput(OLEO.id(), 4)
            ),
            List.of(
                new ItemDeMaoDeObraInput("Troca de óleo", Preco.deReais("90.00"), 1)
            )
        );

        ArgumentCaptor<Orcamento> captor = ArgumentCaptor.forClass(Orcamento.class);
        verify(orcamentoRepository).salvar(captor.capture());
        // 50×2 + 80×4 + 90×1 = 100 + 320 + 90 = 510
        assertEquals(Preco.deReais("510.00"), captor.getValue().total());
        assertEquals(3, captor.getValue().quantidadeDeItens());
    }

    @Test
    @DisplayName("orçamento com itens é enviado antes de ser salvo")
    void orcamentoComItensEhEnviado() {
        OrdemDeServico os = osPreparada();
        when(osRepository.buscarPorId(os.id())).thenReturn(Optional.of(os));
        when(pecaRepository.buscarPorId(FILTRO.id())).thenReturn(Optional.of(FILTRO));

        MontarOrcamentoDaOS sut = new MontarOrcamentoDaOS(osRepository, orcamentoRepository, pecaRepository);
        sut.executar(os.id(), List.of(new ItemDePecaInput(FILTRO.id(), 1)), List.of());

        ArgumentCaptor<Orcamento> captor = ArgumentCaptor.forClass(Orcamento.class);
        verify(orcamentoRepository).salvar(captor.capture());
        assertEquals(StatusOrcamento.ENVIADO, captor.getValue().status());
    }

    @Test
    @DisplayName("orçamento é salvo antes da OS")
    void ordemDeSalvar() {
        OrdemDeServico os = osPreparada();
        when(osRepository.buscarPorId(os.id())).thenReturn(Optional.of(os));
        when(pecaRepository.buscarPorId(FILTRO.id())).thenReturn(Optional.of(FILTRO));

        MontarOrcamentoDaOS sut = new MontarOrcamentoDaOS(osRepository, orcamentoRepository, pecaRepository);
        sut.executar(os.id(), List.of(new ItemDePecaInput(FILTRO.id(), 1)), List.of());

        InOrder ordem = inOrder(orcamentoRepository, osRepository);
        ordem.verify(orcamentoRepository).salvar(any(Orcamento.class));
        ordem.verify(osRepository).salvar(any(OrdemDeServico.class));
    }

    @Test
    @DisplayName("OS não encontrada lança OrdemDeServicoNaoEncontrada sem salvar nada")
    void osNaoEncontrada() {
        OrdemDeServicoId id = OrdemDeServicoId.novo();
        when(osRepository.buscarPorId(id)).thenReturn(Optional.empty());

        MontarOrcamentoDaOS sut = new MontarOrcamentoDaOS(osRepository, orcamentoRepository, pecaRepository);
        assertThrows(
            OrdemDeServicoNaoEncontrada.class,
            () -> sut.executar(id, List.of(), List.of())
        );
        verify(orcamentoRepository, never()).salvar(any());
        verify(osRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("peça não encontrada lança PecaNaoEncontrada sem salvar nada")
    void pecaNaoEncontrada() {
        OrdemDeServico os = osPreparada();
        PecaId pecaInexistente = PecaId.novo();
        when(osRepository.buscarPorId(os.id())).thenReturn(Optional.of(os));
        when(pecaRepository.buscarPorId(pecaInexistente)).thenReturn(Optional.empty());

        MontarOrcamentoDaOS sut = new MontarOrcamentoDaOS(osRepository, orcamentoRepository, pecaRepository);
        assertThrows(
            PecaNaoEncontrada.class,
            () -> sut.executar(os.id(), List.of(new ItemDePecaInput(pecaInexistente, 1)), List.of())
        );
        verify(orcamentoRepository, never()).salvar(any());
        verify(osRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("lista de peças nula lança IllegalArgumentException")
    void listaDePecasNula() {
        MontarOrcamentoDaOS sut = new MontarOrcamentoDaOS(osRepository, orcamentoRepository, pecaRepository);
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> sut.executar(OrdemDeServicoId.novo(), null, List.of())
        );
        assertEquals("listas de itens não podem ser nulas", ex.getMessage());
    }

    @Test
    @DisplayName("lista de mão de obra nula lança IllegalArgumentException")
    void listaDeMaoDeObraNula() {
        MontarOrcamentoDaOS sut = new MontarOrcamentoDaOS(osRepository, orcamentoRepository, pecaRepository);
        assertThrows(
            IllegalArgumentException.class,
            () -> sut.executar(OrdemDeServicoId.novo(), List.of(), null)
        );
    }

    @Test
    @DisplayName("listas vazias são aceitas — orçamento vazio em RASCUNHO é salvo")
    void listasVaziasSaoAceitas() {
        OrdemDeServico os = osPreparada();
        when(osRepository.buscarPorId(os.id())).thenReturn(Optional.of(os));

        MontarOrcamentoDaOS sut = new MontarOrcamentoDaOS(osRepository, orcamentoRepository, pecaRepository);
        OrcamentoId id = sut.executar(os.id(), List.of(), List.of());

        assertNotNull(id);
        ArgumentCaptor<Orcamento> captor = ArgumentCaptor.forClass(Orcamento.class);
        verify(orcamentoRepository).salvar(captor.capture());
        assertTrue(captor.getValue().ehVazio());
        assertEquals(StatusOrcamento.RASCUNHO, captor.getValue().status());
    }

    @Test
    @DisplayName("construtor rejeita osRepository nulo")
    void rejeitaOsRepoNulo() {
        assertThrows(NullPointerException.class,
            () -> new MontarOrcamentoDaOS(null, orcamentoRepository, pecaRepository));
    }

    @Test
    @DisplayName("construtor rejeita orcamentoRepository nulo")
    void rejeitaOrcamentoRepoNulo() {
        assertThrows(NullPointerException.class,
            () -> new MontarOrcamentoDaOS(osRepository, null, pecaRepository));
    }

    @Test
    @DisplayName("construtor rejeita pecaRepository nulo")
    void rejeitaPecaRepoNulo() {
        assertThrows(NullPointerException.class,
            () -> new MontarOrcamentoDaOS(osRepository, orcamentoRepository, null));
    }
}
