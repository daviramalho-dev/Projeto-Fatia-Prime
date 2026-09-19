package com.example.Fatia.Prime;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PedidoRequest(
    @NotNull(message = "O usuário é obrigatório")
    Long usuarioId,
    String observacoes,
    @NotNull(message = "O pedido deve conter itens")
    @Valid
    List<ItemPedidoRequest> itens
) {
}
