package com.example.Fatia.Prime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
class PedidoServiceTests {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Usuario usuario;
    private Produto produto;

    @BeforeEach
    void prepararDados() {
        pedidoRepository.deleteAll();
        produtoRepository.deleteAll();
        categoriaRepository.deleteAll();
        usuarioRepository.deleteAll();

        usuario = usuarioRepository.saveAndFlush(
            new Usuario("Cliente de Teste", "cliente-pedido@fatiaprime.test", "hash-de-teste")
        );
        Categoria categoria = categoriaRepository.saveAndFlush(new Categoria("Testes"));
        produto = produtoRepository.saveAndFlush(
            new Produto("Produto de Teste", "Descrição do banco", new BigDecimal("12.50"), null, categoria)
        );
    }

    @Test
    void criaPedidoValidoComPrecoDoBancoETotalCalculado() {
        Pedido pedido = pedidoService.criar(requestComQuantidade(3));

        assertNotNull(pedido.getId());
        assertEquals(usuario.getId(), pedido.getUsuario().getId());
        assertEquals(1, pedido.getItens().size());
        assertEquals(produto.getId(), pedido.getItens().get(0).getProduto().getId());
        assertEquals(new BigDecimal("12.50"), pedido.getItens().get(0).getPrecoUnitario());
        assertEquals(new BigDecimal("37.50"), pedido.getValorTotal());
        assertEquals("Pedido recebido", pedido.getStatus());
    }

    @Test
    void criaPedidoPublicoSemUsuarioEPersisteDadosDoCliente() {
        Pedido pedido = pedidoService.criar(new PedidoRequest(
            null,
            "Cliente Público",
            "publico@fatiaprime.test",
            "(61) 99999-1111",
            "Rua Principal, 10",
            "Sem cebola",
            List.of(new ItemPedidoRequest(produto.getId(), 2))
        ));

        assertEquals(null, pedido.getUsuario());
        assertEquals("Cliente Público", pedido.getClienteNome());
        assertEquals("publico@fatiaprime.test", pedido.getClienteEmail());
        assertEquals("(61) 99999-1111", pedido.getClienteTelefone());
        assertEquals("Rua Principal, 10", pedido.getEndereco());
        assertEquals(new BigDecimal("25.00"), pedido.getValorTotal());
    }

    @Test
    void rejeitaPedidoSemItens() {
        PedidoRequest request = new PedidoRequest(usuario.getId(), null, List.of());

        assertThrows(ResponseStatusException.class, () -> pedidoService.criar(request));
    }

    @Test
    void rejeitaUsuarioInexistente() {
        PedidoRequest request = new PedidoRequest(999999L, null, List.of(
            new ItemPedidoRequest(produto.getId(), 1)
        ));

        assertThrows(ResponseStatusException.class, () -> pedidoService.criar(request));
    }

    @Test
    void rejeitaQuantidadeZero() {
        assertThrows(ResponseStatusException.class, () -> pedidoService.criar(requestComQuantidade(0)));
    }

    @Test
    void rejeitaQuantidadeNegativa() {
        assertThrows(ResponseStatusException.class, () -> pedidoService.criar(requestComQuantidade(-1)));
    }

    @Test
    void rejeitaProdutoInexistente() {
        PedidoRequest request = new PedidoRequest(usuario.getId(), null, List.of(
            new ItemPedidoRequest(999999L, 1)
        ));

        assertThrows(ResponseStatusException.class, () -> pedidoService.criar(request));
    }

    @Test
    void geraCodigoDeAcompanhamentoUnico() {
        Pedido primeiro = pedidoService.criar(requestComQuantidade(1));
        Pedido segundo = pedidoService.criar(requestComQuantidade(1));

        assertNotNull(primeiro.getCodigo());
        assertNotNull(segundo.getCodigo());
        assertNotEquals(primeiro.getCodigo(), segundo.getCodigo());
        assertEquals(2, pedidoRepository.count());
    }

    @Test
    void naoAceitaPrecoEnviadoPeloClienteComoFonteDoCalculo() {
        Pedido pedido = pedidoService.criar(requestComQuantidade(2));

        assertEquals(new BigDecimal("12.50"), pedido.getItens().get(0).getPrecoUnitario());
        assertEquals(new BigDecimal("25.00"), pedido.getValorTotal());
    }

    private PedidoRequest requestComQuantidade(int quantidade) {
        return new PedidoRequest(usuario.getId(), "Sem observações", List.of(
            new ItemPedidoRequest(produto.getId(), quantidade)
        ));
    }
}