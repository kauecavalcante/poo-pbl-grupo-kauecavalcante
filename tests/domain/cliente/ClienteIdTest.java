package domain.cliente;

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

class ClienteIdTest {

    @Nested
    @DisplayName("Geração e reconstituição")
    class Criacao {

        @Test
        @DisplayName("novo gera um UUID não nulo")
        void novoGeraUuidNaoNulo() {
            ClienteId id = ClienteId.novo();
            assertNotNull(id.valor());
        }

        @Test
        @DisplayName("novo gera identificadores distintos em chamadas sucessivas")
        void novoGeraDistintos() {
            ClienteId a = ClienteId.novo();
            ClienteId b = ClienteId.novo();
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("de a partir de UUID preserva o valor original")
        void deAPartirDeUuid() {
            UUID uuid = UUID.randomUUID();
            ClienteId id = ClienteId.de(uuid);
            assertEquals(uuid, id.valor());
        }

        @Test
        @DisplayName("de a partir de string canônica parseia o UUID")
        void deAPartirDeStringCanonica() {
            String canonico = "550e8400-e29b-41d4-a716-446655440000";
            ClienteId id = ClienteId.de(canonico);
            assertEquals(UUID.fromString(canonico), id.valor());
        }

        @Test
        @DisplayName("rejeita UUID nulo")
        void rejeitaUuidNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ClienteId.de((UUID) null)
            );
            assertEquals("id do cliente não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita string nula")
        void rejeitaStringNula() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ClienteId.de((String) null)
            );
            assertEquals("id do cliente não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita string mal-formada")
        void rejeitaStringMalFormada() {
            assertThrows(
                IllegalArgumentException.class,
                () -> ClienteId.de("não-é-um-uuid")
            );
        }
    }

    @Nested
    @DisplayName("Igualdade")
    class Igualdade {

        @Test
        @DisplayName("dois ClienteId com mesmo UUID são iguais")
        void iguaisQuandoMesmoUuid() {
            UUID uuid = UUID.randomUUID();
            ClienteId a = ClienteId.de(uuid);
            ClienteId b = ClienteId.de(uuid);
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("ClienteId com UUIDs diferentes não são iguais")
        void distintosQuandoUuidsDiferentes() {
            ClienteId a = ClienteId.de(UUID.randomUUID());
            ClienteId b = ClienteId.de(UUID.randomUUID());
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("equals com null retorna false")
        void equalsComNuloRetornaFalso() {
            ClienteId id = ClienteId.novo();
            assertFalse(id.equals(null));
        }

        @Test
        @DisplayName("equals com outro tipo retorna false")
        void equalsComOutroTipoRetornaFalso() {
            ClienteId id = ClienteId.novo();
            assertFalse(id.equals(id.valor()));
        }

        @Test
        @DisplayName("a instância é igual a si mesma")
        void reflexividade() {
            ClienteId id = ClienteId.novo();
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
            ClienteId id = ClienteId.de(canonico);
            assertEquals(canonico, id.toString());
        }
    }
}
