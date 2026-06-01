package domain.veiculo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import domain.cliente.ClienteId;
import java.time.Year;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class VeiculoTest {

    private static final Placa PLACA = Placa.de("ABC1234");
    private static final ClienteId DONO = ClienteId.novo();

    @Nested
    @DisplayName("Criação de novo veículo")
    class Criacao {

        @Test
        @DisplayName("novo gera identidade")
        void novoGeraIdentidade() {
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "Uno", 2010, DONO);
            assertNotNull(v.id());
        }

        @Test
        @DisplayName("novo expõe os dados informados nos acessores")
        void acessoresExpoeDados() {
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "Uno", 2010, DONO);
            assertSame(PLACA, v.placa());
            assertEquals("Fiat", v.marca());
            assertEquals("Uno", v.modelo());
            assertEquals(2010, v.ano());
            assertSame(DONO, v.dono());
        }

        @Test
        @DisplayName("marca é armazenada com trim")
        void marcaArmazenadaComTrim() {
            Veiculo v = Veiculo.novo(PLACA, "   Fiat   ", "Uno", 2010, DONO);
            assertEquals("Fiat", v.marca());
        }

        @Test
        @DisplayName("modelo é armazenado com trim")
        void modeloArmazenadoComTrim() {
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "   Uno   ", 2010, DONO);
            assertEquals("Uno", v.modelo());
        }

        @Test
        @DisplayName("dois veículos novos recebem IDs distintos")
        void novoGeraIdsDistintos() {
            Veiculo a = Veiculo.novo(PLACA, "Fiat", "Uno", 2010, DONO);
            Veiculo b = Veiculo.novo(Placa.de("XYZ4321"), "Fiat", "Uno", 2010, DONO);
            assertEquals(false, a.id().equals(b.id()));
        }
    }

    @Nested
    @DisplayName("Invariantes do veículo")
    class Invariantes {

        @Test
        @DisplayName("rejeita placa nula")
        void rejeitaPlacaNula() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Veiculo.novo(null, "Fiat", "Uno", 2010, DONO)
            );
            assertEquals("placa não pode ser nula", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita dono nulo")
        void rejeitaDonoNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Veiculo.novo(PLACA, "Fiat", "Uno", 2010, null)
            );
            assertEquals("dono do veículo não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita marca nula")
        void rejeitaMarcaNula() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Veiculo.novo(PLACA, null, "Uno", 2010, DONO)
            );
            assertEquals("marca não pode ser vazia", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita marca vazia ou só espaços")
        void rejeitaMarcaVazia() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Veiculo.novo(PLACA, "   ", "Uno", 2010, DONO)
            );
            assertEquals("marca não pode ser vazia", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita marca com menos de 2 caracteres após trim")
        void rejeitaMarcaCurta() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Veiculo.novo(PLACA, "  A  ", "Uno", 2010, DONO)
            );
            assertEquals("marca deve ter ao menos 2 caracteres", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita modelo nulo")
        void rejeitaModeloNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Veiculo.novo(PLACA, "Fiat", null, 2010, DONO)
            );
            assertEquals("modelo não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita modelo vazio ou só espaços")
        void rejeitaModeloVazio() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Veiculo.novo(PLACA, "Fiat", "   ", 2010, DONO)
            );
            assertEquals("modelo não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("rejeita modelo com menos de 2 caracteres após trim")
        void rejeitaModeloCurto() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Veiculo.novo(PLACA, "Fiat", "X", 2010, DONO)
            );
            assertEquals("modelo deve ter ao menos 2 caracteres", ex.getMessage());
        }

        @Test
        @DisplayName("aceita ano 1900 como limite inferior")
        void aceitaAnoLimiteInferior() {
            Veiculo v = Veiculo.novo(PLACA, "Ford", "Modelo T", 1900, DONO);
            assertEquals(1900, v.ano());
        }

        @Test
        @DisplayName("rejeita ano 1899")
        void rejeitaAnoAbaixoDoMinimo() {
            int anoMaximo = Year.now().getValue() + 1;
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Veiculo.novo(PLACA, "Ford", "Modelo T", 1899, DONO)
            );
            assertEquals("ano do veículo deve ser entre 1900 e " + anoMaximo, ex.getMessage());
        }

        @Test
        @DisplayName("aceita ano atual mais um como limite superior")
        void aceitaAnoLimiteSuperior() {
            int anoMaximo = Year.now().getValue() + 1;
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "Uno", anoMaximo, DONO);
            assertEquals(anoMaximo, v.ano());
        }

        @Test
        @DisplayName("rejeita ano dois acima do ano atual")
        void rejeitaAnoAcimaDoMaximo() {
            int anoFutura = Year.now().getValue() + 2;
            int anoMaximo = Year.now().getValue() + 1;
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Veiculo.novo(PLACA, "Fiat", "Uno", anoFutura, DONO)
            );
            assertTrue(ex.getMessage().contains(String.valueOf(anoMaximo)));
        }
    }

    @Nested
    @DisplayName("Reconstituição a partir de identidade existente")
    class Reconstituicao {

        @Test
        @DisplayName("reconstituir preserva o ID informado")
        void reconstituirPreservaId() {
            VeiculoId id = VeiculoId.novo();
            Veiculo v = Veiculo.reconstituir(id, PLACA, "Fiat", "Uno", 2010, DONO);
            assertEquals(id, v.id());
        }

        @Test
        @DisplayName("reconstituir rejeita ID nulo")
        void reconstituirRejeitaIdNulo() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Veiculo.reconstituir(null, PLACA, "Fiat", "Uno", 2010, DONO)
            );
            assertEquals("id do veículo não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("reconstituir aplica as mesmas validações de marca")
        void reconstituirValidaMarca() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Veiculo.reconstituir(VeiculoId.novo(), PLACA, "", "Uno", 2010, DONO)
            );
            assertEquals("marca não pode ser vazia", ex.getMessage());
        }

        @Test
        @DisplayName("reconstituir aplica as mesmas validações de ano")
        void reconstituirValidaAno() {
            assertThrows(
                IllegalArgumentException.class,
                () -> Veiculo.reconstituir(VeiculoId.novo(), PLACA, "Fiat", "Uno", 1850, DONO)
            );
        }
    }

    @Nested
    @DisplayName("Igualdade por identidade")
    class Igualdade {

        @Test
        @DisplayName("dois veículos com mesmo ID são iguais mesmo com dados divergentes")
        void iguaisQuandoMesmoIdAindaQueDadosMudem() {
            VeiculoId id = VeiculoId.novo();
            Veiculo antes = Veiculo.reconstituir(id, PLACA, "Fiat", "Uno", 2010, DONO);
            Veiculo depois = Veiculo.reconstituir(id, PLACA, "Volkswagen", "Gol", 2015, ClienteId.novo());
            assertEquals(antes, depois);
            assertEquals(antes.hashCode(), depois.hashCode());
        }

        @Test
        @DisplayName("dois veículos com IDs diferentes não são iguais mesmo com dados idênticos")
        void distintosQuandoIdsDiferentes() {
            Veiculo a = Veiculo.reconstituir(VeiculoId.novo(), PLACA, "Fiat", "Uno", 2010, DONO);
            Veiculo b = Veiculo.reconstituir(VeiculoId.novo(), PLACA, "Fiat", "Uno", 2010, DONO);
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("equals com null retorna false")
        void equalsComNuloRetornaFalso() {
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "Uno", 2010, DONO);
            assertFalse(v.equals(null));
        }

        @Test
        @DisplayName("equals com tipo diferente retorna false")
        void equalsComOutroTipoRetornaFalso() {
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "Uno", 2010, DONO);
            assertFalse(v.equals(v.id()));
        }

        @Test
        @DisplayName("a instância é igual a si mesma")
        void reflexividade() {
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "Uno", 2010, DONO);
            assertTrue(v.equals(v));
        }
    }

    @Nested
    @DisplayName("Mutações controladas")
    class Mutacoes {

        @Test
        @DisplayName("alterarMarca substitui o valor preservando ID e placa")
        void alterarMarcaPreservaIdentidade() {
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "Uno", 2010, DONO);
            VeiculoId idAntes = v.id();
            v.alterarMarca("Volkswagen");
            assertEquals("Volkswagen", v.marca());
            assertSame(idAntes, v.id());
            assertSame(PLACA, v.placa());
        }

        @Test
        @DisplayName("alterarMarca aplica trim")
        void alterarMarcaAplicaTrim() {
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "Uno", 2010, DONO);
            v.alterarMarca("   Volkswagen   ");
            assertEquals("Volkswagen", v.marca());
        }

        @Test
        @DisplayName("alterarMarca rejeita valor nulo")
        void alterarMarcaRejeitaNulo() {
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "Uno", 2010, DONO);
            assertThrows(IllegalArgumentException.class, () -> v.alterarMarca(null));
        }

        @Test
        @DisplayName("alterarMarca rejeita valor curto")
        void alterarMarcaRejeitaCurto() {
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "Uno", 2010, DONO);
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> v.alterarMarca("A")
            );
            assertEquals("marca deve ter ao menos 2 caracteres", ex.getMessage());
        }

        @Test
        @DisplayName("alterarModelo substitui o valor preservando identidade")
        void alterarModeloPreservaIdentidade() {
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "Uno", 2010, DONO);
            VeiculoId idAntes = v.id();
            v.alterarModelo("Palio");
            assertEquals("Palio", v.modelo());
            assertSame(idAntes, v.id());
        }

        @Test
        @DisplayName("alterarModelo rejeita valor vazio")
        void alterarModeloRejeitaVazio() {
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "Uno", 2010, DONO);
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> v.alterarModelo("   ")
            );
            assertEquals("modelo não pode ser vazio", ex.getMessage());
        }

        @Test
        @DisplayName("alterarAno substitui o valor preservando identidade")
        void alterarAnoPreservaIdentidade() {
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "Uno", 2010, DONO);
            VeiculoId idAntes = v.id();
            v.alterarAno(2015);
            assertEquals(2015, v.ano());
            assertSame(idAntes, v.id());
        }

        @Test
        @DisplayName("alterarAno rejeita ano fora do intervalo")
        void alterarAnoRejeitaForaDoIntervalo() {
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "Uno", 2010, DONO);
            assertThrows(IllegalArgumentException.class, () -> v.alterarAno(1800));
            assertThrows(IllegalArgumentException.class, () -> v.alterarAno(Year.now().getValue() + 5));
        }

        @Test
        @DisplayName("alteração falha não modifica o estado do veículo")
        void alteracaoFalhaNaoModificaEstado() {
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "Uno", 2010, DONO);
            assertThrows(IllegalArgumentException.class, () -> v.alterarMarca("X"));
            assertThrows(IllegalArgumentException.class, () -> v.alterarModelo(""));
            assertThrows(IllegalArgumentException.class, () -> v.alterarAno(1500));
            assertEquals("Fiat", v.marca());
            assertEquals("Uno", v.modelo());
            assertEquals(2010, v.ano());
        }
    }

    @Nested
    @DisplayName("Transferência de propriedade")
    class Transferencia {

        @Test
        @DisplayName("transferirPara substitui o dono preservando ID e placa")
        void transferirPreservaIdentidade() {
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "Uno", 2010, DONO);
            VeiculoId idAntes = v.id();
            ClienteId novoDono = ClienteId.novo();
            v.transferirPara(novoDono);
            assertEquals(novoDono, v.dono());
            assertSame(idAntes, v.id());
            assertSame(PLACA, v.placa());
        }

        @Test
        @DisplayName("transferirPara rejeita destino nulo")
        void transferirRejeitaNulo() {
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "Uno", 2010, DONO);
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> v.transferirPara(null)
            );
            assertEquals("dono do veículo não pode ser nulo", ex.getMessage());
        }

        @Test
        @DisplayName("transferirPara rejeita o mesmo dono atual")
        void transferirRejeitaMesmoDono() {
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "Uno", 2010, DONO);
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> v.transferirPara(DONO)
            );
            assertEquals("veículo já pertence a esse cliente", ex.getMessage());
        }

        @Test
        @DisplayName("transferência falha não modifica o dono")
        void transferenciaFalhaPreservaDonoAtual() {
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "Uno", 2010, DONO);
            assertThrows(IllegalArgumentException.class, () -> v.transferirPara(null));
            assertThrows(IllegalArgumentException.class, () -> v.transferirPara(DONO));
            assertSame(DONO, v.dono());
        }

        @Test
        @DisplayName("transferências sucessivas para donos distintos atualizam o estado")
        void transferenciasSucessivas() {
            Veiculo v = Veiculo.novo(PLACA, "Fiat", "Uno", 2010, DONO);
            ClienteId segundo = ClienteId.novo();
            ClienteId terceiro = ClienteId.novo();
            v.transferirPara(segundo);
            v.transferirPara(terceiro);
            assertEquals(terceiro, v.dono());
        }
    }
}
