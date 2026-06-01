package domain.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CPFTest {

    private static final String CPF_VALIDO_SEM_MASCARA = "52998224725";
    private static final String CPF_VALIDO_COM_MASCARA = "529.982.247-25";

    @Nested
    @DisplayName("Sanitização e validação de tamanho")
    class Sanitizacao {

        @Test
        @DisplayName("aceita string contendo apenas 11 dígitos")
        void aceitaSomenteDigitos() {
            CPF cpf = CPF.de(CPF_VALIDO_SEM_MASCARA);
            assertEquals(CPF_VALIDO_SEM_MASCARA, cpf.numero());
        }

        @Test
        @DisplayName("aceita string com máscara e armazena apenas dígitos")
        void aceitaComMascara() {
            CPF cpf = CPF.de(CPF_VALIDO_COM_MASCARA);
            assertEquals(CPF_VALIDO_SEM_MASCARA, cpf.numero());
        }

        @Test
        @DisplayName("aceita entrada com espaços extras ao redor")
        void aceitaComEspacosExtras() {
            CPF cpf = CPF.de("  529.982.247-25  ");
            assertEquals(CPF_VALIDO_SEM_MASCARA, cpf.numero());
        }

        @Test
        @DisplayName("aceita entrada com espaços internos entre os grupos")
        void aceitaComEspacosInternos() {
            CPF cpf = CPF.de("529 982 247 25");
            assertEquals(CPF_VALIDO_SEM_MASCARA, cpf.numero());
        }

        @Test
        @DisplayName("rejeita entrada nula")
        void rejeitaNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CPF.de(null)
            );
            assertEquals("cpf não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita string vazia")
        void rejeitaVazio() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CPF.de("")
            );
            assertEquals("cpf deve ter 11 dígitos", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita entrada com menos de 11 dígitos")
        void rejeitaCurto() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CPF.de("12345")
            );
            assertEquals("cpf deve ter 11 dígitos", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita entrada com mais de 11 dígitos")
        void rejeitaLongo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CPF.de("123456789012")
            );
            assertEquals("cpf deve ter 11 dígitos", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita entrada totalmente formada por letras")
        void rejeitaApenasLetras() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CPF.de("abcdefghijk")
            );
            assertEquals("cpf deve ter 11 dígitos", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita entrada com letras misturadas a dígitos")
        void rejeitaLetrasMisturadas() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CPF.de("1234567890a")
            );
            assertEquals("cpf deve ter 11 dígitos", ex.getMessage());
        }
    }
}
