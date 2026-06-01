package domain.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
