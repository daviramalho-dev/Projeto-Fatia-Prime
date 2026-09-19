package com.example.Fatia.Prime;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoController(
        PedidoRepository pedidoRepository,
        UsuarioRepository usuarioRepository,
        ProdutoRepository produtoRepository
    ) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
    }

    @GetMapping
    public List<PedidoResponse> listar() {
        return pedidoRepository.findAll().stream()
            .map(PedidoResponse::de)
            .toList();
    }

    @GetMapping("/{id}")
    public PedidoResponse buscarPorId(@PathVariable Long id) {
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));

        return PedidoResponse.de(pedido);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse criar(@Valid @RequestBody PedidoRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setCodigo("FP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        pedido.setStatus("Pedido recebido");
        pedido.setObservacoes(request.observacoes());

        List<ItemPedido> itens = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedidoRequest itemRequest : request.itens()) {
            Produto produto = produtoRepository.findById(itemRequest.produtoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

            if (!produto.isAtivo()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto indisponível: " + produto.getNome());
            }

            if (itemRequest.quantidade() == null || itemRequest.quantidade() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantidade do item deve ser maior que zero");
            }

            BigDecimal precoUnitario = produto.getPreco();
            ItemPedido item = new ItemPedido(produto, itemRequest.quantidade(), precoUnitario);
            item.setPedido(pedido);
            itens.add(item);
            total = total.add(precoUnitario.multiply(BigDecimal.valueOf(itemRequest.quantidade())));
        }

        pedido.setItens(itens);
        pedido.setValorTotal(total);
        Pedido salvo = pedidoRepository.saveAndFlush(pedido);
        return PedidoResponse.de(salvo);
    }
}
