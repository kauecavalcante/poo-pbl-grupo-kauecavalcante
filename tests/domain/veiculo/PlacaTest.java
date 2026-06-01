package domain.veiculo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PlacaTest {

    @Nested
    @DisplayName("Normalização e validação")
    class Normalizacao {

        @ParameterizedTest(name = "{0} normaliza para ABC1234")
        @ValueSource(strings = {
            "ABC1234",
            "abc1234",
            "ABC-1234",
            "abc-1234",
            "  abc-1234  ",
            "ABC 1234"
        })
        @DisplayName("aceita placa antiga em diferentes representações")
        void aceitaPlacaAntiga(String entrada) {
            Placa p = Placa.de(entrada);
            assertEquals("ABC1234", p.valor());
        }

        @ParameterizedTest(name = "{0} normaliza para ABC1D23")
        @ValueSource(strings = {
            "ABC1D23",
            "abc1d23",
            "ABC-1D23",
            "abc-1d23",
            "  ABC-1D23  ",
            "AbC-1d23"
        })
        @DisplayName("aceita placa Mercosul em diferentes representações")
        void aceitaPlacaMercosul(String entrada) {
            Placa p = Placa.de(entrada);
            assertEquals("ABC1D23", p.valor());
        }

        @Test
        @DisplayName("ehAntiga retorna true para padrão AAA0000")
        void identificaAntiga() {
            Placa p = Placa.de("ABC1234");
            assertTrue(p.ehAntiga());
            assertFalse(p.ehMercosul());
        }

        @Test
        @DisplayName("ehMercosul retorna true para padrão AAA0A00")
        void identificaMercosul() {
            Placa p = Placa.de("ABC1D23");
            assertTrue(p.ehMercosul());
            assertFalse(p.ehAntiga());
        }

        @Test
        @DisplayName("rejeita entrada nula")
        void rejeitaNula() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Placa.de(null)
            );
            assertEquals("placa não pode ser nula", ex.getMessage());
        }

        @ParameterizedTest(name = "rejeita comprimento errado: {0}")
        @ValueSource(strings = {"", "ABC12", "ABC12345", "AB-1234", "ABCD1234"})
        @DisplayName("rejeita entradas que não normalizam para 7 caracteres")
        void rejeitaTamanhoErrado(String entrada) {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Placa.de(entrada)
            );
            assertEquals("placa deve ter 7 caracteres", ex.getMessage());
        }

        @ParameterizedTest(name = "rejeita formato: {0}")
        @ValueSource(strings = {
            "1234567",
            "ABCDEFG",
            "12C1234",
            "ABCD234",
            "ABC12D3",
            "AB1C234"
        })
        @DisplayName("rejeita entradas com 7 caracteres mas fora dos padrões válidos")
        void rejeitaFormatoInvalido(String entrada) {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Placa.de(entrada)
            );
            assertEquals("placa inválida", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Formatação e igualdade")
    class FormatacaoEIgualdade {

        @Test
        @DisplayName("formatada insere hífen após os três primeiros caracteres em placa antiga")
        void formatadaPlacaAntiga() {
            assertEquals("ABC-1234", Placa.de("abc1234").formatada());
        }

        @Test
        @DisplayName("formatada insere hífen após os três primeiros caracteres em placa Mercosul")
        void formatadaPlacaMercosul() {
            assertEquals("ABC-1D23", Placa.de("abc1d23").formatada());
        }

        @Test
        @DisplayName("toString delega para formatada")
        void toStringIgualAFormatada() {
            Placa p = Placa.de("ABC1234");
            assertEquals(p.formatada(), p.toString());
        }

        @Test
        @DisplayName("placas com mesmo valor normalizado são iguais")
        void iguaisQuandoMesmoValor() {
            Placa a = Placa.de("abc-1234");
            Placa b = Placa.de("ABC1234");
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("placas com valores diferentes não são iguais")
        void distintasQuandoValoresDiferentes() {
            assertNotEquals(Placa.de("ABC1234"), Placa.de("ABC1D23"));
        }

        @Test
        @DisplayName("equals com null retorna false")
        void equalsComNuloRetornaFalso() {
            Placa p = Placa.de("ABC1234");
            assertFalse(p.equals(null));
        }

        @Test
        @DisplayName("equals com outro tipo retorna false")
        void equalsComOutroTipoRetornaFalso() {
            Placa p = Placa.de("ABC1234");
            assertFalse(p.equals("ABC1234"));
        }

        @Test
        @DisplayName("a instância é igual a si mesma")
        void reflexividade() {
            Placa p = Placa.de("ABC1234");
            assertTrue(p.equals(p));
        }
    }
}
