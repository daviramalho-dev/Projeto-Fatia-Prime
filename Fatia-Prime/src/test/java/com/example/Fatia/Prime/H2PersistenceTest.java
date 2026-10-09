package com.example.Fatia.Prime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:fatiaprime",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.defer-datasource-initialization=true"
})
@Transactional
class H2PersistenceTest {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void devePersistirECategoriasEProdutosViaSeed() {
        // Verifica se os dados do data.sql foram carregados
        List<Categoria> categorias = categoriaRepository.findAll();
        assertThat(categorias).isNotEmpty();
        assertThat(categorias).hasSize(6); // Clássicas, Carnes, Frango, Queijos, Doces, Bebidas

        List<Produto> produtos = produtoRepository.findAll();
        assertThat(produtos).isNotEmpty();
        // Deveria ter pelo menos os produtos do seed (23 salgadas + 12 doces + 9 bebidas = ~44)
        assertThat(produtos.size()).isGreaterThan(30);
    }

    @Test
    void devePersistirPedidoComItensERecuperar() {
        // Limpa pedidos existentes para isolar o teste (especialmente após outras execuções)
        pedidoRepository.deleteAll();
        // Também limpa dados relacionados (itens_pedido -> pedidos -> produtos)
        entityManager.flush();
        entityManager.clear();

        // Cria um pedido simples
        Produto produto = produtoRepository.findAll().stream().findFirst().orElseThrow();
        Pedido pedido = new Pedido();
        pedido.setClienteNome("Teste Persistência");
        pedido.setClienteTelefone("11999999999");
        pedido.setEndereco("Rua Teste, 123");
        pedido.setCep("12345678");
        pedido.setCodigo("FP-TEST-" + System.currentTimeMillis());
        pedido.setStatus("Pedido recebido");
        pedido.setValorTotal(new BigDecimal("52.90"));
        pedido.setValorFrete(BigDecimal.ZERO);

        ItemPedido item = new ItemPedido(produto, 1, produto.getPreco());
        item.setTipoProdutoSnapshot(produto.getTipo());
        pedido.adicionarItem(item);

        Pedido salvo = pedidoRepository.saveAndFlush(pedido);
        Long id = salvo.getId();

        // Flush e clear para forçar leitura do banco
        entityManager.flush();
        entityManager.clear();

        // Recupera e verifica
        Pedido recuperado = pedidoRepository.findById(id).orElseThrow();
        assertThat(recuperado.getId()).isEqualTo(id);
        assertThat(recuperado.getClienteNome()).isEqualTo("Teste Persistência");
        assertThat(recuperado.getItens()).hasSize(1);
        assertThat(recuperado.getItens().get(0).getPrecoUnitario()).isEqualByComparingTo(produto.getPreco());
    }

    @Test
    void devePersistirUsuarioAdminEAutenticacao() {
        Usuario admin = usuarioRepository.saveAndFlush(new Usuario(
            "Administrador de teste",
            "admin-persistence-" + UUID.randomUUID() + "@fatiaprime.test",
            passwordEncoder.encode("senha-123"),
            UsuarioRole.ADMIN
        ));
        Long id = admin.getId();

        entityManager.clear();
        Usuario recuperado = usuarioRepository.findById(id).orElseThrow();

        assertThat(recuperado.getEmail()).contains("admin-persistence-");
        assertThat(recuperado.getSenhaHash()).isNotEmpty();
        assertThat(recuperado.getRole()).isEqualTo(UsuarioRole.ADMIN);
        assertThat(passwordEncoder.matches("senha-123", recuperado.getSenhaHash())).isTrue();
    }

    @Test
    void devePersistirAtualizacaoStatusPedido() {
        Produto produto = produtoRepository.findAll().stream().findFirst().orElseThrow();
        Pedido pedido = new Pedido();
        pedido.setClienteNome("Teste Status");
        pedido.setClienteTelefone("11888888888");
        pedido.setEndereco("Rua Status, 456");
        pedido.setCep("87654321");
        pedido.setCodigo("FP-STATUS-" + System.currentTimeMillis());
        pedido.setStatus("Pedido recebido");
        pedido.setValorTotal(new BigDecimal("52.90"));
        pedido.setValorFrete(BigDecimal.ZERO);

        ItemPedido item = new ItemPedido(produto, 1, produto.getPreco());
        item.setTipoProdutoSnapshot(produto.getTipo());
        pedido.adicionarItem(item);

        Pedido salvo = pedidoRepository.saveAndFlush(pedido);
        Long id = salvo.getId();

        entityManager.flush();
        entityManager.clear();

        // Atualiza status
        Pedido paraAtualizar = pedidoRepository.findById(id).orElseThrow();
        paraAtualizar.setStatus("Pedido em andamento");
        pedidoRepository.saveAndFlush(paraAtualizar);

        entityManager.flush();
        entityManager.clear();

        // Verifica se persistiu
        Pedido verificado = pedidoRepository.findById(id).orElseThrow();
        assertThat(verificado.getStatus()).isEqualTo("Pedido em andamento");
        // Verifica se a atualização persistiu após clear
        entityManager.clear();
        Pedido verificado2 = pedidoRepository.findById(id).orElseThrow();
        assertThat(verificado2.getStatus()).isEqualTo("Pedido em andamento");
    }
}
