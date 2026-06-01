package domain.ordemservico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import domain.cliente.ClienteId;
import domain.veiculo.VeiculoId;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OrdemDeServicoTest {

    private static final ClienteId CLIENTE_ID = ClienteId.novo();
    private static final VeiculoId VEICULO_ID = VeiculoId.novo();

    @Nested
    @DisplayName("Abertura de OS")
    class Abertura {

        @Test
        @DisplayName("abrir gera identidade")
        void abrirGeraIdentidade() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            assertNotNull(os.id());
        }

        @Test
        @DisplayName("abrir expõe cliente e veículo informados")
        void abrirExpoeClienteEVeiculo() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            assertSame(CLIENTE_ID, os.clienteId());
            assertSame(VEICULO_ID, os.veiculoId());
        }

        @Test
        @DisplayName("abrir começa no estado RECEBIDA")
        void abrirComecaEmRecebida() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            assertEquals(StatusOS.RECEBIDA, os.status());
        }

        @Test
        @DisplayName("abrir registra a data de hoje como dataAbertura")
        void abrirRegistraDataAbertura() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            assertEquals(LocalDate.now(), os.dataAbertura());
        }

        @Test
        @DisplayName("abrir começa sem orçamento, diagnóstico, conclusão ou entrega")
        void abrirComecaSemDadosOpcionais() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            assertEquals(Optional.empty(), os.orcamentoId());
            assertEquals(Optional.empty(), os.diagnostico());
            assertEquals(Optional.empty(), os.dataConclusao());
            assertEquals(Optional.empty(), os.dataEntrega());
            assertEquals(Optional.empty(), os.motivoRejeicao());
            assertEquals(Optional.empty(), os.motivoCancelamento());
        }

        @Test
        @DisplayName("duas OS abertas recebem IDs distintos")
        void abrirGeraIdsDistintos() {
            OrdemDeServico a = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            OrdemDeServico b = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            assertEquals(false, a.id().equals(b.id()));
        }

        @Test
        @DisplayName("rejeita cliente nulo")
        void rejeitaClienteNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> OrdemDeServico.abrir(null, VEICULO_ID)
            );
            assertEquals("cliente da OS não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita veículo nulo")
        void rejeitaVeiculoNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> OrdemDeServico.abrir(CLIENTE_ID, null)
            );
            assertEquals("veículo da OS não pode ser nulo", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Registro de diagnóstico")
    class Diagnostico {

        @Test
        @DisplayName("registrarDiagnostico armazena texto válido com trim")
        void registrarDiagnosticoArmazena() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            os.registrarDiagnostico("   Suspensão dianteira com folga   ");
            assertEquals(Optional.of("Suspensão dianteira com folga"), os.diagnostico());
        }

        @Test
        @DisplayName("registrarDiagnostico rejeita texto nulo")
        void registrarDiagnosticoRejeitaNulo() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> os.registrarDiagnostico(null)
            );
            assertEquals("diagnóstico não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("registrarDiagnostico rejeita texto vazio ou só espaços")
        void registrarDiagnosticoRejeitaVazio() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            assertThrows(IllegalArgumentException.class, () -> os.registrarDiagnostico("   "));
        }

        @Test
        @DisplayName("registrarDiagnostico rejeita texto com menos de 5 caracteres após trim")
        void registrarDiagnosticoRejeitaCurto() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> os.registrarDiagnostico("ABC")
            );
            assertEquals("diagnóstico deve ter ao menos 5 caracteres", ex.getMessage());
        }

        @Test
        @DisplayName("registrarDiagnostico falha não modifica o diagnóstico já existente")
        void registroFalhaPreservaDiagnostico() {
            OrdemDeServico os = OrdemDeServico.abrir(CLIENTE_ID, VEICULO_ID);
            os.registrarDiagnostico("Suspensão com folga");
            assertThrows(IllegalArgumentException.class, () -> os.registrarDiagnostico(""));
            assertEquals(Optional.of("Suspensão com folga"), os.diagnostico());
        }
    }
}
