package com.example.Fatia.Prime;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PedidoRequest(
    Long usuarioId,
    String clienteNome,
    String clienteEmail,
    String clienteTelefone,
    String endereco,
    String observacoes,
    @NotNull(message = "O pedido deve conter itens")
    @Valid
    List<ItemPedidoRequest> itens
) {
}
