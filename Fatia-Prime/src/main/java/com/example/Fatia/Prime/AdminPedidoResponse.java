package com.example.Fatia.Prime;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminPedidoResponse(
    Long id,
    String codigo,
    String status,
    LocalDateTime dataCriacao,
    String nomeCliente,
    String telefone,
    String email,
    String endereco,
    String observacoes,
    List<AdminItemPedidoResponse> itens,
    BigDecimal total
) {
    public static AdminPedidoResponse de(Pedido pedido) {
        List<AdminItemPedidoResponse> itens = pedido.getItens() == null
            ? List.of()
            : pedido.getItens().stream().map(AdminItemPedidoResponse::de).toList();
        Usuario usuario = pedido.getUsuario();
        return new AdminPedidoResponse(
            pedido.getId(),
            pedido.getCodigo(),
            pedido.getStatus(),
            pedido.getDataCriacao(),
            usuario != null ? usuario.getNome() : null,
            usuario != null ? usuario.getTelefone() : null,
            usuario != null ? usuario.getEmail() : null,
            null,
            pedido.getObservacoes(),
            itens,
            pedido.getValorTotal()
        );
    }
}
