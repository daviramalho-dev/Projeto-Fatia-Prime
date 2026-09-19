package com.example.Fatia.Prime;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ConsultaPedidoResponse(
    Long id,
    Long usuarioId,
    String codigo,
    String status,
    LocalDateTime dataCriacao,
    BigDecimal valorTotal,
    String observacoes,
    String nomeCliente,
    String emailCliente,
    String telefone,
    List<ConsultaItemPedidoResponse> itens
) {
    public static ConsultaPedidoResponse de(Pedido pedido) {
        Usuario usuario = pedido.getUsuario();
        return new ConsultaPedidoResponse(
            pedido.getId(),
            usuario != null ? usuario.getId() : null,
            pedido.getCodigo(),
            pedido.getStatus(),
            pedido.getDataCriacao(),
            pedido.getValorTotal(),
            pedido.getObservacoes(),
            usuario != null ? usuario.getNome() : pedido.getClienteNome(),
            usuario != null ? usuario.getEmail() : pedido.getClienteEmail(),
            usuario != null ? usuario.getTelefone() : pedido.getClienteTelefone(),
            pedido.getItens().stream().map(ConsultaItemPedidoResponse::de).toList()
        );
    }
}