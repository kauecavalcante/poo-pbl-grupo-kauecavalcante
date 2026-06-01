package domain.cliente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.shared.CPF;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ClienteTest {

    private static final CPF CPF_VALIDO = CPF.de("529.982.247-25");

    @Nested
    @DisplayName("Criação de novo cliente")
    class Criacao {

        @Test
        @DisplayName("novo cria cliente com identidade gerada")
        void novoGeraIdentidade() {
            Cliente c = Cliente.novo("Maria Silva", CPF_VALIDO, "11912345678");
            assertNotNull(c.id());
        }

        @Test
        @DisplayName("dois clientes criados via novo recebem IDs distintos")
        void novoGeraIdsDistintos() {
            Cliente a = Cliente.novo("Maria Silva", CPF_VALIDO, "11912345678");
            Cliente b = Cliente.novo("João Souza", CPF.de("111.444.777-35"), "11999998888");
            assertEquals(false, a.id().equals(b.id()));
        }

        @Test
        @DisplayName("acessores expõem os dados informados")
        void acessoresExpoeOsDados() {
            Cliente c = Cliente.novo("Maria Silva", CPF_VALIDO, "11912345678");
            assertEquals("Maria Silva", c.nome());
            assertSame(CPF_VALIDO, c.cpf());
            assertEquals("11912345678", c.telefone());
        }

        @Test
        @DisplayName("nome com espaços ao redor é armazenado já com trim")
        void nomeArmazenadoComTrim() {
            Cliente c = Cliente.novo("   Maria Silva   ", CPF_VALIDO, "11912345678");
            assertEquals("Maria Silva", c.nome());
        }

        @Test
        @DisplayName("telefone com espaços ao redor é armazenado já com trim")
        void telefoneArmazenadoComTrim() {
            Cliente c = Cliente.novo("Maria Silva", CPF_VALIDO, "  11912345678  ");
            assertEquals("11912345678", c.telefone());
        }

        @Test
        @DisplayName("telefone com máscara é aceito desde que tenha ao menos 8 dígitos")
        void telefoneComMascaraAceito() {
            Cliente c = Cliente.novo("Maria Silva", CPF_VALIDO, "(11) 91234-5678");
            assertEquals("(11) 91234-5678", c.telefone());
        }

        @Test
        @DisplayName("telefone com exatamente 8 dígitos é aceito")
        void telefoneComOitoDigitosAceito() {
            Cliente c = Cliente.novo("Maria Silva", CPF_VALIDO, "12345678");
            assertEquals("12345678", c.telefone());
        }
    }

    @Nested
    @DisplayName("Invariantes do cliente")
    class Invariantes {

        @Test
        @DisplayName("rejeita nome nulo")
        void rejeitaNomeNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Cliente.novo(null, CPF_VALIDO, "11912345678")
            );
            assertEquals("nome do cliente não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita nome vazio")
        void rejeitaNomeVazio() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Cliente.novo("", CPF_VALIDO, "11912345678")
            );
            assertEquals("nome do cliente não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita nome composto somente de espaços")
        void rejeitaNomeSomenteEspacos() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Cliente.novo("     ", CPF_VALIDO, "11912345678")
            );
            assertEquals("nome do cliente não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita nome com menos de 2 caracteres após trim")
        void rejeitaNomeMuitoCurto() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Cliente.novo("  A  ", CPF_VALIDO, "11912345678")
            );
            assertEquals("nome do cliente deve ter ao menos 2 caracteres", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita cpf nulo")
        void rejeitaCpfNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Cliente.novo("Maria Silva", null, "11912345678")
            );
            assertEquals("cpf não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita telefone nulo")
        void rejeitaTelefoneNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Cliente.novo("Maria Silva", CPF_VALIDO, null)
            );
            assertEquals("telefone não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita telefone vazio")
        void rejeitaTelefoneVazio() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Cliente.novo("Maria Silva", CPF_VALIDO, "")
            );
            assertEquals("telefone não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita telefone composto somente de espaços")
        void rejeitaTelefoneSomenteEspacos() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Cliente.novo("Maria Silva", CPF_VALIDO, "      ")
            );
            assertEquals("telefone não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita telefone com menos de 8 dígitos contando só números")
        void rejeitaTelefoneCurto() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Cliente.novo("Maria Silva", CPF_VALIDO, "(11) 1234")
            );
            assertEquals("telefone deve ter ao menos 8 dígitos", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Reconstituição a partir de identidade existente")
    class Reconstituicao {

        @Test
        @DisplayName("reconstituir preserva o ID informado")
        void reconstituirPreservaId() {
            ClienteId id = ClienteId.novo();
            Cliente c = Cliente.reconstituir(id, "Maria Silva", CPF_VALIDO, "11912345678");
            assertEquals(id, c.id());
        }

        @Test
        @DisplayName("reconstituir rejeita ID nulo")
        void reconstituirRejeitaIdNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Cliente.reconstituir(null, "Maria Silva", CPF_VALIDO, "11912345678")
            );
            assertEquals("id do cliente não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("reconstituir aplica as mesmas validações de nome")
        void reconstituirValidaNome() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Cliente.reconstituir(ClienteId.novo(), "", CPF_VALIDO, "11912345678")
            );
            assertEquals("nome do cliente não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("reconstituir aplica as mesmas validações de cpf")
        void reconstituirValidaCpf() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Cliente.reconstituir(ClienteId.novo(), "Maria Silva", null, "11912345678")
            );
            assertEquals("cpf não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("reconstituir aplica as mesmas validações de telefone")
        void reconstituirValidaTelefone() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Cliente.reconstituir(ClienteId.novo(), "Maria Silva", CPF_VALIDO, "123")
            );
            assertEquals("telefone deve ter ao menos 8 dígitos", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Igualdade por identidade")
    class Igualdade {

        @Test
        @DisplayName("dois clientes com mesmo ID são iguais mesmo com nome e telefone divergentes")
        void iguaisQuandoMesmoIdAindaQueDadosMudem() {
            ClienteId id = ClienteId.novo();
            Cliente antes = Cliente.reconstituir(id, "Maria Silva", CPF_VALIDO, "11912345678");
            Cliente depois = Cliente.reconstituir(id, "Maria S. Pereira", CPF_VALIDO, "11999998888");
            assertEquals(antes, depois);
            assertEquals(antes.hashCode(), depois.hashCode());
        }

        @Test
        @DisplayName("dois clientes com IDs diferentes não são iguais mesmo com dados idênticos")
        void distintosQuandoIdsDiferentes() {
            Cliente a = Cliente.reconstituir(ClienteId.novo(), "Maria Silva", CPF_VALIDO, "11912345678");
            Cliente b = Cliente.reconstituir(ClienteId.novo(), "Maria Silva", CPF_VALIDO, "11912345678");
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("equals com null retorna false")
        void equalsComNuloRetornaFalso() {
            Cliente c = Cliente.novo("Maria Silva", CPF_VALIDO, "11912345678");
            assertFalse(c.equals(null));
        }

        @Test
        @DisplayName("equals com tipo diferente retorna false")
        void equalsComOutroTipoRetornaFalso() {
            Cliente c = Cliente.novo("Maria Silva", CPF_VALIDO, "11912345678");
            assertFalse(c.equals(c.id()));
        }

        @Test
        @DisplayName("a instância é igual a si mesma")
        void reflexividade() {
            Cliente c = Cliente.novo("Maria Silva", CPF_VALIDO, "11912345678");
            assertTrue(c.equals(c));
        }
    }

    @Nested
    @DisplayName("Mutações controladas")
    class Mutacoes {

        @Test
        @DisplayName("alterarNome substitui o nome preservando a identidade")
        void alterarNomePreservaIdentidade() {
            Cliente c = Cliente.novo("Maria Silva", CPF_VALIDO, "11912345678");
            ClienteId idAntes = c.id();
            c.alterarNome("Maria S. Pereira");
            assertEquals("Maria S. Pereira", c.nome());
            assertSame(idAntes, c.id());
        }

        @Test
        @DisplayName("alterarNome aplica trim no novo valor")
        void alterarNomeAplicaTrim() {
            Cliente c = Cliente.novo("Maria Silva", CPF_VALIDO, "11912345678");
            c.alterarNome("   João Souza   ");
            assertEquals("João Souza", c.nome());
        }

        @Test
        @DisplayName("alterarNome rejeita valor nulo")
        void alterarNomeRejeitaNulo() {
            Cliente c = Cliente.novo("Maria Silva", CPF_VALIDO, "11912345678");
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> c.alterarNome(null)
            );
            assertEquals("nome do cliente não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("alterarNome rejeita valor vazio ou só espaços")
        void alterarNomeRejeitaVazio() {
            Cliente c = Cliente.novo("Maria Silva", CPF_VALIDO, "11912345678");
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> c.alterarNome("   ")
            );
            assertEquals("nome do cliente não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("alterarNome rejeita nome curto demais")
        void alterarNomeRejeitaCurto() {
            Cliente c = Cliente.novo("Maria Silva", CPF_VALIDO, "11912345678");
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> c.alterarNome("A")
            );
            assertEquals("nome do cliente deve ter ao menos 2 caracteres", ex.getMessage());
        }

        @Test
        @DisplayName("alterarTelefone substitui o telefone preservando a identidade")
        void alterarTelefonePreservaIdentidade() {
            Cliente c = Cliente.novo("Maria Silva", CPF_VALIDO, "11912345678");
            ClienteId idAntes = c.id();
            c.alterarTelefone("(11) 99999-8888");
            assertEquals("(11) 99999-8888", c.telefone());
            assertSame(idAntes, c.id());
        }

        @Test
        @DisplayName("alterarTelefone rejeita valor nulo")
        void alterarTelefoneRejeitaNulo() {
            Cliente c = Cliente.novo("Maria Silva", CPF_VALIDO, "11912345678");
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> c.alterarTelefone(null)
            );
            assertEquals("telefone não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("alterarTelefone rejeita valor vazio ou só espaços")
        void alterarTelefoneRejeitaVazio() {
            Cliente c = Cliente.novo("Maria Silva", CPF_VALIDO, "11912345678");
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> c.alterarTelefone("   ")
            );
            assertEquals("telefone não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("alterarTelefone rejeita telefone com menos de 8 dígitos")
        void alterarTelefoneRejeitaCurto() {
            Cliente c = Cliente.novo("Maria Silva", CPF_VALIDO, "11912345678");
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> c.alterarTelefone("(11) 123")
            );
            assertEquals("telefone deve ter ao menos 8 dígitos", ex.getMessage());
        }

        @Test
        @DisplayName("alteração falha não modifica o estado do cliente")
        void alteracaoFalhaNaoModificaEstado() {
            Cliente c = Cliente.novo("Maria Silva", CPF_VALIDO, "11912345678");
            assertThrows(IllegalArgumentException.class, () -> c.alterarNome("A"));
            assertThrows(IllegalArgumentException.class, () -> c.alterarTelefone("123"));
            assertEquals("Maria Silva", c.nome());
            assertEquals("11912345678", c.telefone());
        }
    }
}
