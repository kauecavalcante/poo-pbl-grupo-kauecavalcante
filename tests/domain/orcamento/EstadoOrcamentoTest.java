package domain.orcamento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class EstadoOrcamentoTest {

    @Nested
    @DisplayName("Rascunho")
    class RascunhoTest {

        @Test
        @DisplayName("status retorna RASCUNHO")
        void statusRascunho() {
            assertEquals(StatusOrcamento.RASCUNHO, new Rascunho().status());
        }

        @Test
        @DisplayName("permite edição de itens")
        void permiteEditar() {
            assertTrue(new Rascunho().podeEditarItens());
        }

        @Test
        @DisplayName("aoEnviar com itens transita para Enviado")
        void aoEnviarComItensTransitaParaEnviado() {
            EstadoOrcamento proximo = new Rascunho().aoEnviar(1);
            assertEquals(StatusOrcamento.ENVIADO, proximo.status());
        }

        @Test
        @DisplayName("aoEnviar sem itens rejeita")
        void aoEnviarSemItensRejeita() {
            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> new Rascunho().aoEnviar(0)
            );
            assertEquals("orçamento sem itens não pode ser enviado", ex.getMessage());
        }

        @Test
        @DisplayName("aoAprovar é transição inválida")
        void aoAprovarInvalida() {
            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> new Rascunho().aoAprovar()
            );
            assertEquals("orçamento em rascunho não pode ser aprovado", ex.getMessage());
        }

        @Test
        @DisplayName("aoRejeitar é transição inválida")
        void aoRejeitarInvalida() {
            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> new Rascunho().aoRejeitar("motivo qualquer")
            );
            assertEquals("orçamento em rascunho não pode ser rejeitado", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Enviado")
    class EnviadoTest {

        @Test
        @DisplayName("status retorna ENVIADO")
        void statusEnviado() {
            assertEquals(StatusOrcamento.ENVIADO, new Enviado().status());
        }

        @Test
        @DisplayName("não permite edição de itens")
        void naoPermiteEditar() {
            assertFalse(new Enviado().podeEditarItens());
        }

        @Test
        @DisplayName("aoEnviar rejeita reenvio")
        void aoEnviarRejeitaReenvio() {
            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> new Enviado().aoEnviar(5)
            );
            assertEquals("orçamento já enviado não pode ser reenviado", ex.getMessage());
        }

        @Test
        @DisplayName("aoAprovar transita para Aprovado")
        void aoAprovarTransitaParaAprovado() {
            EstadoOrcamento proximo = new Enviado().aoAprovar();
            assertEquals(StatusOrcamento.APROVADO, proximo.status());
        }

        @Test
        @DisplayName("aoRejeitar com motivo válido transita para Rejeitado")
        void aoRejeitarComMotivoValido() {
            EstadoOrcamento proximo = new Enviado().aoRejeitar("Cliente desistiu");
            assertEquals(StatusOrcamento.REJEITADO, proximo.status());
            assertEquals("Cliente desistiu", ((Rejeitado) proximo).motivo());
        }

        @Test
        @DisplayName("aoRejeitar rejeita motivo nulo")
        void aoRejeitarRejeitaMotivoNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Enviado().aoRejeitar(null)
            );
            assertEquals("motivo de rejeição não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("aoRejeitar rejeita motivo vazio")
        void aoRejeitarRejeitaMotivoVazio() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Enviado().aoRejeitar("    ")
            );
            assertEquals("motivo de rejeição não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("aoRejeitar rejeita motivo com menos de 3 caracteres após trim")
        void aoRejeitarRejeitaMotivoCurto() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Enviado().aoRejeitar(" AB ")
            );
            assertEquals("motivo de rejeição deve ter ao menos 3 caracteres", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Aprovado")
    class AprovadoTest {

        @Test
        @DisplayName("status retorna APROVADO")
        void statusAprovado() {
            assertEquals(StatusOrcamento.APROVADO, new Aprovado().status());
        }

        @Test
        @DisplayName("não permite edição de itens")
        void naoPermiteEditar() {
            assertFalse(new Aprovado().podeEditarItens());
        }

        @Test
        @DisplayName("aoEnviar é transição inválida (estado terminal)")
        void aoEnviarInvalida() {
            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> new Aprovado().aoEnviar(1)
            );
            assertEquals("orçamento aprovado é estado terminal", ex.getMessage());
        }

        @Test
        @DisplayName("aoAprovar é transição inválida (estado terminal)")
        void aoAprovarInvalida() {
            assertThrows(IllegalStateException.class, () -> new Aprovado().aoAprovar());
        }

        @Test
        @DisplayName("aoRejeitar é transição inválida (estado terminal)")
        void aoRejeitarInvalida() {
            assertThrows(IllegalStateException.class, () -> new Aprovado().aoRejeitar("motivo"));
        }
    }

    @Nested
    @DisplayName("Rejeitado")
    class RejeitadoTest {

        @Test
        @DisplayName("status retorna REJEITADO")
        void statusRejeitado() {
            assertEquals(StatusOrcamento.REJEITADO, new Rejeitado("Cliente desistiu").status());
        }

        @Test
        @DisplayName("não permite edição de itens")
        void naoPermiteEditar() {
            assertFalse(new Rejeitado("Cliente desistiu").podeEditarItens());
        }

        @Test
        @DisplayName("motivo é armazenado após trim")
        void motivoComTrim() {
            Rejeitado r = new Rejeitado("   Preço acima do esperado   ");
            assertEquals("Preço acima do esperado", r.motivo());
        }

        @Test
        @DisplayName("construtor rejeita motivo nulo")
        void construtorRejeitaMotivoNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Rejeitado(null)
            );
            assertEquals("motivo de rejeição não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("construtor rejeita motivo vazio ou só espaços")
        void construtorRejeitaMotivoVazio() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Rejeitado("   ")
            );
            assertEquals("motivo de rejeição não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("construtor rejeita motivo com menos de 3 caracteres após trim")
        void construtorRejeitaMotivoCurto() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Rejeitado("AB")
            );
            assertEquals("motivo de rejeição deve ter ao menos 3 caracteres", ex.getMessage());
        }

        @Test
        @DisplayName("aoEnviar é transição inválida (estado terminal)")
        void aoEnviarInvalida() {
            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> new Rejeitado("Cliente desistiu").aoEnviar(1)
            );
            assertEquals("orçamento rejeitado é estado terminal", ex.getMessage());
        }

        @Test
        @DisplayName("aoAprovar é transição inválida (estado terminal)")
        void aoAprovarInvalida() {
            assertThrows(IllegalStateException.class, () -> new Rejeitado("Cliente desistiu").aoAprovar());
        }

        @Test
        @DisplayName("aoRejeitar é transição inválida (estado terminal)")
        void aoRejeitarInvalida() {
            assertThrows(
                IllegalStateException.class,
                () -> new Rejeitado("Cliente desistiu").aoRejeitar("outro motivo")
            );
        }
    }
}
