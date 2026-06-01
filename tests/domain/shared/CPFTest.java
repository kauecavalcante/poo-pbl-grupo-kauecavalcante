package domain.shared;

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

    @Nested
    @DisplayName("Sequências de dígitos repetidos")
    class DigitosRepetidos {

        @ParameterizedTest(name = "rejeita {0}")
        @ValueSource(strings = {
            "00000000000",
            "11111111111",
            "22222222222",
            "33333333333",
            "44444444444",
            "55555555555",
            "66666666666",
            "77777777777",
            "88888888888",
            "99999999999"
        })
        @DisplayName("rejeita CPF com todos os dígitos iguais")
        void rejeitaSequenciaRepetida(String entrada) {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CPF.de(entrada)
            );
            assertEquals("cpf inválido", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Dígitos verificadores")
    class DigitosVerificadores {

        @ParameterizedTest(name = "aceita CPF válido {0}")
        @ValueSource(strings = {
            "529.982.247-25",
            "111.444.777-35",
            "390.533.447-05"
        })
        @DisplayName("aceita CPFs com dígitos verificadores corretos")
        void aceitaCpfValido(String entrada) {
            CPF cpf = CPF.de(entrada);
            assertEquals(11, cpf.numero().length());
        }

        @Test
        @DisplayName("rejeita CPF com 10º dígito (DV1) errado")
        void rejeitaDv1Errado() {
            // "529.982.247-25" é válido; trocar DV1 de 2 para 1 invalida só o primeiro DV
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CPF.de("529.982.247-15")
            );
            assertEquals("cpf inválido", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita CPF com 11º dígito (DV2) errado")
        void rejeitaDv2Errado() {
            // "529.982.247-25" é válido; trocar DV2 de 5 para 6 invalida só o segundo DV
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CPF.de("529.982.247-26")
            );
            assertEquals("cpf inválido", ex.getMessage());
        }

        @ParameterizedTest(name = "rejeita CPF inválido {0}")
        @ValueSource(strings = {
            "123.456.789-00",
            "390.533.447-04",
            "111.444.777-30",
            "390.533.447-15",
            "111.444.777-99"
        })
        @DisplayName("rejeita CPFs com dígitos verificadores incorretos")
        void rejeitaCpfsInvalidos(String entrada) {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CPF.de(entrada)
            );
            assertEquals("cpf inválido", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Formatação e igualdade")
    class Apresentacao {

        @Test
        @DisplayName("formatado retorna CPF com pontuação canônica")
        void formatadoComMascara() {
            CPF cpf = CPF.de(CPF_VALIDO_SEM_MASCARA);
            assertEquals(CPF_VALIDO_COM_MASCARA, cpf.formatado());
        }

        @Test
        @DisplayName("toString retorna o mesmo formato de formatado")
        void toStringIgualAoFormatado() {
            CPF cpf = CPF.de(CPF_VALIDO_SEM_MASCARA);
            assertEquals(cpf.formatado(), cpf.toString());
        }

        @Test
        @DisplayName("dois CPFs criados a partir de representações diferentes são iguais")
        void igualdadeIndependeDaMascaraNaEntrada() {
            CPF semMascara = CPF.de(CPF_VALIDO_SEM_MASCARA);
            CPF comMascara = CPF.de(CPF_VALIDO_COM_MASCARA);
            assertEquals(semMascara, comMascara);
            assertEquals(semMascara.hashCode(), comMascara.hashCode());
        }

        @Test
        @DisplayName("CPFs com números diferentes não são iguais")
        void distintosQuandoNumerosDiferentes() {
            CPF a = CPF.de("529.982.247-25");
            CPF b = CPF.de("111.444.777-35");
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("equals com null retorna false")
        void equalsComNuloRetornaFalso() {
            CPF cpf = CPF.de(CPF_VALIDO_SEM_MASCARA);
            assertFalse(cpf.equals(null));
        }

        @Test
        @DisplayName("equals com tipo diferente retorna false")
        void equalsComOutroTipoRetornaFalso() {
            CPF cpf = CPF.de(CPF_VALIDO_SEM_MASCARA);
            assertFalse(cpf.equals(CPF_VALIDO_SEM_MASCARA));
        }

        @Test
        @DisplayName("a instância é igual a si mesma")
        void reflexividade() {
            CPF cpf = CPF.de(CPF_VALIDO_SEM_MASCARA);
            assertTrue(cpf.equals(cpf));
        }
    }
}
