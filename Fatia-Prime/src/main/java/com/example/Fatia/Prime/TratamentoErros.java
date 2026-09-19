package com.example.Fatia.Prime;

import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class TratamentoErros {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacao(MethodArgumentNotValidException erro) {
        String mensagem = erro.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getDefaultMessage())
            .distinct()
            .sorted()
            .collect(Collectors.joining(". "));

        return erro(HttpStatus.BAD_REQUEST, mensagem.isBlank() ? "Dados inválidos" : mensagem);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> regra(ResponseStatusException erro) {
        HttpStatus status = HttpStatus.valueOf(erro.getStatusCode().value());
        return erro(status, erro.getReason() == null ? "Dados inválidos" : erro.getReason());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> duplicado() {
        return erro(HttpStatus.CONFLICT, "Não foi possível salvar os dados. Verifique os campos informados.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> jsonInvalido() {
        return erro(HttpStatus.BAD_REQUEST, "JSON inválido. Verifique o corpo da requisição.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> tipoInvalido() {
        return erro(HttpStatus.BAD_REQUEST, "Parâmetro informado é inválido.");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> rotaNaoEncontrada() {
        return erro(HttpStatus.NOT_FOUND, "Recurso não encontrado.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> inesperado(Exception erro) {
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor.");
    }

    private ResponseEntity<ErrorResponse> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status)
            .body(new ErrorResponse(status.value(), mensagem));
    }
}