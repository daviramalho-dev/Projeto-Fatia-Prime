package com.example.Fatia.Prime;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class TratamentoErros {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validacao(MethodArgumentNotValidException erro) {
        String mensagem = erro.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getDefaultMessage())
            .distinct()
            .sorted()
            .collect(Collectors.joining(". "));

        return ResponseEntity.badRequest()
            .body(Map.of("mensagem", mensagem));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> regra(ResponseStatusException erro) {
        return ResponseEntity.status(erro.getStatusCode())
            .body(Map.of(
                "mensagem",
                Objects.requireNonNullElse(erro.getReason(), "Dados inválidos")
            ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> duplicado() {
        return ResponseEntity.status(409)
            .body(Map.of(
                "mensagem",
                "Não foi possível salvar: verifique se o e-mail já está cadastrado"
            ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> jsonInvalido() {
        return ResponseEntity.badRequest()
            .body(Map.of(
                "mensagem",
                "Envie nome, e-mail e senha em um JSON válido"
            ));
    }
}