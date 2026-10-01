package com.example.Fatia.Prime;

import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/frete")
public class FreteController {

    private final FaixaFreteRepository repository;

    public FreteController(FaixaFreteRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/consulta")
    public FreteResponse consultar(@RequestParam(required = false) String cep) {
        if (cep == null || !cep.trim().matches("[0-9]{5}-?[0-9]{3}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um CEP válido.");
        }

        String cepNormalizado = cep.replaceAll("\\D", "");
        FaixaFrete faixa = repository
            .findFirstByAtivoTrueAndCepInicialLessThanEqualAndCepFinalGreaterThanEqualOrderByCepInicialAsc(
                cepNormalizado,
                cepNormalizado
            )
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Infelizmente, ainda não entregamos nessa região."
            ));

        return FreteResponse.de(cepNormalizado, faixa);
    }
}
