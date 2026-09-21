package com.example.Fatia.Prime;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoConsultaResponse(
    String codigo,
    String status,
    LocalDateTime dataCriacao,
    String clienteTelefone,
    BigDecimal valorTotal,
    List<ItemPedidoResponse> itens
) {
    public static PedidoConsultaResponse de(Pedido pedido) {
        List<ItemPedidoResponse> itens = pedido.getItens() == null
            ? List.of()
            : pedido.getItens().stream().map(ItemPedidoResponse::de).toList();

        return new PedidoConsultaResponse(
            pedido.getCodigo(),
            pedido.getStatus(),
            pedido.getDataCriacao(),
            pedido.getClienteTelefone(),
            pedido.getValorTotal(),
            itens
        );
    }
}
