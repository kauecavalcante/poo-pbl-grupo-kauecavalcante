package infrastructure.sqlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.orcamento.Aprovado;
import domain.orcamento.ItemDeOrcamento;
import domain.orcamento.ItemDeOrcamentoId;
import domain.orcamento.Orcamento;
import domain.orcamento.OrcamentoId;
import domain.orcamento.Rascunho;
import domain.orcamento.Rejeitado;
import domain.orcamento.StatusOrcamento;
import domain.peca.PecaId;
import domain.shared.Preco;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrcamentoSqliteRepositoryTest {

    private OficinaDatabase database;
    private OrcamentoSqliteRepository repo;

    @BeforeEach
    void setup() {
        database = new OficinaDatabase("jdbc:sqlite::memory:");
        repo = new OrcamentoSqliteRepository(database);
    }

    @AfterEach
    void teardown() {
        database.close();
    }

    @Test
    @DisplayName("salvar e buscar orçamento vazio em rascunho preserva estado")
    void roundtripVazioEmRascunho() {
        Orcamento o = Orcamento.novo();
        repo.salvar(o);

        Orcamento recuperado = repo.buscarPorId(o.id()).orElseThrow();
        assertEquals(o.id(), recuperado.id());
        assertEquals(StatusOrcamento.RASCUNHO, recuperado.status());
        assertTrue(recuperado.ehVazio());
    }

    @Test
    @DisplayName("salvar e buscar orçamento com itens preserva todos eles")
    void roundtripComItens() {
        PecaId pecaId = PecaId.novo();
        Orcamento o = Orcamento.novo();
        o.adicionarItemDePeca(pecaId, "Filtro de óleo", Preco.deReais("50.00"), 2);
        o.adicionarItemDeMaoDeObra("Troca de óleo", Preco.deReais("80.00"), 1);
        o.enviar();
        repo.salvar(o);

        Orcamento recuperado = repo.buscarPorId(o.id()).orElseThrow();
        assertEquals(StatusOrcamento.ENVIADO, recuperado.status());
        assertEquals(2, recuperado.quantidadeDeItens());
        // 50×2 + 80 = 180
        assertEquals(Preco.deReais("180.00"), recuperado.total());

        ItemDeOrcamento itemPeca = recuperado.itens().stream()
            .filter(ItemDeOrcamento::ehPeca).findFirst().orElseThrow();
        assertEquals(Optional.of(pecaId), itemPeca.pecaId());
        assertEquals("Filtro de óleo", itemPeca.descricao());
        assertEquals(Preco.deReais("50.00"), itemPeca.precoUnitario());
        assertEquals(2, itemPeca.quantidade());

        ItemDeOrcamento itemMao = recuperado.itens().stream()
            .filter(ItemDeOrcamento::ehMaoDeObra).findFirst().orElseThrow();
        assertTrue(itemMao.pecaId().isEmpty());
        assertEquals("Troca de óleo", itemMao.descricao());
    }

    @Test
    @DisplayName("atualizar substitui completamente a lista de itens (delete-and-recreate)")
    void atualizarItens() {
        Orcamento o = Orcamento.novo();
        ItemDeOrcamentoId itemId1 = o.adicionarItemDeMaoDeObra("Troca 1", Preco.deReais("10.00"), 1);
        ItemDeOrcamentoId itemId2 = o.adicionarItemDeMaoDeObra("Troca 2", Preco.deReais("20.00"), 1);
        repo.salvar(o);

        o.removerItem(itemId1);
        repo.salvar(o);

        Orcamento recuperado = repo.buscarPorId(o.id()).orElseThrow();
        assertEquals(1, recuperado.quantidadeDeItens());
        assertEquals(itemId2, recuperado.itens().get(0).id());
    }

    @Test
    @DisplayName("buscar por id inexistente retorna Optional.empty")
    void buscarPorIdInexistente() {
        assertEquals(Optional.empty(), repo.buscarPorId(OrcamentoId.novo()));
    }

    @Test
    @DisplayName("buscar por id null retorna Optional.empty")
    void buscarPorIdNulo() {
        assertEquals(Optional.empty(), repo.buscarPorId(null));
    }

    @Test
    @DisplayName("orçamento APROVADO é preservado em roundtrip")
    void roundtripAprovado() {
        Orcamento o = Orcamento.novo();
        o.adicionarItemDeMaoDeObra("Troca", Preco.deReais("100.00"), 1);
        o.enviar();
        o.aprovar();
        repo.salvar(o);
        Orcamento recuperado = repo.buscarPorId(o.id()).orElseThrow();
        assertEquals(StatusOrcamento.APROVADO, recuperado.status());
    }

    @Test
    @DisplayName("orçamento REJEITADO preserva o motivo em roundtrip")
    void roundtripRejeitadoPreservaMotivo() {
        Orcamento o = Orcamento.novo();
        o.adicionarItemDeMaoDeObra("Troca", Preco.deReais("100.00"), 1);
        o.enviar();
        o.rejeitar("Cliente desistiu da compra");
        repo.salvar(o);
        Orcamento recuperado = repo.buscarPorId(o.id()).orElseThrow();
        assertEquals(StatusOrcamento.REJEITADO, recuperado.status());
        assertEquals(Optional.of("Cliente desistiu da compra"), recuperado.motivoRejeicao());
    }

    @Test
    @DisplayName("reconstituir cliente do banco devolve orçamento em RASCUNHO sem motivo")
    void reconstituirRascunhoSemMotivo() {
        OrcamentoId id = OrcamentoId.novo();
        Orcamento o = Orcamento.reconstituir(id, List.of(), new Rascunho());
        repo.salvar(o);
        Orcamento recuperado = repo.buscarPorId(id).orElseThrow();
        assertEquals(StatusOrcamento.RASCUNHO, recuperado.status());
        assertEquals(Optional.empty(), recuperado.motivoRejeicao());
    }

    @Test
    @DisplayName("reconstituir orçamento aprovado via reconstituir externo funciona")
    void reconstituirAprovado() {
        OrcamentoId id = OrcamentoId.novo();
        ItemDeOrcamento item = ItemDeOrcamento.deMaoDeObra("Troca", Preco.deReais("80.00"), 1);
        Orcamento o = Orcamento.reconstituir(id, List.of(item), new Aprovado());
        repo.salvar(o);
        Orcamento recuperado = repo.buscarPorId(id).orElseThrow();
        assertEquals(StatusOrcamento.APROVADO, recuperado.status());
    }

    @Test
    @DisplayName("reconstituir REJEITADO com motivo preserva motivo no roundtrip")
    void reconstituirRejeitadoComMotivo() {
        OrcamentoId id = OrcamentoId.novo();
        ItemDeOrcamento item = ItemDeOrcamento.deMaoDeObra("Troca", Preco.deReais("80.00"), 1);
        Orcamento o = Orcamento.reconstituir(id, List.of(item), new Rejeitado("Custou caro"));
        repo.salvar(o);
        Orcamento recuperado = repo.buscarPorId(id).orElseThrow();
        assertEquals(Optional.of("Custou caro"), recuperado.motivoRejeicao());
    }

    @Test
    @DisplayName("salvar(null) lança NullPointerException")
    void salvarNulo() {
        NullPointerException ex = assertThrows(NullPointerException.class, () -> repo.salvar(null));
        assertEquals("orcamento", ex.getMessage());
    }
}
