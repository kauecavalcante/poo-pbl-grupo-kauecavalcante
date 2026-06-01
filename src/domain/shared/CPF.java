package domain.shared;

public final class CPF {

    private static final int TAMANHO = 11;

    private final String numero;

    private CPF(String numero) {
        this.numero = numero;
    }

    public static CPF de(String entrada) {
        if (entrada == null) {
            throw new IllegalArgumentException("cpf não pode ser nulo");
        }
        String limpo = entrada.replace(".", "").replace("-", "").replace(" ", "");
        if (limpo.length() != TAMANHO || !contemSomenteDigitos(limpo)) {
            throw new IllegalArgumentException("cpf deve ter 11 dígitos");
        }
        if (todosDigitosIguais(limpo) || !digitosVerificadoresCorretos(limpo)) {
            throw new IllegalArgumentException("cpf inválido");
        }
        return new CPF(limpo);
    }

    public String numero() {
        return numero;
    }

    private static boolean contemSomenteDigitos(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean todosDigitosIguais(String s) {
        char primeiro = s.charAt(0);
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) != primeiro) {
                return false;
            }
        }
        return true;
    }

    private static boolean digitosVerificadoresCorretos(String s) {
        int dv1Esperado = calcularDigitoVerificador(s, 9, 10);
        if (dv1Esperado != Character.digit(s.charAt(9), 10)) {
            return false;
        }
        int dv2Esperado = calcularDigitoVerificador(s, 10, 11);
        return dv2Esperado == Character.digit(s.charAt(10), 10);
    }

    private static int calcularDigitoVerificador(String s, int quantidade, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += Character.digit(s.charAt(i), 10) * (pesoInicial - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
