package com.example.Fatia.Prime;

public record UsuarioResponse(Long id, String nome, String email) {
    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}