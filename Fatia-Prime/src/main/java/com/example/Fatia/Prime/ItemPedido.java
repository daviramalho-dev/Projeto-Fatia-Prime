package com.example.Fatia.Prime;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "itens_pedido")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pizza", nullable = false, length = 20)
    private TipoPizza tipoPizza = TipoPizza.INTEIRA;

    @Column(name = "nome_produto_snapshot", length = 150)
    private String nomeProdutoSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_produto_snapshot", length = 20)
    private TipoProdutoPizza tipoProdutoSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segundo_produto_id")
    private Produto segundoProduto;

    @Column(name = "nome_segundo_produto_snapshot", length = 150)
    private String nomeSegundoProdutoSnapshot;

    @Column(name = "borda_nome", length = 100)
    private String bordaNome;

    @Column(name = "borda_preco", precision = 10, scale = 2)
    private BigDecimal bordaPreco = BigDecimal.ZERO;

    @ElementCollection
    @CollectionTable(name = "item_pedido_adicionais", joinColumns = @JoinColumn(name = "item_pedido_id"))
    @AttributeOverrides({
        @AttributeOverride(name = "opcaoId", column = @Column(name = "opcao_id")),
        @AttributeOverride(name = "nome", column = @Column(name = "nome", nullable = false, length = 100)),
        @AttributeOverride(name = "precoAdicional", column = @Column(name = "preco_adicional", nullable = false, precision = 10, scale = 2)),
        @AttributeOverride(name = "tipo", column = @Column(name = "tipo_opcao", length = 20))
    })
    private List<ItemPedidoAdicional> adicionais = new ArrayList<>();

    public ItemPedido() {
    }

    public ItemPedido(Produto produto, Integer quantidade,
                      BigDecimal precoUnitario) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.nomeProdutoSnapshot = produto != null ? produto.getNome() : null;
        this.tipoProdutoSnapshot = produto != null ? produto.getTipo() : null;
    }

    public Long getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public TipoPizza getTipoPizza() {
        return tipoPizza;
    }

    public void setTipoPizza(TipoPizza tipoPizza) {
        this.tipoPizza = tipoPizza;
    }

    public String getNomeProdutoSnapshot() {
        return nomeProdutoSnapshot;
    }

    public void setNomeProdutoSnapshot(String nomeProdutoSnapshot) {
        this.nomeProdutoSnapshot = nomeProdutoSnapshot;
    }

    public TipoProdutoPizza getTipoProdutoSnapshot() {
        return tipoProdutoSnapshot != null ? tipoProdutoSnapshot : produto != null ? produto.getTipo() : null;
    }

    public void setTipoProdutoSnapshot(TipoProdutoPizza tipoProdutoSnapshot) {
        this.tipoProdutoSnapshot = tipoProdutoSnapshot;
    }

    public Produto getSegundoProduto() {
        return segundoProduto;
    }

    public void setSegundoProduto(Produto segundoProduto) {
        this.segundoProduto = segundoProduto;
    }

    public String getNomeSegundoProdutoSnapshot() {
        return nomeSegundoProdutoSnapshot;
    }

    public void setNomeSegundoProdutoSnapshot(String nomeSegundoProdutoSnapshot) {
        this.nomeSegundoProdutoSnapshot = nomeSegundoProdutoSnapshot;
    }

    public String getBordaNome() {
        return bordaNome;
    }

    public void setBordaNome(String bordaNome) {
        this.bordaNome = bordaNome;
    }

    public BigDecimal getBordaPreco() {
        return bordaPreco == null ? BigDecimal.ZERO : bordaPreco;
    }

    public void setBordaPreco(BigDecimal bordaPreco) {
        this.bordaPreco = bordaPreco;
    }

    public List<ItemPedidoAdicional> getAdicionais() {
        return adicionais;
    }

    public void setAdicionais(List<ItemPedidoAdicional> adicionais) {
        this.adicionais = adicionais == null ? new ArrayList<>() : new ArrayList<>(adicionais);
    }
}