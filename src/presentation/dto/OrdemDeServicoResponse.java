package presentation.dto;

public record OrdemDeServicoResponse(
    String id,
    String clienteId,
    String veiculoId,
    String status,
    String orcamentoId,
    String diagnostico,
    String motivoRejeicao,
    String motivoCancelamento,
    String dataAbertura,
    String dataConclusao,
    String dataEntrega
) {}
