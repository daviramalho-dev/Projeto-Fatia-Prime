package com.example.Fatia.Prime;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/produtos")
@Transactional
public class AdminProdutoController {

    private static final List<String> STATUS_VALIDOS = List.of("ativo", "inativo");

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;

    public AdminProdutoController(ProdutoRepository produtoRepository, CategoriaRepository categoriaRepository) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    public List<ProdutoResponse> listar(
        @RequestParam(required = false) String nome,
        @RequestParam(required = false) Long categoriaId,
        @RequestParam(required = false) String status
    ) {
        String nomeBusca = normalizar(nome);
        Boolean ativo = status == null || status.isBlank() ? null : converterStatus(status);

        return produtoRepository.findAll().stream()
            .filter(produto -> nomeBusca.isBlank() || normalizar(produto.getNome()).contains(nomeBusca))
            .filter(produto -> categoriaId == null
                || produto.getCategoria() != null && categoriaId.equals(produto.getCategoria().getId()))
            .filter(produto -> ativo == null || produto.isAtivo() == ativo)
            .sorted((primeiro, segundo) -> normalizar(primeiro.getNome()).compareTo(normalizar(segundo.getNome())))
            .map(ProdutoResponse::de)
            .toList();
    }

    @GetMapping("/{id}")
    public ProdutoResponse buscarPorId(@PathVariable Long id) {
        return ProdutoResponse.de(buscarProduto(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoResponse criar(@Valid @RequestBody AdminProdutoRequest request) {
        Categoria categoria = buscarCategoria(request.categoriaId());
        Produto produto = new Produto();
        preencher(produto, request, categoria);
        return ProdutoResponse.de(produtoRepository.saveAndFlush(produto));
    }

    @PutMapping("/{id}")
    public ProdutoResponse editar(@PathVariable Long id, @Valid @RequestBody AdminProdutoRequest request) {
        Produto produto = buscarProduto(id);
        Categoria categoria = buscarCategoria(request.categoriaId());
        preencher(produto, request, categoria);
        return ProdutoResponse.de(produtoRepository.saveAndFlush(produto));
    }

    @PatchMapping("/{id}/status")
    public ProdutoResponse atualizarStatus(
        @PathVariable Long id,
        @Valid @RequestBody AdminProdutoStatusRequest request
    ) {
        Produto produto = buscarProduto(id);
        produto.setAtivo(converterStatus(request.status()));
        return ProdutoResponse.de(produtoRepository.saveAndFlush(produto));
    }

    private Produto buscarProduto(Long id) {
        return produtoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));
    }

    private void preencher(Produto produto, AdminProdutoRequest request, Categoria categoria) {
        produto.setNome(request.nome().trim());
        produto.setDescricao(normalizarOpcional(request.descricao()));
        produto.setPreco(request.preco());
        produto.setImagem(normalizarOpcional(request.imagem()));
        produto.setCategoria(categoria);
        if (request.ativo() != null) produto.setAtivo(request.ativo());
    }

    private Boolean converterStatus(String status) {
        String valor = normalizar(status);
        if (!STATUS_VALIDOS.contains(valor)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status deve ser ativo ou inativo");
        }
        return "ativo".equals(valor);
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizarOpcional(String valor) {
        String normalizado = valor == null ? null : valor.trim();
        return normalizado == null || normalizado.isBlank() ? null : normalizado;
    }
}