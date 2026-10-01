package com.example.Fatia.Prime;

import java.math.BigDecimal;

public record ItemPedidoAdicionalResponse(
    Long opcaoId,
    String nome,
    BigDecimal precoAdicional
) {
    public static ItemPedidoAdicionalResponse de(ItemPedidoAdicional adicional) {
        return new ItemPedidoAdicionalResponse(
            adicional.getOpcaoId(),
            adicional.getNome(),
            adicional.getPrecoAdicional()
        );
    }
}
