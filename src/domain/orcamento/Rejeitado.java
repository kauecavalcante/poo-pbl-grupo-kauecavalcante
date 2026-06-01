package domain.orcamento;

public final class Rejeitado implements EstadoOrcamento {

    private final String motivo;

    public Rejeitado(String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("motivo de rejeição não pode ser vazio");
        }
        String normalizado = motivo.trim();
        if (normalizado.length() < 3) {
            throw new IllegalArgumentException("motivo de rejeição deve ter ao menos 3 caracteres");
        }
        this.motivo = normalizado;
    }

    public String motivo() {
        return motivo;
    }

    @Override
    public StatusOrcamento status() {
        return StatusOrcamento.REJEITADO;
    }

    @Override
    public boolean podeEditarItens() {
        return false;
    }

    @Override
    public EstadoOrcamento aoEnviar(int quantidadeDeItens) {
        throw new IllegalStateException("orçamento rejeitado é estado terminal");
    }

    @Override
    public EstadoOrcamento aoAprovar() {
        throw new IllegalStateException("orçamento rejeitado é estado terminal");
    }

    @Override
    public EstadoOrcamento aoRejeitar(String motivo) {
        throw new IllegalStateException("orçamento rejeitado é estado terminal");
    }
}
