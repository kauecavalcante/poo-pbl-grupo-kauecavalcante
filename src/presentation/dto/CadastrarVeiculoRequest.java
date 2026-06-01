package presentation.dto;

public record CadastrarVeiculoRequest(String placa, String marca, String modelo, int ano, String donoId) {}
