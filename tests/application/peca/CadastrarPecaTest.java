package application.peca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.excecao.CodigoDePecaJaCadastrado;
import domain.peca.Peca;
import domain.peca.PecaId;
import domain.peca.PecaRepository;
import domain.shared.Preco;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CadastrarPecaTest {

    private static final Preco PRECO = Preco.deReais("199.90");

    @Mock
    private PecaRepository pecaRepository;

    @Test
    @DisplayName("cadastro feliz salva peça e retorna id gerado")
    void cadastroFeliz() {
        when(pecaRepository.buscarPorCodigo("FLT-1001")).thenReturn(Optional.empty());

        CadastrarPeca sut = new CadastrarPeca(pecaRepository);
        PecaId id = sut.executar("FLT-1001", "Filtro de óleo", PRECO, 10);

        assertNotNull(id);
        ArgumentCaptor<Peca> captor = ArgumentCaptor.forClass(Peca.class);
        verify(pecaRepository).salvar(captor.capture());
        Peca salva = captor.getValue();
        assertEquals(id, salva.id());
        assertEquals("FLT-1001", salva.codigo());
        assertEquals("Filtro de óleo", salva.descricao());
        assertEquals(PRECO, salva.preco());
        assertEquals(10, salva.estoque());
    }

    @Test
    @DisplayName("código já cadastrado lança CodigoDePecaJaCadastrado e não chama salvar")
    void codigoJaCadastrado() {
        Peca existente = Peca.nova("FLT-1001", "Filtro original", PRECO, 5);
        when(pecaRepository.buscarPorCodigo("FLT-1001")).thenReturn(Optional.of(existente));

        CadastrarPeca sut = new CadastrarPeca(pecaRepository);
        CodigoDePecaJaCadastrado ex = assertThrows(
            CodigoDePecaJaCadastrado.class,
            () -> sut.executar("FLT-1001", "Filtro de óleo", PRECO, 10)
        );
        assertEquals("código de peça já cadastrado: FLT-1001", ex.getMessage());
        verify(pecaRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("verificação de duplicidade usa o código após trim")
    void verificacaoUsaCodigoComTrim() {
        Peca existente = Peca.nova("FLT-1001", "Filtro original", PRECO, 5);
        when(pecaRepository.buscarPorCodigo("FLT-1001")).thenReturn(Optional.of(existente));

        CadastrarPeca sut = new CadastrarPeca(pecaRepository);
        assertThrows(
            CodigoDePecaJaCadastrado.class,
            () -> sut.executar("  FLT-1001  ", "Filtro de óleo", PRECO, 10)
        );
        verify(pecaRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("construtor rejeita repositório nulo")
    void construtorRejeitaRepoNulo() {
        NullPointerException ex = assertThrows(
            NullPointerException.class,
            () -> new CadastrarPeca(null)
        );
        assertEquals("pecaRepository", ex.getMessage());
    }

    @Test
    @DisplayName("código nulo propaga IllegalArgumentException do domínio sem chamar salvar")
    void codigoNuloPropagaExcecaoDeDominio() {
        CadastrarPeca sut = new CadastrarPeca(pecaRepository);
        assertThrows(
            IllegalArgumentException.class,
            () -> sut.executar(null, "Filtro de óleo", PRECO, 10)
        );
        verify(pecaRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("preço nulo propaga IllegalArgumentException do domínio")
    void precoNuloPropagaExcecaoDeDominio() {
        when(pecaRepository.buscarPorCodigo("FLT-1001")).thenReturn(Optional.empty());

        CadastrarPeca sut = new CadastrarPeca(pecaRepository);
        assertThrows(
            IllegalArgumentException.class,
            () -> sut.executar("FLT-1001", "Filtro de óleo", null, 10)
        );
        verify(pecaRepository, never()).salvar(any());
    }

    @Test
    @DisplayName("estoque negativo propaga IllegalArgumentException do domínio")
    void estoqueNegativoPropagaExcecaoDeDominio() {
        when(pecaRepository.buscarPorCodigo("FLT-1001")).thenReturn(Optional.empty());

        CadastrarPeca sut = new CadastrarPeca(pecaRepository);
        assertThrows(
            IllegalArgumentException.class,
            () -> sut.executar("FLT-1001", "Filtro de óleo", PRECO, -1)
        );
        verify(pecaRepository, never()).salvar(any());
    }
}
