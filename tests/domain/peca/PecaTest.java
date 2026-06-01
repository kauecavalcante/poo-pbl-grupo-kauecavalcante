package domain.peca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Nested
    @DisplayName("Reconstituição a partir de identidade existente")
    class Reconstituicao {

        @Test
        @DisplayName("reconstituir preserva o ID informado")
        void reconstituirPreservaId() {
            PecaId id = PecaId.novo();
            Peca p = Peca.reconstituir(id, "FLT-1001", "Filtro de óleo", PRECO_PADRAO, 10);
            assertEquals(id, p.id());
        }

        @Test
        @DisplayName("reconstituir rejeita ID nulo")
        void reconstituirRejeitaIdNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Peca.reconstituir(null, "FLT-1001", "Filtro de óleo", PRECO_PADRAO, 10)
            );
            assertEquals("id da peça não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("reconstituir aplica as mesmas validações de código")
        void reconstituirValidaCodigo() {
            assertThrows(
                IllegalArgumentException.class,
                () -> Peca.reconstituir(PecaId.novo(), "AB", "Filtro de óleo", PRECO_PADRAO, 10)
            );
        }

        @Test
        @DisplayName("reconstituir aplica as mesmas validações de estoque")
        void reconstituirValidaEstoque() {
            assertThrows(
                IllegalArgumentException.class,
                () -> Peca.reconstituir(PecaId.novo(), "FLT-1001", "Filtro de óleo", PRECO_PADRAO, -5)
            );
        }
    }

    @Nested
    @DisplayName("Igualdade por identidade")
    class Igualdade {

        @Test
        @DisplayName("duas peças com mesmo ID são iguais mesmo com dados divergentes")
        void iguaisQuandoMesmoIdAindaQueDadosMudem() {
            PecaId id = PecaId.novo();
            Peca antes = Peca.reconstituir(id, "FLT-1001", "Filtro de óleo", PRECO_PADRAO, 10);
            Peca depois = Peca.reconstituir(id, "FLT-1001", "Filtro de óleo premium", Preco.deReais("250.00"), 3);
            assertEquals(antes, depois);
            assertEquals(antes.hashCode(), depois.hashCode());
        }

        @Test
        @DisplayName("duas peças com IDs diferentes não são iguais mesmo com dados idênticos")
        void distintasQuandoIdsDiferentes() {
            Peca a = Peca.reconstituir(PecaId.novo(), "FLT-1001", "Filtro de óleo", PRECO_PADRAO, 10);
            Peca b = Peca.reconstituir(PecaId.novo(), "FLT-1001", "Filtro de óleo", PRECO_PADRAO, 10);
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("equals com null retorna false")
        void equalsComNuloRetornaFalso() {
            Peca p = Peca.nova("FLT-1001", "Filtro de óleo", PRECO_PADRAO, 10);
            assertFalse(p.equals(null));
        }

        @Test
        @DisplayName("equals com tipo diferente retorna false")
        void equalsComOutroTipoRetornaFalso() {
            Peca p = Peca.nova("FLT-1001", "Filtro de óleo", PRECO_PADRAO, 10);
            assertFalse(p.equals(p.id()));
        }

        @Test
        @DisplayName("a instância é igual a si mesma")
        void reflexividade() {
            Peca p = Peca.nova("FLT-1001", "Filtro de óleo", PRECO_PADRAO, 10);
            assertTrue(p.equals(p));
        }
    }

    @Nested
    @DisplayName("Predicados de estoque")
    class Predicados {

        @Test
        @DisplayName("temEstoque retorna false para peça em ruptura")
        void temEstoqueFalsoQuandoZero() {
            Peca p = Peca.nova("FLT-1001", "Filtro de óleo", PRECO_PADRAO, 0);
            assertFalse(p.temEstoque());
        }

        @Test
        @DisplayName("temEstoque retorna true para qualquer quantidade positiva")
        void temEstoqueTrueQuandoPositivo() {
            Peca p = Peca.nova("FLT-1001", "Filtro de óleo", PRECO_PADRAO, 1);
            assertTrue(p.temEstoque());
        }

        @Test
        @DisplayName("temEstoqueSuficiente retorna true quando estoque excede a quantidade pedida")
        void temEstoqueSuficienteExcedente() {
            Peca p = Peca.nova("FLT-1001", "Filtro de óleo", PRECO_PADRAO, 10);
            assertTrue(p.temEstoqueSuficiente(5));
        }

        @Test
        @DisplayName("temEstoqueSuficiente retorna true quando estoque iguala a quantidade pedida")
        void temEstoqueSuficienteExato() {
            Peca p = Peca.nova("FLT-1001", "Filtro de óleo", PRECO_PADRAO, 10);
            assertTrue(p.temEstoqueSuficiente(10));
        }

        @Test
        @DisplayName("temEstoqueSuficiente retorna false quando estoque é menor que a quantidade pedida")
        void temEstoqueSuficienteInsuficiente() {
            Peca p = Peca.nova("FLT-1001", "Filtro de óleo", PRECO_PADRAO, 3);
            assertFalse(p.temEstoqueSuficiente(5));
        }

        @Test
        @DisplayName("temEstoqueSuficiente para quantidade zero é sempre verdadeiro")
        void temEstoqueSuficienteZero() {
            Peca p = Peca.nova("FLT-1001", "Filtro de óleo", PRECO_PADRAO, 0);
            assertTrue(p.temEstoqueSuficiente(0));
        }

        @Test
        @DisplayName("temEstoqueSuficiente rejeita quantidade negativa")
        void temEstoqueSuficienteRejeitaNegativo() {
            Peca p = Peca.nova("FLT-1001", "Filtro de óleo", PRECO_PADRAO, 10);
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> p.temEstoqueSuficiente(-1)
            );
            assertEquals("quantidade não pode ser negativa", ex.getMessage());
        }
    }
}
