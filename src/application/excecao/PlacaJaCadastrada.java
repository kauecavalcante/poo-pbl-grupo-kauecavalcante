package application.excecao;

import domain.veiculo.Placa;

public class PlacaJaCadastrada extends RuntimeException {

    public PlacaJaCadastrada(Placa placa) {
        super("placa já cadastrada: " + placa.formatada());
    }
}
