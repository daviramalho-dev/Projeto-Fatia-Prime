package com.example.Fatia.Prime;

import java.math.BigDecimal;
import java.util.List;

public record AdminItemPedidoResponse(
    Long id,
    Long produtoId,
    String nomeProduto,
    Integer quantidade,
    BigDecimal precoUnitario,
    BigDecimal subtotal,
    TipoProdutoPizza tipoProduto,
    TipoPizza tipoPizza,
    Long segundoProdutoId,
    String nomeSegundoProduto,
    String borda,
    BigDecimal precoBorda,
    List<ItemPedidoAdicionalResponse> adicionais,
    List<ItemPedidoAdicionalResponse> molhos
) {
    public static AdminItemPedidoResponse de(ItemPedido item) {
        BigDecimal preco = item.getPrecoUnitario();
        BigDecimal subtotal = preco == null || item.getQuantidade() == null
            ? BigDecimal.ZERO
            : preco.multiply(BigDecimal.valueOf(item.getQuantidade()));
        List<ItemPedidoAdicionalResponse> adicionais = opcoes(item, TipoOpcaoPizza.ADICIONAL);
        List<ItemPedidoAdicionalResponse> molhos = opcoes(item, TipoOpcaoPizza.MOLHO);
        return new AdminItemPedidoResponse(
            item.getId(),
            item.getProduto() != null ? item.getProduto().getId() : null,
            item.getNomeProdutoSnapshot() != null
                ? item.getNomeProdutoSnapshot()
                : item.getProduto() != null ? item.getProduto().getNome() : null,
            item.getQuantidade(),
            preco,
            subtotal,
            item.getTipoProdutoSnapshot(),
            item.getTipoPizza(),
            item.getSegundoProduto() != null ? item.getSegundoProduto().getId() : null,
            item.getNomeSegundoProdutoSnapshot() != null
                ? item.getNomeSegundoProdutoSnapshot()
                : item.getSegundoProduto() != null ? item.getSegundoProduto().getNome() : null,
            item.getBordaNome(),
            item.getBordaPreco(),
            adicionais,
            molhos
        );
    }

    private static List<ItemPedidoAdicionalResponse> opcoes(ItemPedido item, TipoOpcaoPizza tipo) {
        return item.getAdicionais() == null ? List.of() : item.getAdicionais().stream()
            .filter(opcao -> opcao.getTipo() == tipo)
            .map(ItemPedidoAdicionalResponse::de)
            .toList();
    }
}
