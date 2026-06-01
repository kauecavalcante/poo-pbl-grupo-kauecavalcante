package application.excecao;

import domain.shared.CPF;

public class CpfJaCadastrado extends RuntimeException {

    public CpfJaCadastrado(CPF cpf) {
        super("CPF já cadastrado: " + cpf.formatado());
    }
}
