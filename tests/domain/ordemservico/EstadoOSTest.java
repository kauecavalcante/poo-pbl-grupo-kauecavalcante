package domain.ordemservico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class EstadoOSTest {

    private static final String MOTIVO_VALIDO = "Cliente desistiu do serviço";

    @Nested
    @DisplayName("Recebida")
    class RecebidaTest {

        @Test
        @DisplayName("status retorna RECEBIDA")
        void statusRecebida() {
            assertEquals(StatusOS.RECEBIDA, new Recebida().status());
        }

        @Test
        @DisplayName("não é terminal")
        void naoTerminal() {
            assertFalse(new Recebida().ehTerminal());
        }

        @Test
        @DisplayName("aoEnviarParaAprovacao transita para AguardandoAprovacao")
        void aoEnviarParaAprovacaoTransita() {
            assertEquals(StatusOS.AGUARDANDO_APROVACAO, new Recebida().aoEnviarParaAprovacao().status());
        }

        @Test
        @DisplayName("aoAprovar é inválida")
        void aoAprovarInvalida() {
            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> new Recebida().aoAprovar());
            assertEquals("OS no estado RECEBIDA não pode ser aprovada", ex.getMessage());
        }

        @Test
        @DisplayName("aoRejeitar é inválida")
        void aoRejeitarInvalida() {
            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> new Recebida().aoRejeitar(MOTIVO_VALIDO)
            );
            assertEquals("OS no estado RECEBIDA não pode ser rejeitada", ex.getMessage());
        }

        @Test
        @DisplayName("aoConcluir é inválida")
        void aoConcluirInvalida() {
            assertThrows(IllegalStateException.class, () -> new Recebida().aoConcluir());
        }

        @Test
        @DisplayName("aoEntregar é inválida")
        void aoEntregarInvalida() {
            assertThrows(IllegalStateException.class, () -> new Recebida().aoEntregar());
        }

        @Test
        @DisplayName("aoCancelar com motivo válido transita para Cancelada")
        void aoCancelarTransita() {
            EstadoOS proximo = new Recebida().aoCancelar(MOTIVO_VALIDO);
            assertEquals(StatusOS.CANCELADA, proximo.status());
            assertEquals(MOTIVO_VALIDO, ((Cancelada) proximo).motivo());
        }
    }

    @Nested
    @DisplayName("AguardandoAprovacao")
    class AguardandoAprovacaoTest {

        @Test
        @DisplayName("status retorna AGUARDANDO_APROVACAO")
        void statusAguardando() {
            assertEquals(StatusOS.AGUARDANDO_APROVACAO, new AguardandoAprovacao().status());
        }

        @Test
        @DisplayName("não é terminal")
        void naoTerminal() {
            assertFalse(new AguardandoAprovacao().ehTerminal());
        }

        @Test
        @DisplayName("aoEnviarParaAprovacao é inválida (já está aguardando)")
        void aoEnviarParaAprovacaoInvalida() {
            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> new AguardandoAprovacao().aoEnviarParaAprovacao()
            );
            assertEquals("OS no estado AGUARDANDO_APROVACAO não pode ser reenviada para aprovação", ex.getMessage());
        }

        @Test
        @DisplayName("aoAprovar transita para EmExecucao")
        void aoAprovarTransita() {
            assertEquals(StatusOS.EM_EXECUCAO, new AguardandoAprovacao().aoAprovar().status());
        }

        @Test
        @DisplayName("aoRejeitar com motivo válido transita para Rejeitada")
        void aoRejeitarTransita() {
            EstadoOS proximo = new AguardandoAprovacao().aoRejeitar(MOTIVO_VALIDO);
            assertEquals(StatusOS.REJEITADA, proximo.status());
            assertEquals(MOTIVO_VALIDO, ((Rejeitada) proximo).motivo());
        }

        @Test
        @DisplayName("aoConcluir é inválida")
        void aoConcluirInvalida() {
            assertThrows(IllegalStateException.class, () -> new AguardandoAprovacao().aoConcluir());
        }

        @Test
        @DisplayName("aoEntregar é inválida")
        void aoEntregarInvalida() {
            assertThrows(IllegalStateException.class, () -> new AguardandoAprovacao().aoEntregar());
        }

        @Test
        @DisplayName("aoCancelar com motivo válido transita para Cancelada")
        void aoCancelarTransita() {
            EstadoOS proximo = new AguardandoAprovacao().aoCancelar(MOTIVO_VALIDO);
            assertEquals(StatusOS.CANCELADA, proximo.status());
        }
    }

    @Nested
    @DisplayName("EmExecucao")
    class EmExecucaoTest {

        @Test
        @DisplayName("status retorna EM_EXECUCAO")
        void statusEmExecucao() {
            assertEquals(StatusOS.EM_EXECUCAO, new EmExecucao().status());
        }

        @Test
        @DisplayName("não é terminal")
        void naoTerminal() {
            assertFalse(new EmExecucao().ehTerminal());
        }

        @Test
        @DisplayName("aoEnviarParaAprovacao é inválida")
        void aoEnviarParaAprovacaoInvalida() {
            assertThrows(IllegalStateException.class, () -> new EmExecucao().aoEnviarParaAprovacao());
        }

        @Test
        @DisplayName("aoAprovar é inválida")
        void aoAprovarInvalida() {
            assertThrows(IllegalStateException.class, () -> new EmExecucao().aoAprovar());
        }

        @Test
        @DisplayName("aoRejeitar é inválida")
        void aoRejeitarInvalida() {
            assertThrows(IllegalStateException.class, () -> new EmExecucao().aoRejeitar(MOTIVO_VALIDO));
        }

        @Test
        @DisplayName("aoConcluir transita para Concluida")
        void aoConcluirTransita() {
            assertEquals(StatusOS.CONCLUIDA, new EmExecucao().aoConcluir().status());
        }

        @Test
        @DisplayName("aoEntregar é inválida (precisa passar por Concluida primeiro)")
        void aoEntregarInvalida() {
            assertThrows(IllegalStateException.class, () -> new EmExecucao().aoEntregar());
        }

        @Test
        @DisplayName("aoCancelar transita para Cancelada")
        void aoCancelarTransita() {
            assertEquals(StatusOS.CANCELADA, new EmExecucao().aoCancelar(MOTIVO_VALIDO).status());
        }
    }

    @Nested
    @DisplayName("Concluida")
    class ConcluidaTest {

        @Test
        @DisplayName("status retorna CONCLUIDA")
        void statusConcluida() {
            assertEquals(StatusOS.CONCLUIDA, new Concluida().status());
        }

        @Test
        @DisplayName("não é terminal (ainda pode entregar ou cancelar)")
        void naoTerminal() {
            assertFalse(new Concluida().ehTerminal());
        }

        @Test
        @DisplayName("aoConcluir é inválida (não concluir duas vezes)")
        void aoConcluirInvalida() {
            assertThrows(IllegalStateException.class, () -> new Concluida().aoConcluir());
        }

        @Test
        @DisplayName("aoEntregar transita para Entregue")
        void aoEntregarTransita() {
            assertEquals(StatusOS.ENTREGUE, new Concluida().aoEntregar().status());
        }

        @Test
        @DisplayName("aoEnviarParaAprovacao é inválida")
        void aoEnviarParaAprovacaoInvalida() {
            assertThrows(IllegalStateException.class, () -> new Concluida().aoEnviarParaAprovacao());
        }

        @Test
        @DisplayName("aoAprovar é inválida")
        void aoAprovarInvalida() {
            assertThrows(IllegalStateException.class, () -> new Concluida().aoAprovar());
        }

        @Test
        @DisplayName("aoRejeitar é inválida")
        void aoRejeitarInvalida() {
            assertThrows(IllegalStateException.class, () -> new Concluida().aoRejeitar(MOTIVO_VALIDO));
        }

        @Test
        @DisplayName("aoCancelar ainda é permitido em Concluida")
        void aoCancelarPermitido() {
            assertEquals(StatusOS.CANCELADA, new Concluida().aoCancelar(MOTIVO_VALIDO).status());
        }
    }

    @Nested
    @DisplayName("Entregue (terminal)")
    class EntregueTest {

        @Test
        @DisplayName("status retorna ENTREGUE")
        void statusEntregue() {
            assertEquals(StatusOS.ENTREGUE, new Entregue().status());
        }

        @Test
        @DisplayName("é terminal")
        void terminal() {
            assertTrue(new Entregue().ehTerminal());
        }

        @Test
        @DisplayName("todas as transições são rejeitadas como estado terminal")
        void todasTransicoesRejeitadas() {
            Entregue e = new Entregue();
            String esperado = "OS no estado ENTREGUE é estado terminal";
            assertEquals(esperado, assertThrows(IllegalStateException.class, e::aoEnviarParaAprovacao).getMessage());
            assertEquals(esperado, assertThrows(IllegalStateException.class, e::aoAprovar).getMessage());
            assertEquals(esperado, assertThrows(IllegalStateException.class, () -> e.aoRejeitar(MOTIVO_VALIDO)).getMessage());
            assertEquals(esperado, assertThrows(IllegalStateException.class, e::aoConcluir).getMessage());
            assertEquals(esperado, assertThrows(IllegalStateException.class, e::aoEntregar).getMessage());
            assertEquals(esperado, assertThrows(IllegalStateException.class, () -> e.aoCancelar(MOTIVO_VALIDO)).getMessage());
        }
    }

    @Nested
    @DisplayName("Rejeitada (terminal)")
    class RejeitadaTest {

        @Test
        @DisplayName("status retorna REJEITADA")
        void statusRejeitada() {
            assertEquals(StatusOS.REJEITADA, new Rejeitada(MOTIVO_VALIDO).status());
        }

        @Test
        @DisplayName("é terminal")
        void terminal() {
            assertTrue(new Rejeitada(MOTIVO_VALIDO).ehTerminal());
        }

        @Test
        @DisplayName("motivo é armazenado após trim")
        void motivoComTrim() {
            assertEquals("Preço acima do esperado", new Rejeitada("   Preço acima do esperado   ").motivo());
        }

        @Test
        @DisplayName("rejeita motivo nulo")
        void rejeitaMotivoNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Rejeitada(null)
            );
            assertEquals("motivo de rejeição não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita motivo vazio")
        void rejeitaMotivoVazio() {
            assertThrows(IllegalArgumentException.class, () -> new Rejeitada("   "));
        }

        @Test
        @DisplayName("rejeita motivo curto")
        void rejeitaMotivoCurto() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Rejeitada("AB")
            );
            assertEquals("motivo de rejeição deve ter ao menos 3 caracteres", ex.getMessage());
        }

        @Test
        @DisplayName("todas as transições são rejeitadas como estado terminal")
        void todasTransicoesRejeitadas() {
            Rejeitada r = new Rejeitada(MOTIVO_VALIDO);
            assertThrows(IllegalStateException.class, r::aoEnviarParaAprovacao);
            assertThrows(IllegalStateException.class, r::aoAprovar);
            assertThrows(IllegalStateException.class, () -> r.aoRejeitar(MOTIVO_VALIDO));
            assertThrows(IllegalStateException.class, r::aoConcluir);
            assertThrows(IllegalStateException.class, r::aoEntregar);
            assertThrows(IllegalStateException.class, () -> r.aoCancelar(MOTIVO_VALIDO));
        }
    }

    @Nested
    @DisplayName("Cancelada (terminal)")
    class CanceladaTest {

        @Test
        @DisplayName("status retorna CANCELADA")
        void statusCancelada() {
            assertEquals(StatusOS.CANCELADA, new Cancelada(MOTIVO_VALIDO).status());
        }

        @Test
        @DisplayName("é terminal")
        void terminal() {
            assertTrue(new Cancelada(MOTIVO_VALIDO).ehTerminal());
        }

        @Test
        @DisplayName("motivo é armazenado após trim")
        void motivoComTrim() {
            assertEquals("Cliente desistiu", new Cancelada("   Cliente desistiu   ").motivo());
        }

        @Test
        @DisplayName("rejeita motivo nulo")
        void rejeitaMotivoNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Cancelada(null)
            );
            assertEquals("motivo de cancelamento não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita motivo vazio")
        void rejeitaMotivoVazio() {
            assertThrows(IllegalArgumentException.class, () -> new Cancelada("   "));
        }

        @Test
        @DisplayName("rejeita motivo curto")
        void rejeitaMotivoCurto() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Cancelada("AB")
            );
            assertEquals("motivo de cancelamento deve ter ao menos 3 caracteres", ex.getMessage());
        }

        @Test
        @DisplayName("todas as transições são rejeitadas como estado terminal")
        void todasTransicoesRejeitadas() {
            Cancelada c = new Cancelada(MOTIVO_VALIDO);
            assertThrows(IllegalStateException.class, c::aoEnviarParaAprovacao);
            assertThrows(IllegalStateException.class, c::aoAprovar);
            assertThrows(IllegalStateException.class, () -> c.aoRejeitar(MOTIVO_VALIDO));
            assertThrows(IllegalStateException.class, c::aoConcluir);
            assertThrows(IllegalStateException.class, c::aoEntregar);
            assertThrows(IllegalStateException.class, () -> c.aoCancelar(MOTIVO_VALIDO));
        }
    }
}
