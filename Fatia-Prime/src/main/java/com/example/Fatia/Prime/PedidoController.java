package com.example.Fatia.Prime;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoController(
        PedidoRepository pedidoRepository,
        ProdutoRepository produtoRepository
    ) {
        this.pedidoRepository = pedidoRepository;
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

    @GetMapping("/consulta")
    @Transactional(readOnly = true)
    public List<PedidoResponse> consultar(
        @RequestParam(required = false) String codigo,
        @RequestParam(required = false) String telefone
    ) {
        String codigoNormalizado = codigo == null ? "" : codigo.trim().toUpperCase(Locale.ROOT);
        String telefoneNormalizado = normalizarTelefone(telefone);
        if (codigoNormalizado.isBlank() == telefoneNormalizado.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o código ou o telefone do pedido");
        }

        List<Pedido> pedidos = pedidoRepository.findAllForConsulta().stream()
            .filter(pedido -> codigoNormalizado.isBlank()
                ? telefoneNormalizado.equals(normalizarTelefone(telefoneDoPedido(pedido)))
                : codigoNormalizado.equals(normalizarCodigo(pedido.getCodigo())))
            .toList();

        if (pedidos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado");
        }
        return pedidos.stream().map(PedidoResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public PedidoResponse criar(@Valid @RequestBody PedidoRequest request) {
        Pedido pedido = new Pedido();
        if (request.usuarioId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pedidos públicos não podem informar usuário");
        }
        validarClientePublico(request);
        pedido.setClienteNome(request.clienteNome().trim());
        pedido.setClienteEmail(request.clienteEmail().trim());
        pedido.setClienteTelefone(normalizarTelefone(request.clienteTelefone()));
        pedido.setEndereco(normalizarOpcional(request.endereco()));
        pedido.setCodigo("FP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        pedido.setStatus("Pedido recebido");
        pedido.setObservacoes(request.observacoes());

        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedidoRequest itemRequest : request.itens()) {
            if (itemRequest == null || itemRequest.produtoId() == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado");
            }
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
            pedido.adicionarItem(item);
            total = total.add(precoUnitario.multiply(BigDecimal.valueOf(itemRequest.quantidade())));
        }

        pedido.setValorTotal(total);
        Pedido salvo = pedidoRepository.saveAndFlush(pedido);
        return PedidoResponse.de(salvo);
    }

    private void validarClientePublico(PedidoRequest request) {
        if (request.clienteNome() == null || request.clienteNome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome do cliente é obrigatório");
        }
        if (request.clienteEmail() == null || request.clienteEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O e-mail do cliente é obrigatório");
        }
        if (request.clienteTelefone() == null || normalizarTelefone(request.clienteTelefone()).isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O telefone do cliente é obrigatório");
        }
    }

    private String telefoneDoPedido(Pedido pedido) {
        return pedido.getClienteTelefone() != null
            ? pedido.getClienteTelefone()
            : pedido.getUsuario() != null ? pedido.getUsuario().getTelefone() : "";
    }

    private String normalizarCodigo(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizarTelefone(String telefone) {
        return telefone == null ? "" : telefone.replaceAll("\\D", "");
    }

    private String normalizarOpcional(String valor) {
        String normalizado = valor == null ? null : valor.trim();
        return normalizado == null || normalizado.isBlank() ? null : normalizado;
    }
}
