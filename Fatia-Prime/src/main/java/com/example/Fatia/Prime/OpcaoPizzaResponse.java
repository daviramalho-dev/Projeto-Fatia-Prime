package com.example.Fatia.Prime;

import java.math.BigDecimal;

public record OpcaoPizzaResponse(
    Long id,
    String nome,
    TipoOpcaoPizza tipo,
    TipoProdutoPizza tipoProduto,
    BigDecimal precoAdicional,
    boolean ativo
) {
    public static OpcaoPizzaResponse de(OpcaoPizza opcao) {
        return new OpcaoPizzaResponse(
            opcao.getId(),
            opcao.getNome(),
            opcao.getTipo(),
            opcao.getTipoProduto(),
            opcao.getPrecoAdicional(),
            opcao.isAtivo()
        );
    }
}
