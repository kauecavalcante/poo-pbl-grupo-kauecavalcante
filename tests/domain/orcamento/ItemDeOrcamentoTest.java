package domain.orcamento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.peca.PecaId;
import domain.shared.Preco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ItemDeOrcamentoTest {

    private static final PecaId PECA_ID = PecaId.novo();
    private static final Preco PRECO = Preco.deReais("100.00");

    @Nested
    @DisplayName("Criação de item de peça")
    class CriacaoDePeca {

        @Test
        @DisplayName("dePeca cria item com tipo PECA e pecaId presente")
        void dePecaCriaItem() {
            ItemDeOrcamento item = ItemDeOrcamento.dePeca(PECA_ID, "Filtro de óleo", PRECO, 2);
            assertEquals(TipoItem.PECA, item.tipo());
            assertTrue(item.pecaId().isPresent());
            assertEquals(PECA_ID, item.pecaId().get());
            assertTrue(item.ehPeca());
            assertFalse(item.ehMaoDeObra());
        }

        @Test
        @DisplayName("dePeca gera identidade automaticamente")
        void dePecaGeraIdentidade() {
            ItemDeOrcamento item = ItemDeOrcamento.dePeca(PECA_ID, "Filtro de óleo", PRECO, 2);
            assertNotNull(item.id());
        }

        @Test
        @DisplayName("dePeca expõe descrição, preço e quantidade")
        void dePecaExpoeDados() {
            ItemDeOrcamento item = ItemDeOrcamento.dePeca(PECA_ID, "Filtro de óleo", PRECO, 2);
            assertEquals("Filtro de óleo", item.descricao());
            assertSame(PRECO, item.precoUnitario());
            assertEquals(2, item.quantidade());
        }

        @Test
        @DisplayName("dePeca aplica trim na descrição")
        void dePecaAplicaTrim() {
            ItemDeOrcamento item = ItemDeOrcamento.dePeca(PECA_ID, "   Filtro de óleo   ", PRECO, 2);
            assertEquals("Filtro de óleo", item.descricao());
        }

        @Test
        @DisplayName("dePeca rejeita pecaId nulo")
        void dePecaRejeitaPecaIdNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ItemDeOrcamento.dePeca(null, "Filtro de óleo", PRECO, 2)
            );
            assertEquals("item de peça exige pecaId", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Criação de item de mão de obra")
    class CriacaoDeMaoDeObra {

        @Test
        @DisplayName("deMaoDeObra cria item com tipo MAO_DE_OBRA e sem pecaId")
        void deMaoDeObraCriaItem() {
            ItemDeOrcamento item = ItemDeOrcamento.deMaoDeObra("Troca de filtro", PRECO, 1);
            assertEquals(TipoItem.MAO_DE_OBRA, item.tipo());
            assertFalse(item.pecaId().isPresent());
            assertTrue(item.ehMaoDeObra());
            assertFalse(item.ehPeca());
        }

        @Test
        @DisplayName("deMaoDeObra gera identidade automaticamente")
        void deMaoDeObraGeraIdentidade() {
            ItemDeOrcamento item = ItemDeOrcamento.deMaoDeObra("Troca de filtro", PRECO, 1);
            assertNotNull(item.id());
        }

        @Test
        @DisplayName("dois itens criados sucessivamente recebem IDs distintos")
        void itensSucessivosTemIdsDistintos() {
            ItemDeOrcamento a = ItemDeOrcamento.deMaoDeObra("Troca de filtro", PRECO, 1);
            ItemDeOrcamento b = ItemDeOrcamento.deMaoDeObra("Troca de filtro", PRECO, 1);
            assertEquals(false, a.id().equals(b.id()));
        }
    }

    @Nested
    @DisplayName("Invariantes do item")
    class Invariantes {

        @Test
        @DisplayName("rejeita descrição nula")
        void rejeitaDescricaoNula() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ItemDeOrcamento.deMaoDeObra(null, PRECO, 1)
            );
            assertEquals("descrição do item não pode ser vazia", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita descrição vazia ou só espaços")
        void rejeitaDescricaoVazia() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ItemDeOrcamento.deMaoDeObra("   ", PRECO, 1)
            );
            assertEquals("descrição do item não pode ser vazia", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita descrição com menos de 3 caracteres após trim")
        void rejeitaDescricaoCurta() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ItemDeOrcamento.deMaoDeObra("AB", PRECO, 1)
            );
            assertEquals("descrição do item deve ter ao menos 3 caracteres", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita preço unitário nulo")
        void rejeitaPrecoNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ItemDeOrcamento.deMaoDeObra("Troca de filtro", null, 1)
            );
            assertEquals("preço unitário não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita quantidade zero")
        void rejeitaQuantidadeZero() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ItemDeOrcamento.deMaoDeObra("Troca de filtro", PRECO, 0)
            );
            assertEquals("quantidade do item deve ser positiva", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita quantidade negativa")
        void rejeitaQuantidadeNegativa() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ItemDeOrcamento.dePeca(PECA_ID, "Filtro de óleo", PRECO, -3)
            );
            assertEquals("quantidade do item deve ser positiva", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Subtotal e atualização de quantidade")
    class CalculoEAtualizacao {

        @Test
        @DisplayName("subtotal de item de peça é preço unitário multiplicado pela quantidade")
        void subtotalPeca() {
            ItemDeOrcamento item = ItemDeOrcamento.dePeca(PECA_ID, "Filtro de óleo", Preco.deReais("25.00"), 4);
            assertEquals(Preco.deReais("100.00"), item.subtotal());
        }

        @Test
        @DisplayName("subtotal de item de mão de obra também escala pela quantidade")
        void subtotalMaoDeObra() {
            ItemDeOrcamento item = ItemDeOrcamento.deMaoDeObra("Troca de filtro", Preco.deReais("80.00"), 2);
            assertEquals(Preco.deReais("160.00"), item.subtotal());
        }

        @Test
        @DisplayName("atualizarQuantidade reflete na quantidade e no subtotal")
        void atualizarQuantidadeRefleteNoSubtotal() {
            ItemDeOrcamento item = ItemDeOrcamento.deMaoDeObra("Troca de filtro", Preco.deReais("80.00"), 2);
            item.atualizarQuantidade(5);
            assertEquals(5, item.quantidade());
            assertEquals(Preco.deReais("400.00"), item.subtotal());
        }

        @Test
        @DisplayName("atualizarQuantidade preserva a identidade do item")
        void atualizarQuantidadePreservaIdentidade() {
            ItemDeOrcamento item = ItemDeOrcamento.deMaoDeObra("Troca de filtro", PRECO, 2);
            ItemDeOrcamentoId idAntes = item.id();
            item.atualizarQuantidade(7);
            assertSame(idAntes, item.id());
        }

        @Test
        @DisplayName("atualizarQuantidade rejeita zero")
        void atualizarQuantidadeRejeitaZero() {
            ItemDeOrcamento item = ItemDeOrcamento.deMaoDeObra("Troca de filtro", PRECO, 2);
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> item.atualizarQuantidade(0)
            );
            assertEquals("quantidade do item deve ser positiva", ex.getMessage());
        }

        @Test
        @DisplayName("atualizarQuantidade rejeita valor negativo")
        void atualizarQuantidadeRejeitaNegativo() {
            ItemDeOrcamento item = ItemDeOrcamento.deMaoDeObra("Troca de filtro", PRECO, 2);
            assertThrows(IllegalArgumentException.class, () -> item.atualizarQuantidade(-1));
        }

        @Test
        @DisplayName("atualização falha não modifica a quantidade")
        void atualizacaoFalhaNaoModificaEstado() {
            ItemDeOrcamento item = ItemDeOrcamento.deMaoDeObra("Troca de filtro", PRECO, 2);
            assertThrows(IllegalArgumentException.class, () -> item.atualizarQuantidade(0));
            assertThrows(IllegalArgumentException.class, () -> item.atualizarQuantidade(-3));
            assertEquals(2, item.quantidade());
        }
    }
}
