package domain.cliente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;

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
}
