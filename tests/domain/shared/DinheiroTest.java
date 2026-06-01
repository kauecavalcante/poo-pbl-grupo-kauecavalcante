package domain.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DinheiroTest {

    private static final Currency BRL = Currency.getInstance("BRL");
    private static final Currency USD = Currency.getInstance("USD");

    @Nested
    @DisplayName("Construção e factories")
    class Construcao {

        @Test
        @DisplayName("cria Dinheiro com valor e moeda informados")
        void criaComValorEMoeda() {
            Dinheiro d = Dinheiro.de(new BigDecimal("10.00"), USD);
            assertEquals(new BigDecimal("10.00"), d.valor());
            assertEquals(USD, d.moeda());
        }

        @Test
        @DisplayName("deReais a partir de BigDecimal usa moeda BRL")
        void deReaisAPartirDeBigDecimal() {
            Dinheiro d = Dinheiro.deReais(new BigDecimal("99.99"));
            assertEquals(new BigDecimal("99.99"), d.valor());
            assertEquals(BRL, d.moeda());
        }

        @Test
        @DisplayName("deReais a partir de String usa moeda BRL")
        void deReaisAPartirDeString() {
            Dinheiro d = Dinheiro.deReais("42.00");
            assertEquals(new BigDecimal("42.00"), d.valor());
            assertEquals(BRL, d.moeda());
        }

        @Test
        @DisplayName("zeroReais retorna zero em BRL com escala dois")
        void zeroReaisEZeroComEscalaDois() {
            Dinheiro d = Dinheiro.zeroReais();
            assertEquals(new BigDecimal("0.00"), d.valor());
            assertEquals(BRL, d.moeda());
        }

        @Test
        @DisplayName("normaliza escala para duas casas decimais")
        void normalizaEscalaParaDuasCasas() {
            Dinheiro d = Dinheiro.deReais("10.1");
            assertEquals(new BigDecimal("10.10"), d.valor());
            assertEquals(2, d.valor().scale());
        }

        @Test
        @DisplayName("arredondamento HALF_EVEN mantém último dígito par no caso de meio")
        void arredondamentoHalfEvenManteamPar() {
            // 10.125 está exatamente entre 10.12 e 10.13; HALF_EVEN escolhe 10.12 (par)
            Dinheiro d = Dinheiro.deReais("10.125");
            assertEquals(new BigDecimal("10.12"), d.valor());
        }

        @Test
        @DisplayName("arredondamento HALF_EVEN sobe para par no caso de meio")
        void arredondamentoHalfEvenSobeParaPar() {
            // 10.135 está exatamente entre 10.13 e 10.14; HALF_EVEN escolhe 10.14 (par)
            Dinheiro d = Dinheiro.deReais("10.135");
            assertEquals(new BigDecimal("10.14"), d.valor());
        }

        @Test
        @DisplayName("rejeita valor BigDecimal nulo")
        void rejeitaValorBigDecimalNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Dinheiro.de(null, BRL)
            );
            assertEquals("valor não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita valor String nulo")
        void rejeitaValorStringNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Dinheiro.deReais((String) null)
            );
            assertEquals("valor não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita moeda nula")
        void rejeitaMoedaNula() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Dinheiro.de(new BigDecimal("1.00"), null)
            );
            assertEquals("moeda não pode ser nula", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Igualdade")
    class Igualdade {

        @Test
        @DisplayName("dois Dinheiros com mesmo valor e mesma moeda são iguais")
        void iguaisQuandoMesmoValorEMoeda() {
            Dinheiro a = Dinheiro.deReais("10.00");
            Dinheiro b = Dinheiro.deReais("10.00");
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("escalas diferentes na entrada não afetam igualdade após normalização")
        void igualdadeAposNormalizacaoDeEscala() {
            Dinheiro a = Dinheiro.deReais("10");
            Dinheiro b = Dinheiro.deReais("10.00");
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("valores diferentes tornam os Dinheiros distintos")
        void distintosQuandoValoresDiferentes() {
            Dinheiro a = Dinheiro.deReais("10.00");
            Dinheiro b = Dinheiro.deReais("10.01");
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("moedas diferentes tornam os Dinheiros distintos")
        void distintosQuandoMoedasDiferentes() {
            Dinheiro a = Dinheiro.de(new BigDecimal("10.00"), BRL);
            Dinheiro b = Dinheiro.de(new BigDecimal("10.00"), USD);
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("equals com null retorna false")
        void equalsComNuloRetornaFalso() {
            Dinheiro a = Dinheiro.deReais("10.00");
            assertFalse(a.equals(null));
        }

        @Test
        @DisplayName("equals com tipo diferente retorna false")
        void equalsComOutroTipoRetornaFalso() {
            Dinheiro a = Dinheiro.deReais("10.00");
            assertFalse(a.equals("10.00"));
        }

        @Test
        @DisplayName("a instância é igual a si mesma")
        void reflexividade() {
            Dinheiro a = Dinheiro.deReais("10.00");
            assertTrue(a.equals(a));
        }
    }
}
