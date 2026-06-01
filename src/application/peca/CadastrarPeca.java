package application.peca;

import application.excecao.CodigoDePecaJaCadastrado;
import domain.peca.Peca;
import domain.peca.PecaId;
import domain.peca.PecaRepository;
import domain.shared.Preco;
import java.util.Objects;

public final class CadastrarPeca {

    private final PecaRepository pecaRepository;

    public CadastrarPeca(PecaRepository pecaRepository) {
        this.pecaRepository = Objects.requireNonNull(pecaRepository, "pecaRepository");
    }

    public PecaId executar(String codigo, String descricao, Preco preco, int estoqueInicial) {
        if (codigo != null) {
            pecaRepository.buscarPorCodigo(codigo.trim()).ifPresent(existente -> {
                throw new CodigoDePecaJaCadastrado(codigo.trim());
            });
        }
        Peca peca = Peca.nova(codigo, descricao, preco, estoqueInicial);
        pecaRepository.salvar(peca);
        return peca.id();
    }
}
