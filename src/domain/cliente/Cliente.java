package domain.cliente;

import domain.shared.CPF;

public final class Cliente {

    private final ClienteId id;
    private String nome;
    private final CPF cpf;
    private String telefone;

    private Cliente(ClienteId id, String nome, CPF cpf, String telefone) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
    }

    public static Cliente novo(String nome, CPF cpf, String telefone) {
        exigirCpfNaoNulo(cpf);
        return new Cliente(ClienteId.novo(), validarNome(nome), cpf, validarTelefone(telefone));
    }

    public ClienteId id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    public CPF cpf() {
        return cpf;
    }

    public String telefone() {
        return telefone;
    }

    private static void exigirCpfNaoNulo(CPF cpf) {
        if (cpf == null) {
            throw new IllegalArgumentException("cpf não pode ser nulo");
        }
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("nome do cliente não pode ser vazio");
        }
        String normalizado = nome.trim();
        if (normalizado.length() < 2) {
            throw new IllegalArgumentException("nome do cliente deve ter ao menos 2 caracteres");
        }
        return normalizado;
    }

    private static String validarTelefone(String telefone) {
        if (telefone == null || telefone.trim().isEmpty()) {
            throw new IllegalArgumentException("telefone não pode ser vazio");
        }
        String normalizado = telefone.trim();
        if (contarDigitos(normalizado) < 8) {
            throw new IllegalArgumentException("telefone deve ter ao menos 8 dígitos");
        }
        return normalizado;
    }

    private static int contarDigitos(String s) {
        int total = 0;
        for (int i = 0; i < s.length(); i++) {
            if (Character.isDigit(s.charAt(i))) {
                total++;
            }
        }
        return total;
    }
}
