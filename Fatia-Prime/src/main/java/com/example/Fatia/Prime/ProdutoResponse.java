package com.example.Fatia.Prime;

import java.math.BigDecimal;

public record ProdutoResponse(
    Long id,
    String nome,
    String descricao,
    BigDecimal preco,
    String imagem,
    String destaque,
    boolean ativo,
    Long categoriaId,
    String categoriaNome,
    TipoProdutoPizza tipo
) {
    public static ProdutoResponse de(Produto produto) {
        return new ProdutoResponse(
            produto.getId(),
            produto.getNome(),
            produto.getDescricao(),
            produto.getPreco(),
            produto.getImagem(),
            produto.getDestaque(),
            produto.isAtivo(),
            produto.getCategoria() != null ? produto.getCategoria().getId() : null,
            produto.getCategoria() != null ? produto.getCategoria().getNome() : null,
            produto.getTipo()
        );
    }
}
