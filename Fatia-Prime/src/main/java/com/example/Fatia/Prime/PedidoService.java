package com.example.Fatia.Prime;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
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
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados do pedido são obrigatórios");
        }
        if (request.itens() == null || request.itens().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O pedido deve conter pelo menos um item");
        }

        Pedido pedido = new Pedido();
        if (request.usuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
            pedido.setUsuario(usuario);
        } else {
            validarClientePublico(request);
            pedido.setClienteNome(request.clienteNome().trim());
            pedido.setClienteEmail(request.clienteEmail().trim());
            pedido.setClienteTelefone(request.clienteTelefone().trim());
            pedido.setEndereco(normalizarOpcional(request.endereco()));
        }
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

    @Transactional(readOnly = true)
    public List<ConsultaPedidoResponse> consultar(String codigo, String telefone) {
        String codigoNormalizado = codigo == null ? "" : codigo.trim().toUpperCase(Locale.ROOT);
        String telefoneNormalizado = telefone == null ? "" : telefone.replaceAll("\\D", "");

        if (codigoNormalizado.isBlank() && telefoneNormalizado.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o código ou o telefone do pedido");
        }
        if (!codigoNormalizado.isBlank() && !telefoneNormalizado.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe apenas o código ou o telefone do pedido");
        }
        if (!telefoneNormalizado.isBlank() && telefoneNormalizado.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Telefone inválido");
        }

        List<Pedido> pedidos = !codigoNormalizado.isBlank()
            ? pedidoRepository.findByCodigoForConsulta(codigoNormalizado).map(List::of).orElseGet(List::of)
            : pedidoRepository.findAllByTelefoneForConsulta(telefoneNormalizado);

        if (pedidos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado");
        }
        return pedidos.stream().map(ConsultaPedidoResponse::de).toList();
    }

    private void validarClientePublico(PedidoRequest request) {
        if (request.clienteNome() == null || request.clienteNome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome do cliente é obrigatório");
        }
        if (request.clienteEmail() == null || request.clienteEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O e-mail do cliente é obrigatório");
        }
        if (request.clienteTelefone() == null || request.clienteTelefone().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O telefone do cliente é obrigatório");
        }
    }

    private String normalizarOpcional(String valor) {
        String normalizado = valor == null ? null : valor.trim();
        return normalizado == null || normalizado.isBlank() ? null : normalizado;
    }

    private String gerarCodigoUnico() {
        String codigo;
        do {
            codigo = "FP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        } while (pedidoRepository.existsByCodigo(codigo));
        return codigo;
    }
}