package com.example.Fatia.Prime;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PedidoService {

    private static final String STATUS_INICIAL = "Pedido recebido";

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoService(
        PedidoRepository pedidoRepository,
        UsuarioRepository usuarioRepository,
        ProdutoRepository produtoRepository
    ) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public Pedido criar(PedidoRequest request) {
        if (request == null || request.usuarioId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O usuário é obrigatório");
        }
        if (request.itens() == null || request.itens().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O pedido deve conter pelo menos um item");
        }

        Usuario usuario = usuarioRepository.findById(request.usuarioId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setCodigo(gerarCodigoUnico());
        pedido.setStatus(STATUS_INICIAL);
        pedido.setObservacoes(request.observacoes());

        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedidoRequest itemRequest : request.itens()) {
            if (itemRequest == null || itemRequest.produtoId() == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado");
            }
            if (itemRequest.quantidade() == null || itemRequest.quantidade() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantidade do item deve ser maior que zero");
            }

            Produto produto = produtoRepository.findById(itemRequest.produtoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
            if (!produto.isAtivo()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto indisponível: " + produto.getNome());
            }

            BigDecimal precoUnitario = produto.getPreco();
            ItemPedido item = new ItemPedido(produto, itemRequest.quantidade(), precoUnitario);
            pedido.adicionarItem(item);
            total = total.add(precoUnitario.multiply(BigDecimal.valueOf(itemRequest.quantidade())));
        }

        pedido.setValorTotal(total);
        return pedidoRepository.saveAndFlush(pedido);
    }

    private String gerarCodigoUnico() {
        String codigo;
        do {
            codigo = "FP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        } while (pedidoRepository.existsByCodigo(codigo));
        return codigo;
    }
}