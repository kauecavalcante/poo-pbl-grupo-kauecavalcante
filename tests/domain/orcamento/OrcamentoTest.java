package domain.orcamento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.shared.Preco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OrcamentoTest {

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
}
