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

class OrcamentoIdTest {

    @Nested
    @DisplayName("Geração e reconstituição")
    class Criacao {

        @Test
        @DisplayName("novo gera UUID não nulo")
        void novoGeraUuidNaoNulo() {
            assertNotNull(OrcamentoId.novo().valor());
        }

        @Test
        @DisplayName("novo gera identificadores distintos em chamadas sucessivas")
        void novoGeraDistintos() {
            assertNotEquals(OrcamentoId.novo(), OrcamentoId.novo());
        }

        @Test
        @DisplayName("de a partir de UUID preserva o valor")
        void deAPartirDeUuid() {
            UUID uuid = UUID.randomUUID();
            assertEquals(uuid, OrcamentoId.de(uuid).valor());
        }

        @Test
        @DisplayName("de a partir de string canônica parseia o UUID")
        void deAPartirDeStringCanonica() {
            String canonico = "550e8400-e29b-41d4-a716-446655440000";
            assertEquals(UUID.fromString(canonico), OrcamentoId.de(canonico).valor());
        }

        @Test
        @DisplayName("rejeita UUID nulo")
        void rejeitaUuidNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> OrcamentoId.de((UUID) null)
            );
            assertEquals("id do orçamento não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita string nula")
        void rejeitaStringNula() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> OrcamentoId.de((String) null)
            );
            assertEquals("id do orçamento não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita string mal-formada")
        void rejeitaStringMalFormada() {
            assertThrows(IllegalArgumentException.class, () -> OrcamentoId.de("não-é-um-uuid"));
        }
    }

    @Nested
    @DisplayName("Igualdade")
    class Igualdade {

        @Test
        @DisplayName("dois OrcamentoId com mesmo UUID são iguais")
        void iguaisQuandoMesmoUuid() {
            UUID uuid = UUID.randomUUID();
            assertEquals(OrcamentoId.de(uuid), OrcamentoId.de(uuid));
            assertEquals(OrcamentoId.de(uuid).hashCode(), OrcamentoId.de(uuid).hashCode());
        }

        @Test
        @DisplayName("OrcamentoId com UUIDs diferentes não são iguais")
        void distintosQuandoUuidsDiferentes() {
            assertNotEquals(OrcamentoId.novo(), OrcamentoId.novo());
        }

        @Test
        @DisplayName("equals com null retorna false")
        void equalsComNuloRetornaFalso() {
            assertFalse(OrcamentoId.novo().equals(null));
        }

        @Test
        @DisplayName("equals com outro tipo retorna false")
        void equalsComOutroTipoRetornaFalso() {
            OrcamentoId id = OrcamentoId.novo();
            assertFalse(id.equals(id.valor()));
        }

        @Test
        @DisplayName("a instância é igual a si mesma")
        void reflexividade() {
            OrcamentoId id = OrcamentoId.novo();
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
            assertEquals(canonico, OrcamentoId.de(canonico).toString());
        }
    }
}
