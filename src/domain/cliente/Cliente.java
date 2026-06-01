package domain.cliente;

import domain.shared.CPF;
import java.util.Objects;

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

    public static Cliente reconstituir(ClienteId id, String nome, CPF cpf, String telefone) {
        if (id == null) {
            throw new IllegalArgumentException("id do cliente não pode ser nulo");
        }
        exigirCpfNaoNulo(cpf);
        return new Cliente(id, validarNome(nome), cpf, validarTelefone(telefone));
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

    public void alterarNome(String novoNome) {
        this.nome = validarNome(novoNome);
    }

    public void alterarTelefone(String novoTelefone) {
        this.telefone = validarTelefone(novoTelefone);
    }

    // Entidade: igualdade definida exclusivamente pela identidade — duas versões
    // do mesmo cliente (ex: antes e depois de alterar o nome) devem ser tratadas
    // como o mesmo cliente em coleções e comparações de domínio.
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cliente outro)) {
            return false;
        }
        return id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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
