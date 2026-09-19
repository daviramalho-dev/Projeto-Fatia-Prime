package com.example.Fatia.Prime;

public record CategoriaResponse(Long id, String nome) {
    public static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome());
    }
}
