package com.example.Fatia.Prime;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
    private final OpcaoPizzaRepository opcaoPizzaRepository;

    public PedidoController(
        PedidoRepository pedidoRepository,
        ProdutoRepository produtoRepository,
        OpcaoPizzaRepository opcaoPizzaRepository
    ) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.opcaoPizzaRepository = opcaoPizzaRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<PedidoResponse> listar() {
        return pedidoRepository.findAll().stream()
            .map(PedidoResponse::de)
            .toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(@PathVariable Long id) {
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));

        return PedidoResponse.de(pedido);
    }

    @GetMapping("/consulta")
    @Transactional(readOnly = true)
    public List<PedidoConsultaResponse> consultar(
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
        return pedidos.stream().map(PedidoConsultaResponse::de).toList();
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

            if (itemRequest.quantidade() == null || itemRequest.quantidade() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantidade do item deve ser maior que zero");
            }

            validarProdutoDisponivel(produto);
            TipoPizza tipoPizza = itemRequest.tipoPizza() == null ? TipoPizza.INTEIRA : itemRequest.tipoPizza();
            Produto segundoProduto = null;
            if (tipoPizza == TipoPizza.MEIO_A_MEIO) {
                if (itemRequest.segundoProdutoId() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione o segundo sabor da pizza");
                }
                segundoProduto = produtoRepository.findById(itemRequest.segundoProdutoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Segundo sabor não encontrado"));
                validarProdutoDisponivel(segundoProduto);
                if (produto.getTipo() != segundoProduto.getTipo()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Os sabores precisam ser do mesmo tipo");
                }
            } else if (itemRequest.segundoProdutoId() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pizza inteira aceita somente um sabor");
            }

            BigDecimal precoBase = segundoProduto == null || produto.getPreco().compareTo(segundoProduto.getPreco()) >= 0
                ? produto.getPreco()
                : segundoProduto.getPreco();
            BigDecimal precoBorda = BigDecimal.ZERO;
            String nomeBorda = null;
            if (itemRequest.bordaId() != null) {
                OpcaoPizza borda = opcaoPizzaRepository.findById(itemRequest.bordaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Borda inválida"));
                validarOpcao(borda, TipoOpcaoPizza.BORDA, produto.getTipo(), "Borda");
                precoBorda = borda.getPrecoAdicional();
                nomeBorda = borda.getNome();
            }

            BigDecimal precoAdicionais = BigDecimal.ZERO;
            List<ItemPedidoAdicional> adicionais = new ArrayList<>();
            Set<Long> idsAdicionais = new HashSet<>();
            for (Long adicionalId : itemRequest.adicionalIds()) {
                if (adicionalId == null || !idsAdicionais.add(adicionalId)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adicional inválido ou repetido");
                }
                OpcaoPizza adicional = opcaoPizzaRepository.findById(adicionalId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adicional inválido"));
                validarOpcao(adicional, TipoOpcaoPizza.ADICIONAL, produto.getTipo(), "Adicional");
                adicionais.add(new ItemPedidoAdicional(
                    adicional.getId(), adicional.getNome(), adicional.getPrecoAdicional()));
                precoAdicionais = precoAdicionais.add(adicional.getPrecoAdicional());
            }

            BigDecimal precoUnitario = precoBase.add(precoBorda).add(precoAdicionais);
            ItemPedido item = new ItemPedido(produto, itemRequest.quantidade(), precoUnitario);
            item.setTipoPizza(tipoPizza);
            item.setSegundoProduto(segundoProduto);
            item.setNomeSegundoProdutoSnapshot(segundoProduto == null ? null : segundoProduto.getNome());
            item.setBordaNome(nomeBorda);
            item.setBordaPreco(precoBorda);
            item.setAdicionais(adicionais);
            pedido.adicionarItem(item);
            total = total.add(precoUnitario.multiply(BigDecimal.valueOf(itemRequest.quantidade())));
        }

        pedido.setValorTotal(total);
        Pedido salvo = pedidoRepository.saveAndFlush(pedido);
        return PedidoResponse.de(salvo);
    }

    private void validarProdutoDisponivel(Produto produto) {
        if (!produto.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto indisponível: " + produto.getNome());
        }
        if (produto.getCategoria() == null || produto.getTipo() == null || produto.getPreco() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto inválido para pedidos");
        }
    }

    private void validarOpcao(OpcaoPizza opcao, TipoOpcaoPizza tipo, TipoProdutoPizza tipoProduto, String rotulo) {
        if (!opcao.isAtivo() || opcao.getTipo() != tipo || opcao.getTipoProduto() != tipoProduto) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, rotulo + " inválida ou indisponível");
        }
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
        String telefone = normalizarTelefone(request.clienteTelefone());
        if (telefone.length() != 10 && telefone.length() != 11) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O telefone do cliente é inválido");
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
