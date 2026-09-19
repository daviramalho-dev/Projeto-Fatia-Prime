package com.example.Fatia.Prime;

import java.math.BigDecimal;

public record AdminItemPedidoResponse(
    Long id,
    Long produtoId,
    String nomeProduto,
    Integer quantidade,
    BigDecimal precoUnitario,
    BigDecimal subtotal
) {
    public static AdminItemPedidoResponse de(ItemPedido item) {
        BigDecimal preco = item.getPrecoUnitario();
        BigDecimal subtotal = preco == null || item.getQuantidade() == null
            ? BigDecimal.ZERO
            : preco.multiply(BigDecimal.valueOf(item.getQuantidade()));
        return new AdminItemPedidoResponse(
            item.getId(),
            item.getProduto() != null ? item.getProduto().getId() : null,
            item.getProduto() != null ? item.getProduto().getNome() : null,
            item.getQuantidade(),
            preco,
            subtotal
        );
    }
}
