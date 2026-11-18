package com.example.Fatia.Prime;

import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaRepository repository;

    public CategoriaController(CategoriaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listar() {
        List<CategoriaResponse> categorias = repository.findAll().stream()
            .map(CategoriaResponse::de)
            .toList();
        // Lista publica: o navegador sempre revalida (ETag do catalogoEtagFilter em WebConfig -> 304).
        return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(categorias);
    }

    @GetMapping("/{id}")
    public CategoriaResponse buscarPorId(@PathVariable Long id) {
        Categoria categoria = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));

        return CategoriaResponse.de(categoria);
    }
}
