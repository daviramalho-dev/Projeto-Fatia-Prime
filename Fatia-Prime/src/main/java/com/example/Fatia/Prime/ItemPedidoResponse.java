package com.example.Fatia.Prime;

import java.math.BigDecimal;

public record ItemPedidoResponse(
    Long id,
    Long produtoId,
    String nomeProduto,
    Integer quantidade,
    BigDecimal precoUnitario
) {
    public static ItemPedidoResponse de(ItemPedido item) {
        return new ItemPedidoResponse(
            item.getId(),
            item.getProduto() != null ? item.getProduto().getId() : null,
            item.getProduto() != null ? item.getProduto().getNome() : null,
            item.getQuantidade(),
            item.getPrecoUnitario()
        );
    }
}
