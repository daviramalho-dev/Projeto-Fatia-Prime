package com.example.Fatia.Prime;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/opcoes-pizza")
@Transactional
public class AdminOpcaoPizzaController {

    private final OpcaoPizzaRepository repository;

    public AdminOpcaoPizzaController(OpcaoPizzaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<OpcaoPizzaResponse> listar() {
        return repository.findAllByOrderByTipoAscTipoProdutoAscNomeAsc().stream()
            .map(OpcaoPizzaResponse::de)
            .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OpcaoPizzaResponse criar(@Valid @RequestBody AdminOpcaoPizzaRequest request) {
        OpcaoPizza opcao = new OpcaoPizza();
        preencher(opcao, request);
        return OpcaoPizzaResponse.de(repository.saveAndFlush(opcao));
    }

    @PutMapping("/{id}")
    public OpcaoPizzaResponse editar(@PathVariable Long id, @Valid @RequestBody AdminOpcaoPizzaRequest request) {
        OpcaoPizza opcao = buscarOpcao(id);
        preencher(opcao, request);
        return OpcaoPizzaResponse.de(repository.saveAndFlush(opcao));
    }

    @PatchMapping("/{id}/status")
    public OpcaoPizzaResponse atualizarStatus(
        @PathVariable Long id,
        @Valid @RequestBody AdminProdutoStatusRequest request
    ) {
        OpcaoPizza opcao = buscarOpcao(id);
        String status = request.status().trim().toLowerCase(Locale.ROOT);
        if (!"ativo".equals(status) && !"inativo".equals(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status deve ser ativo ou inativo");
        }
        opcao.setAtivo("ativo".equals(status));
        return OpcaoPizzaResponse.de(repository.saveAndFlush(opcao));
    }

    private OpcaoPizza buscarOpcao(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opção não encontrada"));
    }

    private void preencher(OpcaoPizza opcao, AdminOpcaoPizzaRequest request) {
        opcao.setNome(request.nome().trim());
        opcao.setTipo(request.tipo());
        opcao.setTipoProduto(request.tipoProduto());
        opcao.setPrecoAdicional(request.precoAdicional());
        if (request.ativo() != null) opcao.setAtivo(request.ativo());
    }
}
