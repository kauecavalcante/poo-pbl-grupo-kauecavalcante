package domain.peca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import domain.shared.Preco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PecaTest {

    private static final Preco PRECO_PADRAO = Preco.deReais("199.90");

    @Nested
    @DisplayName("Criação de nova peça")
    class Criacao {

        @Test
        @DisplayName("nova gera identidade")
        void novaGeraIdentidade() {
            Peca p = Peca.nova("FLT-1001", "Filtro de óleo", PRECO_PADRAO, 10);
            assertNotNull(p.id());
        }

        @Test
        @DisplayName("nova expõe os dados nos acessores")
        void acessoresExpoeDados() {
            Peca p = Peca.nova("FLT-1001", "Filtro de óleo", PRECO_PADRAO, 10);
            assertEquals("FLT-1001", p.codigo());
            assertEquals("Filtro de óleo", p.descricao());
            assertSame(PRECO_PADRAO, p.preco());
            assertEquals(10, p.estoque());
        }

        @Test
        @DisplayName("código com espaços ao redor é armazenado com trim")
        void codigoArmazenadoComTrim() {
            Peca p = Peca.nova("  FLT-1001  ", "Filtro de óleo", PRECO_PADRAO, 10);
            assertEquals("FLT-1001", p.codigo());
        }

        @Test
        @DisplayName("descrição com espaços ao redor é armazenada com trim")
        void descricaoArmazenadaComTrim() {
            Peca p = Peca.nova("FLT-1001", "  Filtro de óleo  ", PRECO_PADRAO, 10);
            assertEquals("Filtro de óleo", p.descricao());
        }

        @Test
        @DisplayName("estoque inicial zero é aceito (peça em ruptura)")
        void estoqueInicialZeroAceito() {
            Peca p = Peca.nova("FLT-1001", "Filtro de óleo", PRECO_PADRAO, 0);
            assertEquals(0, p.estoque());
        }

        @Test
        @DisplayName("preço zero é aceito (item de cortesia)")
        void precoZeroAceito() {
            Peca p = Peca.nova("FLT-1001", "Brinde promocional", Preco.zero(), 10);
            assertEquals(Preco.zero(), p.preco());
        }
    }

    @Nested
    @DisplayName("Invariantes da peça")
    class Invariantes {

        @Test
        @DisplayName("rejeita código nulo")
        void rejeitaCodigoNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Peca.nova(null, "Filtro de óleo", PRECO_PADRAO, 10)
            );
            assertEquals("código da peça não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita código vazio ou só espaços")
        void rejeitaCodigoVazio() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Peca.nova("   ", "Filtro de óleo", PRECO_PADRAO, 10)
            );
            assertEquals("código da peça não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita código com menos de 3 caracteres após trim")
        void rejeitaCodigoCurto() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Peca.nova("AB", "Filtro de óleo", PRECO_PADRAO, 10)
            );
            assertEquals("código da peça deve ter ao menos 3 caracteres", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita código com espaços internos")
        void rejeitaCodigoComEspacosInternos() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Peca.nova("FLT 1001", "Filtro de óleo", PRECO_PADRAO, 10)
            );
            assertEquals("código da peça não pode conter espaços", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita descrição nula")
        void rejeitaDescricaoNula() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Peca.nova("FLT-1001", null, PRECO_PADRAO, 10)
            );
            assertEquals("descrição da peça não pode ser vazia", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita descrição vazia ou só espaços")
        void rejeitaDescricaoVazia() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Peca.nova("FLT-1001", "    ", PRECO_PADRAO, 10)
            );
            assertEquals("descrição da peça não pode ser vazia", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita descrição com menos de 3 caracteres após trim")
        void rejeitaDescricaoCurta() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Peca.nova("FLT-1001", "AB", PRECO_PADRAO, 10)
            );
            assertEquals("descrição da peça deve ter ao menos 3 caracteres", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita preço nulo")
        void rejeitaPrecoNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Peca.nova("FLT-1001", "Filtro de óleo", null, 10)
            );
            assertEquals("preço não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita estoque inicial negativo")
        void rejeitaEstoqueNegativo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Peca.nova("FLT-1001", "Filtro de óleo", PRECO_PADRAO, -1)
            );
            assertEquals("estoque não pode ser negativo", ex.getMessage());
        }
    }
}
