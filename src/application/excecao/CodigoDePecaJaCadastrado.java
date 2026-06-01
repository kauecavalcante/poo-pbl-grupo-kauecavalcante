package application.excecao;

public class CodigoDePecaJaCadastrado extends RuntimeException {

    public CodigoDePecaJaCadastrado(String codigo) {
        super("código de peça já cadastrado: " + codigo);
    }
}
