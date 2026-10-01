package com.example.Fatia.Prime;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/opcoes-pizza")
public class OpcaoPizzaController {

    private final OpcaoPizzaRepository repository;

    public OpcaoPizzaController(OpcaoPizzaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<OpcaoPizzaResponse> listar(@RequestParam TipoProdutoPizza tipoProduto) {
        return repository.findByAtivoTrueAndTipoProdutoOrderByTipoAscNomeAsc(tipoProduto).stream()
            .map(OpcaoPizzaResponse::de)
            .toList();
    }
}
