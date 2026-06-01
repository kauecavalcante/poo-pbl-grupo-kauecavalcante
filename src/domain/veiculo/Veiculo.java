package domain.veiculo;

import domain.cliente.ClienteId;
import java.time.Year;
import java.util.Objects;

public final class Veiculo {

    private static final int ANO_MINIMO = 1900;

    // placa é final: no domínio, trocar placa equivale a trocar de veículo
    // — não há operação "alterar placa" sobre o mesmo agregado.
    private final VeiculoId id;
    private final Placa placa;
    private String marca;
    private String modelo;
    private int ano;
    private ClienteId dono;

    private Veiculo(VeiculoId id, Placa placa, String marca, String modelo, int ano, ClienteId dono) {
        this.id = id;
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.dono = dono;
    }

    public static Veiculo novo(Placa placa, String marca, String modelo, int ano, ClienteId dono) {
        exigirPlacaNaoNula(placa);
        exigirDonoNaoNulo(dono);
        return new Veiculo(
            VeiculoId.novo(),
            placa,
            validarMarca(marca),
            validarModelo(modelo),
            validarAno(ano),
            dono
        );
    }

    public static Veiculo reconstituir(VeiculoId id, Placa placa, String marca, String modelo, int ano, ClienteId dono) {
        if (id == null) {
            throw new IllegalArgumentException("id do veículo não pode ser nulo");
        }
        exigirPlacaNaoNula(placa);
        exigirDonoNaoNulo(dono);
        return new Veiculo(
            id,
            placa,
            validarMarca(marca),
            validarModelo(modelo),
            validarAno(ano),
            dono
        );
    }

    public VeiculoId id() {
        return id;
    }

    public Placa placa() {
        return placa;
    }

    public String marca() {
        return marca;
    }

    public String modelo() {
        return modelo;
    }

    public int ano() {
        return ano;
    }

    public ClienteId dono() {
        return dono;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Veiculo outro)) {
            return false;
        }
        return id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private static void exigirPlacaNaoNula(Placa placa) {
        if (placa == null) {
            throw new IllegalArgumentException("placa não pode ser nula");
        }
    }

    private static void exigirDonoNaoNulo(ClienteId dono) {
        if (dono == null) {
            throw new IllegalArgumentException("dono do veículo não pode ser nulo");
        }
    }

    private static String validarMarca(String marca) {
        if (marca == null || marca.trim().isEmpty()) {
            throw new IllegalArgumentException("marca não pode ser vazia");
        }
        String normalizada = marca.trim();
        if (normalizada.length() < 2) {
            throw new IllegalArgumentException("marca deve ter ao menos 2 caracteres");
        }
        return normalizada;
    }

    private static String validarModelo(String modelo) {
        if (modelo == null || modelo.trim().isEmpty()) {
            throw new IllegalArgumentException("modelo não pode ser vazio");
        }
        String normalizado = modelo.trim();
        if (normalizado.length() < 2) {
            throw new IllegalArgumentException("modelo deve ter ao menos 2 caracteres");
        }
        return normalizado;
    }

    private static int validarAno(int ano) {
        int anoMaximo = Year.now().getValue() + 1;
        if (ano < ANO_MINIMO || ano > anoMaximo) {
            throw new IllegalArgumentException("ano do veículo deve ser entre 1900 e " + anoMaximo);
        }
        return ano;
    }
}
