package com.example.Fatia.Prime;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoRepository repository;

    public ProdutoController(ProdutoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar() {
        return repository.findByAtivoTrueOrderByNomeAsc().stream()
            .map(ProdutoResponse::de)
            .toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(@PathVariable Long id) {
        Produto produto = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
        if (!produto.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado");
        }

        return ProdutoResponse.de(produto);
    }
}
