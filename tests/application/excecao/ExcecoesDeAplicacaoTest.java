package application.excecao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.cliente.ClienteId;
import domain.orcamento.OrcamentoId;
import domain.ordemservico.OrdemDeServicoId;
import domain.peca.PecaId;
import domain.shared.CPF;
import domain.veiculo.Placa;
import domain.veiculo.VeiculoId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ExcecoesDeAplicacaoTest {

    @Nested
    @DisplayName("ClienteNaoEncontrado")
    class ClienteNaoEncontradoTest {

        @Test
        @DisplayName("mensagem contém o id informado")
        void mensagemContemId() {
            ClienteId id = ClienteId.novo();
            ClienteNaoEncontrado ex = new ClienteNaoEncontrado(id);
            assertTrue(ex.getMessage().contains(id.toString()));
        }

        @Test
        @DisplayName("é RuntimeException (unchecked)")
        void ehRuntimeException() {
            assertTrue(new ClienteNaoEncontrado(ClienteId.novo()) instanceof RuntimeException);
        }
    }

    @Nested
    @DisplayName("VeiculoNaoEncontrado")
    class VeiculoNaoEncontradoTest {

        @Test
        @DisplayName("mensagem contém o id informado")
        void mensagemContemId() {
            VeiculoId id = VeiculoId.novo();
            assertTrue(new VeiculoNaoEncontrado(id).getMessage().contains(id.toString()));
        }

        @Test
        @DisplayName("é RuntimeException")
        void ehRuntimeException() {
            assertTrue(new VeiculoNaoEncontrado(VeiculoId.novo()) instanceof RuntimeException);
        }
    }

    @Nested
    @DisplayName("PecaNaoEncontrada")
    class PecaNaoEncontradaTest {

        @Test
        @DisplayName("mensagem contém o id informado")
        void mensagemContemId() {
            PecaId id = PecaId.novo();
            assertTrue(new PecaNaoEncontrada(id).getMessage().contains(id.toString()));
        }

        @Test
        @DisplayName("é RuntimeException")
        void ehRuntimeException() {
            assertTrue(new PecaNaoEncontrada(PecaId.novo()) instanceof RuntimeException);
        }
    }

    @Nested
    @DisplayName("OrcamentoNaoEncontrado")
    class OrcamentoNaoEncontradoTest {

        @Test
        @DisplayName("mensagem contém o id informado")
        void mensagemContemId() {
            OrcamentoId id = OrcamentoId.novo();
            assertTrue(new OrcamentoNaoEncontrado(id).getMessage().contains(id.toString()));
        }

        @Test
        @DisplayName("é RuntimeException")
        void ehRuntimeException() {
            assertTrue(new OrcamentoNaoEncontrado(OrcamentoId.novo()) instanceof RuntimeException);
        }
    }

    @Nested
    @DisplayName("OrdemDeServicoNaoEncontrada")
    class OrdemDeServicoNaoEncontradaTest {

        @Test
        @DisplayName("mensagem contém o id informado")
        void mensagemContemId() {
            OrdemDeServicoId id = OrdemDeServicoId.novo();
            assertTrue(new OrdemDeServicoNaoEncontrada(id).getMessage().contains(id.toString()));
        }

        @Test
        @DisplayName("é RuntimeException")
        void ehRuntimeException() {
            assertTrue(new OrdemDeServicoNaoEncontrada(OrdemDeServicoId.novo()) instanceof RuntimeException);
        }
    }

    @Nested
    @DisplayName("CpfJaCadastrado")
    class CpfJaCadastradoTest {

        @Test
        @DisplayName("mensagem contém o CPF formatado")
        void mensagemContemCpf() {
            CPF cpf = CPF.de("529.982.247-25");
            assertTrue(new CpfJaCadastrado(cpf).getMessage().contains("529.982.247-25"));
        }

        @Test
        @DisplayName("mensagem identifica o conflito como cadastro duplicado de CPF")
        void mensagemIdentificaConflito() {
            CpfJaCadastrado ex = new CpfJaCadastrado(CPF.de("529.982.247-25"));
            assertEquals("CPF já cadastrado: 529.982.247-25", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("PlacaJaCadastrada")
    class PlacaJaCadastradaTest {

        @Test
        @DisplayName("mensagem contém a placa formatada")
        void mensagemContemPlaca() {
            Placa placa = Placa.de("ABC1234");
            assertTrue(new PlacaJaCadastrada(placa).getMessage().contains("ABC-1234"));
        }

        @Test
        @DisplayName("é RuntimeException")
        void ehRuntimeException() {
            assertTrue(new PlacaJaCadastrada(Placa.de("ABC1234")) instanceof RuntimeException);
        }
    }

    @Nested
    @DisplayName("CodigoDePecaJaCadastrado")
    class CodigoDePecaJaCadastradoTest {

        @Test
        @DisplayName("mensagem contém o código informado")
        void mensagemContemCodigo() {
            assertTrue(new CodigoDePecaJaCadastrado("FLT-1001").getMessage().contains("FLT-1001"));
        }

        @Test
        @DisplayName("é RuntimeException")
        void ehRuntimeException() {
            assertTrue(new CodigoDePecaJaCadastrado("FLT-1001") instanceof RuntimeException);
        }
    }
}
