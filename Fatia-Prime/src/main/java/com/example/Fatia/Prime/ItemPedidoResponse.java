package com.example.Fatia.Prime;

import java.math.BigDecimal;
import java.util.List;

public record ItemPedidoResponse(
    Long id,
    Long produtoId,
    String nomeProduto,
    Integer quantidade,
    BigDecimal precoUnitario,
    TipoPizza tipoPizza,
    Long segundoProdutoId,
    String nomeSegundoProduto,
    String borda,
    BigDecimal precoBorda,
    List<ItemPedidoAdicionalResponse> adicionais
) {
    public static ItemPedidoResponse de(ItemPedido item) {
        List<ItemPedidoAdicionalResponse> adicionais = item.getAdicionais() == null
            ? List.of()
            : item.getAdicionais().stream().map(ItemPedidoAdicionalResponse::de).toList();
        return new ItemPedidoResponse(
            item.getId(),
            item.getProduto() != null ? item.getProduto().getId() : null,
            item.getNomeProdutoSnapshot() != null
                ? item.getNomeProdutoSnapshot()
                : item.getProduto() != null ? item.getProduto().getNome() : null,
            item.getQuantidade(),
            item.getPrecoUnitario(),
            item.getTipoPizza(),
            item.getSegundoProduto() != null ? item.getSegundoProduto().getId() : null,
            item.getNomeSegundoProdutoSnapshot() != null
                ? item.getNomeSegundoProdutoSnapshot()
                : item.getSegundoProduto() != null ? item.getSegundoProduto().getNome() : null,
            item.getBordaNome(),
            item.getBordaPreco(),
            adicionais
        );
    }
}
