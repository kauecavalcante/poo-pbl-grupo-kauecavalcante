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

    @Nested
    @DisplayName("Operações aritméticas")
    class Operacoes {

        @Test
        @DisplayName("somar dois valores positivos")
        void somaDoisPositivos() {
            Dinheiro resultado = Dinheiro.deReais("10.00").somar(Dinheiro.deReais("5.50"));
            assertEquals(Dinheiro.deReais("15.50"), resultado);
        }

        @Test
        @DisplayName("somar valores opostos resulta em zero")
        void somaResultandoEmZero() {
            Dinheiro resultado = Dinheiro.deReais("10.00").somar(Dinheiro.deReais("-10.00"));
            assertEquals(Dinheiro.zeroReais(), resultado);
        }

        @Test
        @DisplayName("subtrair valor maior produz resultado negativo")
        void subtracaoComResultadoNegativo() {
            Dinheiro resultado = Dinheiro.deReais("5.00").subtrair(Dinheiro.deReais("8.00"));
            assertEquals(Dinheiro.deReais("-3.00"), resultado);
        }

        @Test
        @DisplayName("multiplicar por BigDecimal fracionário")
        void multiplicacaoPorBigDecimal() {
            Dinheiro resultado = Dinheiro.deReais("10.00").multiplicar(new BigDecimal("1.5"));
            assertEquals(Dinheiro.deReais("15.00"), resultado);
        }

        @Test
        @DisplayName("multiplicar por inteiro")
        void multiplicacaoPorInteiro() {
            Dinheiro resultado = Dinheiro.deReais("7.50").multiplicar(3);
            assertEquals(Dinheiro.deReais("22.50"), resultado);
        }

        @Test
        @DisplayName("multiplicar por zero retorna zero")
        void multiplicacaoPorZero() {
            Dinheiro resultado = Dinheiro.deReais("123.45").multiplicar(0);
            assertEquals(Dinheiro.zeroReais(), resultado);
        }

        @Test
        @DisplayName("multiplicação aplica HALF_EVEN no resultado")
        void multiplicacaoArredondaHalfEven() {
            // 10.00 * 0.333 = 3.33000 → arredonda para 3.33
            Dinheiro resultado = Dinheiro.deReais("10.00").multiplicar(new BigDecimal("0.333"));
            assertEquals(Dinheiro.deReais("3.33"), resultado);
        }

        @Test
        @DisplayName("operações não modificam o Dinheiro original")
        void imutabilidade() {
            Dinheiro original = Dinheiro.deReais("10.00");
            original.somar(Dinheiro.deReais("5.00"));
            original.subtrair(Dinheiro.deReais("2.00"));
            original.multiplicar(7);
            original.multiplicar(new BigDecimal("3.14"));
            assertEquals(Dinheiro.deReais("10.00"), original);
        }
    }

    @Nested
    @DisplayName("Incompatibilidade entre moedas")
    class MoedasDistintas {

        @Test
        @DisplayName("somar moedas diferentes lança MoedasIncompativeisException")
        void somaEntreMoedasDistintasLancaExcecao() {
            Dinheiro reais = Dinheiro.de(new BigDecimal("10.00"), BRL);
            Dinheiro dolares = Dinheiro.de(new BigDecimal("10.00"), USD);

            MoedasIncompativeisException ex = assertThrows(
                MoedasIncompativeisException.class,
                () -> reais.somar(dolares)
            );
            assertTrue(ex.getMessage().contains("BRL"));
            assertTrue(ex.getMessage().contains("USD"));
        }

        @Test
        @DisplayName("subtrair moedas diferentes lança MoedasIncompativeisException")
        void subtracaoEntreMoedasDistintasLancaExcecao() {
            Dinheiro reais = Dinheiro.de(new BigDecimal("10.00"), BRL);
            Dinheiro dolares = Dinheiro.de(new BigDecimal("10.00"), USD);

            MoedasIncompativeisException ex = assertThrows(
                MoedasIncompativeisException.class,
                () -> reais.subtrair(dolares)
            );
            assertTrue(ex.getMessage().contains("BRL"));
            assertTrue(ex.getMessage().contains("USD"));
        }

        @Test
        @DisplayName("mensagem da exceção identifica explicitamente as duas moedas")
        void mensagemDaExcecaoCitaAmbasMoedas() {
            Dinheiro reais = Dinheiro.de(new BigDecimal("1.00"), BRL);
            Dinheiro dolares = Dinheiro.de(new BigDecimal("1.00"), USD);

            MoedasIncompativeisException ex = assertThrows(
                MoedasIncompativeisException.class,
                () -> reais.somar(dolares)
            );
            assertEquals("operação entre moedas distintas: BRL e USD", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Predicados de sinal")
    class Predicados {

        @Test
        @DisplayName("zero é ehZero e nem positivo nem negativo")
        void zeroReconhecidoComoZero() {
            Dinheiro zero = Dinheiro.zeroReais();
            assertTrue(zero.ehZero());
            assertFalse(zero.ehPositivo());
            assertFalse(zero.ehNegativo());
        }

        @Test
        @DisplayName("valor positivo é ehPositivo e não é zero nem negativo")
        void positivoReconhecidoComoPositivo() {
            Dinheiro d = Dinheiro.deReais("1.00");
            assertTrue(d.ehPositivo());
            assertFalse(d.ehZero());
            assertFalse(d.ehNegativo());
        }

        @Test
        @DisplayName("valor negativo é ehNegativo e não é zero nem positivo")
        void negativoReconhecidoComoNegativo() {
            Dinheiro d = Dinheiro.deReais("-1.00");
            assertTrue(d.ehNegativo());
            assertFalse(d.ehZero());
            assertFalse(d.ehPositivo());
        }

        @Test
        @DisplayName("zero também é reconhecido quando construído explicitamente como 0,00")
        void zeroExplicitoTambemEhZero() {
            Dinheiro d = Dinheiro.deReais("0.00");
            assertTrue(d.ehZero());
        }
    }

    @Nested
    @DisplayName("Representação textual")
    class Formatacao {

        @Test
        @DisplayName("BRL com milhar usa ponto como separador e vírgula como decimal")
        void brlComMilhar() {
            assertEquals("R$ 1.234,56", Dinheiro.deReais("1234.56").toString());
        }

        @Test
        @DisplayName("BRL sem milhar mostra apenas duas casas decimais")
        void brlSemMilhar() {
            assertEquals("R$ 10,00", Dinheiro.deReais("10").toString());
        }

        @Test
        @DisplayName("BRL zero é apresentado como R$ 0,00")
        void brlZero() {
            assertEquals("R$ 0,00", Dinheiro.zeroReais().toString());
        }

        @Test
        @DisplayName("BRL negativo apresenta sinal antes do valor")
        void brlNegativo() {
            assertEquals("R$ -10,00", Dinheiro.deReais("-10").toString());
        }

        @Test
        @DisplayName("moeda diferente de BRL usa código ISO seguido do valor")
        void outraMoedaUsaCodigoIso() {
            Dinheiro d = Dinheiro.de(new BigDecimal("10.00"), USD);
            assertEquals("USD 10.00", d.toString());
        }

        @Test
        @DisplayName("moeda diferente de BRL preserva sinal negativo")
        void outraMoedaPreservaSinal() {
            Dinheiro d = Dinheiro.de(new BigDecimal("-10.00"), USD);
            assertEquals("USD -10.00", d.toString());
        }
    }
}
