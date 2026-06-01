package domain.veiculo;

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

class VeiculoIdTest {

    @Nested
    @DisplayName("Geração e reconstituição")
    class Criacao {

        @Test
        @DisplayName("novo gera um UUID não nulo")
        void novoGeraUuidNaoNulo() {
            VeiculoId id = VeiculoId.novo();
            assertNotNull(id.valor());
        }

        @Test
        @DisplayName("novo gera identificadores distintos em chamadas sucessivas")
        void novoGeraDistintos() {
            assertNotEquals(VeiculoId.novo(), VeiculoId.novo());
        }

        @Test
        @DisplayName("de a partir de UUID preserva o valor original")
        void deAPartirDeUuid() {
            UUID uuid = UUID.randomUUID();
            VeiculoId id = VeiculoId.de(uuid);
            assertEquals(uuid, id.valor());
        }

        @Test
        @DisplayName("de a partir de string canônica parseia o UUID")
        void deAPartirDeStringCanonica() {
            String canonico = "550e8400-e29b-41d4-a716-446655440000";
            VeiculoId id = VeiculoId.de(canonico);
            assertEquals(UUID.fromString(canonico), id.valor());
        }

        @Test
        @DisplayName("rejeita UUID nulo")
        void rejeitaUuidNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> VeiculoId.de((UUID) null)
            );
            assertEquals("id do veículo não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita string nula")
        void rejeitaStringNula() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> VeiculoId.de((String) null)
            );
            assertEquals("id do veículo não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita string mal-formada")
        void rejeitaStringMalFormada() {
            assertThrows(
                IllegalArgumentException.class,
                () -> VeiculoId.de("não-é-um-uuid")
            );
        }
    }

    @Nested
    @DisplayName("Igualdade")
    class Igualdade {

        @Test
        @DisplayName("dois VeiculoId com mesmo UUID são iguais")
        void iguaisQuandoMesmoUuid() {
            UUID uuid = UUID.randomUUID();
            assertEquals(VeiculoId.de(uuid), VeiculoId.de(uuid));
            assertEquals(VeiculoId.de(uuid).hashCode(), VeiculoId.de(uuid).hashCode());
        }

        @Test
        @DisplayName("VeiculoId com UUIDs diferentes não são iguais")
        void distintosQuandoUuidsDiferentes() {
            assertNotEquals(VeiculoId.novo(), VeiculoId.novo());
        }

        @Test
        @DisplayName("equals com null retorna false")
        void equalsComNuloRetornaFalso() {
            assertFalse(VeiculoId.novo().equals(null));
        }

        @Test
        @DisplayName("equals com outro tipo retorna false")
        void equalsComOutroTipoRetornaFalso() {
            VeiculoId id = VeiculoId.novo();
            assertFalse(id.equals(id.valor()));
        }

        @Test
        @DisplayName("a instância é igual a si mesma")
        void reflexividade() {
            VeiculoId id = VeiculoId.novo();
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
            VeiculoId id = VeiculoId.de(canonico);
            assertEquals(canonico, id.toString());
        }
    }
}
