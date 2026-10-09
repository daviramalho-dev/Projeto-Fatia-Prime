package com.example.Fatia.Prime;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.zaxxer.hikari.HikariDataSource;

@SpringBootTest
class H2FilePersistenceTest {

    private static final Path DATABASE_DIRECTORY = createDatabaseDirectory();
    private static final String DATABASE_URL = "jdbc:h2:file:" + DATABASE_DIRECTORY.resolve("fatiaprime");

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private DataSource dataSource;

    @DynamicPropertySource
    static void configureFileDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> DATABASE_URL + ";DB_CLOSE_ON_EXIT=FALSE");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Test
    void deveReabrirBancoEmArquivoComPedidoEItensPersistidos() throws SQLException {
        try {
            Categoria categoria = categoriaRepository.saveAndFlush(new Categoria("Arquivo " + UUID.randomUUID()));
            Produto produto = produtoRepository.saveAndFlush(new Produto(
                "Produto arquivo " + UUID.randomUUID(),
                "Produto criado pelo teste de persistência em arquivo",
                new BigDecimal("52.90"),
                null,
                categoria
            ));
            Pedido pedido = new Pedido();
            pedido.setClienteNome("Teste H2 arquivo");
            pedido.setClienteTelefone("11977777777");
            pedido.setEndereco("Rua do Banco, 10");
            pedido.setCep("12345678");
            pedido.setCodigo("FP-FILE-" + UUID.randomUUID().toString().replace("-", ""));
            pedido.setStatus("Pedido recebido");
            pedido.setValorTotal(new BigDecimal("52.90"));
            pedido.setValorFrete(BigDecimal.ZERO);
            pedido.adicionarItem(new ItemPedido(produto, 1, produto.getPreco()));

            Pedido salvo = pedidoRepository.saveAndFlush(pedido);
            Long id = salvo.getId();
            String codigo = salvo.getCodigo();

            ((HikariDataSource) dataSource).close();

            try (Connection connection = DriverManager.getConnection(DATABASE_URL, "sa", "")) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "select codigo, cliente_nome from pedidos where id = ?")) {
                    statement.setLong(1, id);
                    try (ResultSet result = statement.executeQuery()) {
                        assertThat(result.next()).isTrue();
                        assertThat(result.getString("codigo")).isEqualTo(codigo);
                        assertThat(result.getString("cliente_nome")).isEqualTo("Teste H2 arquivo");
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(
                        "select count(*) from itens_pedido where pedido_id = ?")) {
                    statement.setLong(1, id);
                    try (ResultSet result = statement.executeQuery()) {
                        assertThat(result.next()).isTrue();
                        assertThat(result.getInt(1)).isEqualTo(1);
                    }
                }
            }
        } finally {
            ((HikariDataSource) dataSource).close();
            deleteDatabaseDirectory();
        }
    }

    private static Path createDatabaseDirectory() {
        try {
            return Files.createTempDirectory("fatiaprime-h2-file-");
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static void deleteDatabaseDirectory() {
        try (var paths = Files.walk(DATABASE_DIRECTORY)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException exception) {
                    throw new UncheckedIOException(exception);
                }
            });
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
