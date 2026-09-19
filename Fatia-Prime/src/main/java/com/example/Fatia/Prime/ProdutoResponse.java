package com.example.Fatia.Prime;

import java.math.BigDecimal;

public record ProdutoResponse(
    Long id,
    String nome,
    String descricao,
    BigDecimal preco,
    String imagem,
    boolean ativo,
    Long categoriaId,
    String categoriaNome
) {
    public static ProdutoResponse de(Produto produto) {
        return new ProdutoResponse(
            produto.getId(),
            produto.getNome(),
            produto.getDescricao(),
            produto.getPreco(),
            produto.getImagem(),
            produto.isAtivo(),
            produto.getCategoria() != null ? produto.getCategoria().getId() : null,
            produto.getCategoria() != null ? produto.getCategoria().getNome() : null
        );
    }
}
