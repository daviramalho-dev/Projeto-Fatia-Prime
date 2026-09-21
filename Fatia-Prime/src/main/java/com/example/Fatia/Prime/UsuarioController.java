package com.example.Fatia.Prime;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    public UsuarioController(UsuarioRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@Valid @RequestBody CadastroRequest dados) {
        if (dados.senha().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha deve ocupar no máximo 72 bytes em UTF-8");
        }

        if (repository.existsByEmail(dados.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está cadastrado");
        }

        Usuario usuario = new Usuario(
            dados.nome(),
            dados.email(),
            encoder.encode(dados.senha()),
            UsuarioRole.USER
        );

        return UsuarioResponse.de(repository.saveAndFlush(usuario));
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return repository.findAll().stream()
            .map(UsuarioResponse::de)
            .toList();
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscarPorId(@PathVariable Long id) {
        Usuario usuario = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        return UsuarioResponse.de(usuario);
    }
}