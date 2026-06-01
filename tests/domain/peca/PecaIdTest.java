package domain.peca;

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

class PecaIdTest {

    @Nested
    @DisplayName("Geração e reconstituição")
    class Criacao {

        @Test
        @DisplayName("novo gera UUID não nulo")
        void novoGeraUuidNaoNulo() {
            assertNotNull(PecaId.novo().valor());
        }

        @Test
        @DisplayName("novo gera identificadores distintos em chamadas sucessivas")
        void novoGeraDistintos() {
            assertNotEquals(PecaId.novo(), PecaId.novo());
        }

        @Test
        @DisplayName("de a partir de UUID preserva o valor")
        void deAPartirDeUuid() {
            UUID uuid = UUID.randomUUID();
            assertEquals(uuid, PecaId.de(uuid).valor());
        }

        @Test
        @DisplayName("de a partir de string canônica parseia o UUID")
        void deAPartirDeStringCanonica() {
            String canonico = "550e8400-e29b-41d4-a716-446655440000";
            assertEquals(UUID.fromString(canonico), PecaId.de(canonico).valor());
        }

        @Test
        @DisplayName("rejeita UUID nulo")
        void rejeitaUuidNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> PecaId.de((UUID) null)
            );
            assertEquals("id da peça não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita string nula")
        void rejeitaStringNula() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> PecaId.de((String) null)
            );
            assertEquals("id da peça não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita string mal-formada")
        void rejeitaStringMalFormada() {
            assertThrows(IllegalArgumentException.class, () -> PecaId.de("não-é-um-uuid"));
        }
    }

    @Nested
    @DisplayName("Igualdade")
    class Igualdade {

        @Test
        @DisplayName("dois PecaId com mesmo UUID são iguais")
        void iguaisQuandoMesmoUuid() {
            UUID uuid = UUID.randomUUID();
            assertEquals(PecaId.de(uuid), PecaId.de(uuid));
            assertEquals(PecaId.de(uuid).hashCode(), PecaId.de(uuid).hashCode());
        }

        @Test
        @DisplayName("PecaId com UUIDs diferentes não são iguais")
        void distintosQuandoUuidsDiferentes() {
            assertNotEquals(PecaId.novo(), PecaId.novo());
        }

        @Test
        @DisplayName("equals com null retorna false")
        void equalsComNuloRetornaFalso() {
            assertFalse(PecaId.novo().equals(null));
        }

        @Test
        @DisplayName("equals com outro tipo retorna false")
        void equalsComOutroTipoRetornaFalso() {
            PecaId id = PecaId.novo();
            assertFalse(id.equals(id.valor()));
        }

        @Test
        @DisplayName("a instância é igual a si mesma")
        void reflexividade() {
            PecaId id = PecaId.novo();
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
            assertEquals(canonico, PecaId.de(canonico).toString());
        }
    }
}
