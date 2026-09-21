package com.example.Fatia.Prime;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PedidoRequest(
    Long usuarioId,
    String clienteNome,
    String clienteEmail,
    String clienteTelefone,
    String endereco,
    String observacoes,
    @NotEmpty(message = "O pedido deve conter pelo menos um item")
    @Valid
    List<ItemPedidoRequest> itens
) {
}
