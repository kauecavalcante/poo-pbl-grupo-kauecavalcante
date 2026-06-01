package domain.orcamento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ItemDeOrcamentoIdTest {

    @Nested
    @DisplayName("Geração e reconstituição")
    class Criacao {

        @Test
        @DisplayName("novo gera UUID não nulo")
        void novoGeraUuidNaoNulo() {
            assertNotNull(ItemDeOrcamentoId.novo().valor());
        }

        @Test
        @DisplayName("novo gera identificadores distintos em chamadas sucessivas")
        void novoGeraDistintos() {
            assertNotEquals(ItemDeOrcamentoId.novo(), ItemDeOrcamentoId.novo());
        }

        @Test
        @DisplayName("de a partir de UUID preserva o valor")
        void deAPartirDeUuid() {
            UUID uuid = UUID.randomUUID();
            assertEquals(uuid, ItemDeOrcamentoId.de(uuid).valor());
        }

        @Test
        @DisplayName("de a partir de string canônica parseia o UUID")
        void deAPartirDeStringCanonica() {
            String canonico = "550e8400-e29b-41d4-a716-446655440000";
            assertEquals(UUID.fromString(canonico), ItemDeOrcamentoId.de(canonico).valor());
        }

        @Test
        @DisplayName("rejeita UUID nulo")
        void rejeitaUuidNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ItemDeOrcamentoId.de((UUID) null)
            );
            assertEquals("id do item de orçamento não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita string nula")
        void rejeitaStringNula() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ItemDeOrcamentoId.de((String) null)
            );
            assertEquals("id do item de orçamento não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita string mal-formada")
        void rejeitaStringMalFormada() {
            assertThrows(IllegalArgumentException.class, () -> ItemDeOrcamentoId.de("não-é-um-uuid"));
        }
    }

    @Nested
    @DisplayName("Igualdade")
    class Igualdade {

        @Test
        @DisplayName("dois ItemDeOrcamentoId com mesmo UUID são iguais")
        void iguaisQuandoMesmoUuid() {
            UUID uuid = UUID.randomUUID();
            assertEquals(ItemDeOrcamentoId.de(uuid), ItemDeOrcamentoId.de(uuid));
            assertEquals(ItemDeOrcamentoId.de(uuid).hashCode(), ItemDeOrcamentoId.de(uuid).hashCode());
        }

        @Test
        @DisplayName("ItemDeOrcamentoId com UUIDs diferentes não são iguais")
        void distintosQuandoUuidsDiferentes() {
            assertNotEquals(ItemDeOrcamentoId.novo(), ItemDeOrcamentoId.novo());
        }

        @Test
        @DisplayName("equals com null retorna false")
        void equalsComNuloRetornaFalso() {
            assertFalse(ItemDeOrcamentoId.novo().equals(null));
        }

        @Test
        @DisplayName("equals com outro tipo retorna false")
        void equalsComOutroTipoRetornaFalso() {
            ItemDeOrcamentoId id = ItemDeOrcamentoId.novo();
            assertFalse(id.equals(id.valor()));
        }

        @Test
        @DisplayName("a instância é igual a si mesma")
        void reflexividade() {
            ItemDeOrcamentoId id = ItemDeOrcamentoId.novo();
            assertTrue(id.equals(id));
        }
    }

    @Nested
    @DisplayName("Representação textual")
    class Formatacao {

        @Test
        @DisplayName("toString retorna a forma canônica do UUID")
        void toStringRetornaCanonico() {
            String canonico = "550e8400-e29b-41d4-a716-446655440000";
            assertEquals(canonico, ItemDeOrcamentoId.de(canonico).toString());
        }
    }
}
