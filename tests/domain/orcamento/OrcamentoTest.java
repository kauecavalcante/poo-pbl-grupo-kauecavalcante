package domain.orcamento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.peca.PecaId;
import domain.shared.Preco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OrcamentoTest {

    private static final PecaId PECA_ID = PecaId.novo();

    @Nested
    @DisplayName("Criação de orçamento vazio")
    class Criacao {

        @Test
        @DisplayName("novo gera identidade")
        void novoGeraIdentidade() {
            Orcamento o = Orcamento.novo();
            assertNotNull(o.id());
        }

        @Test
        @DisplayName("novo começa sem itens")
        void novoSemItens() {
            Orcamento o = Orcamento.novo();
            assertEquals(0, o.quantidadeDeItens());
            assertTrue(o.ehVazio());
        }

        @Test
        @DisplayName("total de orçamento vazio é Preco.zero")
        void totalVazioEhZero() {
            Orcamento o = Orcamento.novo();
            assertEquals(Preco.zero(), o.total());
        }

        @Test
        @DisplayName("dois orçamentos novos recebem IDs distintos")
        void novoGeraIdsDistintos() {
            assertEquals(false, Orcamento.novo().id().equals(Orcamento.novo().id()));
        }
    }

    @Nested
    @DisplayName("Inserção de itens")
    class Adicao {

        @Test
        @DisplayName("adicionarItemDePeca retorna ID não nulo do item criado")
        void adicionarItemDePecaRetornaId() {
            Orcamento o = Orcamento.novo();
            ItemDeOrcamentoId itemId = o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 2);
            assertNotNull(itemId);
        }

        @Test
        @DisplayName("adicionarItemDePeca incrementa quantidadeDeItens e remove ehVazio")
        void adicionarItemDePecaAtualizaContagem() {
            Orcamento o = Orcamento.novo();
            o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 2);
            assertEquals(1, o.quantidadeDeItens());
            assertFalse(o.ehVazio());
        }

        @Test
        @DisplayName("adicionarItemDePeca atualiza o total")
        void adicionarItemDePecaAtualizaTotal() {
            Orcamento o = Orcamento.novo();
            o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 2);
            assertEquals(Preco.deReais("100.00"), o.total());
        }

        @Test
        @DisplayName("adicionarItemDeMaoDeObra retorna ID e atualiza total")
        void adicionarItemDeMaoDeObra() {
            Orcamento o = Orcamento.novo();
            ItemDeOrcamentoId itemId = o.adicionarItemDeMaoDeObra("Troca de filtro", Preco.deReais("80.00"), 1);
            assertNotNull(itemId);
            assertEquals(Preco.deReais("80.00"), o.total());
        }

        @Test
        @DisplayName("misturar peças e mão de obra soma corretamente os subtotais")
        void misturarTiposSomaSubtotais() {
            Orcamento o = Orcamento.novo();
            o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 2);
            o.adicionarItemDeMaoDeObra("Troca de filtro", Preco.deReais("80.00"), 1);
            o.adicionarItemDePeca(PecaId.novo(), "Óleo", Preco.deReais("30.00"), 4);
            // 100 + 80 + 120 = 300
            assertEquals(Preco.deReais("300.00"), o.total());
            assertEquals(3, o.quantidadeDeItens());
        }

        @Test
        @DisplayName("cada item adicionado recebe um ID único")
        void cadaItemRecebeIdUnico() {
            Orcamento o = Orcamento.novo();
            ItemDeOrcamentoId a = o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 1);
            ItemDeOrcamentoId b = o.adicionarItemDePeca(PECA_ID, "Filtro", Preco.deReais("50.00"), 1);
            assertEquals(false, a.equals(b));
        }

        @Test
        @DisplayName("adicionarItemDePeca propaga validação do item (rejeita pecaId nulo)")
        void adicionarItemDePecaPropagaValidacao() {
            Orcamento o = Orcamento.novo();
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> o.adicionarItemDePeca(null, "Filtro", Preco.deReais("50.00"), 1)
            );
            assertEquals("item de peça exige pecaId", ex.getMessage());
        }

        @Test
        @DisplayName("adicionarItemDeMaoDeObra propaga validação do item (rejeita quantidade zero)")
        void adicionarItemDeMaoDeObraPropagaValidacao() {
            Orcamento o = Orcamento.novo();
            assertThrows(
                IllegalArgumentException.class,
                () -> o.adicionarItemDeMaoDeObra("Troca", Preco.deReais("80.00"), 0)
            );
        }
    }
}
