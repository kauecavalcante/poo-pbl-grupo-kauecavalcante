package domain.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PrecoTest {

    @Nested
    @DisplayName("Criação e validação")
    class Criacao {

        @Test
        @DisplayName("de aceita Dinheiro positivo e expõe o valor interno")
        void aceitaDinheiroPositivo() {
            Dinheiro d = Dinheiro.deReais("10.00");
            Preco p = Preco.de(d);
            assertSame(d, p.valor());
        }

        @Test
        @DisplayName("de aceita Dinheiro zero — item de cortesia é preço válido")
        void aceitaDinheiroZero() {
            Preco p = Preco.de(Dinheiro.zeroReais());
            assertEquals(Dinheiro.zeroReais(), p.valor());
        }

        @Test
        @DisplayName("deReais a partir de BigDecimal cria preço em BRL")
        void deReaisAPartirDeBigDecimal() {
            Preco p = Preco.deReais(new BigDecimal("99.90"));
            assertEquals(Dinheiro.deReais("99.90"), p.valor());
        }

        @Test
        @DisplayName("deReais a partir de String cria preço em BRL")
        void deReaisAPartirDeString() {
            Preco p = Preco.deReais("42.00");
            assertEquals(Dinheiro.deReais("42.00"), p.valor());
        }

        @Test
        @DisplayName("zero retorna preço zerado em BRL")
        void zeroRetornaPrecoZerado() {
            Preco p = Preco.zero();
            assertEquals(Dinheiro.zeroReais(), p.valor());
        }

        @Test
        @DisplayName("rejeita Dinheiro nulo")
        void rejeitaDinheiroNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Preco.de(null)
            );
            assertEquals("preço não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita Dinheiro negativo")
        void rejeitaDinheiroNegativo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Preco.de(Dinheiro.deReais("-1.00"))
            );
            assertEquals("preço não pode ser negativo", ex.getMessage());
        }

        @Test
        @DisplayName("deReais rejeita valor negativo via String")
        void deReaisRejeitaNegativoViaString() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Preco.deReais("-0.01")
            );
            assertEquals("preço não pode ser negativo", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Operações")
    class Operacoes {

        @Test
        @DisplayName("somar dois preços positivos produz preço positivo")
        void somaDoisPositivos() {
            Preco resultado = Preco.deReais("10.00").somar(Preco.deReais("5.50"));
            assertEquals(Preco.deReais("15.50"), resultado);
        }

        @Test
        @DisplayName("somar com zero preserva o valor")
        void somaComZeroPreserva() {
            Preco original = Preco.deReais("10.00");
            assertEquals(original, original.somar(Preco.zero()));
        }

        @Test
        @DisplayName("multiplicar por inteiro positivo escala o preço")
        void multiplicarPorInteiroPositivo() {
            Preco resultado = Preco.deReais("10.00").multiplicar(3);
            assertEquals(Preco.deReais("30.00"), resultado);
        }

        @Test
        @DisplayName("multiplicar por zero retorna preço zerado")
        void multiplicarPorZero() {
            Preco resultado = Preco.deReais("99.99").multiplicar(0);
            assertEquals(Preco.zero(), resultado);
        }

        @Test
        @DisplayName("multiplicar por inteiro negativo é rejeitado")
        void multiplicarPorInteiroNegativoRejeita() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Preco.deReais("10.00").multiplicar(-1)
            );
            assertEquals("fator de multiplicação não pode ser negativo", ex.getMessage());
        }

        @Test
        @DisplayName("multiplicar por BigDecimal positivo escala o preço")
        void multiplicarPorBigDecimalPositivo() {
            Preco resultado = Preco.deReais("10.00").multiplicar(new BigDecimal("1.5"));
            assertEquals(Preco.deReais("15.00"), resultado);
        }

        @Test
        @DisplayName("multiplicar por BigDecimal zero retorna preço zerado")
        void multiplicarPorBigDecimalZero() {
            Preco resultado = Preco.deReais("99.99").multiplicar(BigDecimal.ZERO);
            assertEquals(Preco.zero(), resultado);
        }

        @Test
        @DisplayName("multiplicar por BigDecimal negativo é rejeitado")
        void multiplicarPorBigDecimalNegativoRejeita() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Preco.deReais("10.00").multiplicar(new BigDecimal("-0.5"))
            );
            assertEquals("fator de multiplicação não pode ser negativo", ex.getMessage());
        }

        @Test
        @DisplayName("operações não modificam o preço original")
        void imutabilidade() {
            Preco original = Preco.deReais("10.00");
            original.somar(Preco.deReais("5.00"));
            original.multiplicar(3);
            original.multiplicar(new BigDecimal("2.0"));
            assertEquals(Preco.deReais("10.00"), original);
        }
    }

    @Nested
    @DisplayName("Igualdade e formatação")
    class Igualdade {

        @Test
        @DisplayName("preços com mesmo valor interno são iguais")
        void iguaisQuandoMesmoValor() {
            Preco a = Preco.deReais("10.00");
            Preco b = Preco.deReais("10.00");
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("preços com valores diferentes não são iguais")
        void distintosQuandoValoresDiferentes() {
            assertNotEquals(Preco.deReais("10.00"), Preco.deReais("10.01"));
        }

        @Test
        @DisplayName("equals com null retorna false")
        void equalsComNuloRetornaFalso() {
            assertFalse(Preco.deReais("10.00").equals(null));
        }

        @Test
        @DisplayName("equals com outro tipo retorna false")
        void equalsComOutroTipoRetornaFalso() {
            Preco p = Preco.deReais("10.00");
            assertFalse(p.equals(p.valor()));
        }

        @Test
        @DisplayName("a instância é igual a si mesma")
        void reflexividade() {
            Preco p = Preco.deReais("10.00");
            assertTrue(p.equals(p));
        }

        @Test
        @DisplayName("toString delega para a representação do Dinheiro")
        void toStringDelegaParaDinheiro() {
            Preco p = Preco.deReais("1234.56");
            assertEquals(Dinheiro.deReais("1234.56").toString(), p.toString());
        }
    }
}
