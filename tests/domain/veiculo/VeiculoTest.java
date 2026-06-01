package domain.veiculo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.cliente.ClienteId;
import java.time.Year;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class VeiculoTest {

    private static final Placa PLACA = Placa.de("ABC1234");
    private static final ClienteId DONO = ClienteId.novo();

    @Nested
    @DisplayName("Criação de novo veículo")
    class Criacao {

        @Test
        @DisplayName("novo gera identidade")
        void novoGeraIdentidade() {
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "Uno", 2010, DONO);
            assertNotNull(v.id());
        }

        @Test
        @DisplayName("novo expõe os dados informados nos acessores")
        void acessoresExpoeDados() {
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "Uno", 2010, DONO);
            assertSame(PLACA, v.placa());
            assertEquals("Fiat", v.marca());
            assertEquals("Uno", v.modelo());
            assertEquals(2010, v.ano());
            assertSame(DONO, v.dono());
        }

        @Test
        @DisplayName("marca é armazenada com trim")
        void marcaArmazenadaComTrim() {
            Veiculo v = Veiculo.novo(PLACA, "   Fiat   ", "Uno", 2010, DONO);
            assertEquals("Fiat", v.marca());
        }

        @Test
        @DisplayName("modelo é armazenado com trim")
        void modeloArmazenadoComTrim() {
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "   Uno   ", 2010, DONO);
            assertEquals("Uno", v.modelo());
        }

        @Test
        @DisplayName("dois veículos novos recebem IDs distintos")
        void novoGeraIdsDistintos() {
            Veiculo a = Veiculo.novo(PLACA, "Fiat", "Uno", 2010, DONO);
            Veiculo b = Veiculo.novo(Placa.de("XYZ4321"), "Fiat", "Uno", 2010, DONO);
            assertEquals(false, a.id().equals(b.id()));
        }
    }

    @Nested
    @DisplayName("Invariantes do veículo")
    class Invariantes {

        @Test
        @DisplayName("rejeita placa nula")
        void rejeitaPlacaNula() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Veiculo.novo(null, "Fiat", "Uno", 2010, DONO)
            );
            assertEquals("placa não pode ser nula", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita dono nulo")
        void rejeitaDonoNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Veiculo.novo(PLACA, "Fiat", "Uno", 2010, null)
            );
            assertEquals("dono do veículo não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita marca nula")
        void rejeitaMarcaNula() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Veiculo.novo(PLACA, null, "Uno", 2010, DONO)
            );
            assertEquals("marca não pode ser vazia", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita marca vazia ou só espaços")
        void rejeitaMarcaVazia() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Veiculo.novo(PLACA, "   ", "Uno", 2010, DONO)
            );
            assertEquals("marca não pode ser vazia", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita marca com menos de 2 caracteres após trim")
        void rejeitaMarcaCurta() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Veiculo.novo(PLACA, "  A  ", "Uno", 2010, DONO)
            );
            assertEquals("marca deve ter ao menos 2 caracteres", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita modelo nulo")
        void rejeitaModeloNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Veiculo.novo(PLACA, "Fiat", null, 2010, DONO)
            );
            assertEquals("modelo não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita modelo vazio ou só espaços")
        void rejeitaModeloVazio() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Veiculo.novo(PLACA, "Fiat", "   ", 2010, DONO)
            );
            assertEquals("modelo não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita modelo com menos de 2 caracteres após trim")
        void rejeitaModeloCurto() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Veiculo.novo(PLACA, "Fiat", "X", 2010, DONO)
            );
            assertEquals("modelo deve ter ao menos 2 caracteres", ex.getMessage());
        }

        @Test
        @DisplayName("aceita ano 1900 como limite inferior")
        void aceitaAnoLimiteInferior() {
            Veiculo v = Veiculo.novo(PLACA, "Ford", "Modelo T", 1900, DONO);
            assertEquals(1900, v.ano());
        }

        @Test
        @DisplayName("rejeita ano 1899")
        void rejeitaAnoAbaixoDoMinimo() {
            int anoMaximo = Year.now().getValue() + 1;
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Veiculo.novo(PLACA, "Ford", "Modelo T", 1899, DONO)
            );
            assertEquals("ano do veículo deve ser entre 1900 e " + anoMaximo, ex.getMessage());
        }

        @Test
        @DisplayName("aceita ano atual mais um como limite superior")
        void aceitaAnoLimiteSuperior() {
            int anoMaximo = Year.now().getValue() + 1;
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "Uno", anoMaximo, DONO);
            assertEquals(anoMaximo, v.ano());
        }

        @Test
        @DisplayName("rejeita ano dois acima do ano atual")
        void rejeitaAnoAcimaDoMaximo() {
            int anoFutura = Year.now().getValue() + 2;
            int anoMaximo = Year.now().getValue() + 1;
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Veiculo.novo(PLACA, "Fiat", "Uno", anoFutura, DONO)
            );
            assertTrue(ex.getMessage().contains(String.valueOf(anoMaximo)));
        }
    }
}
