package com.example.Fatia.Prime;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/pedidos")
public class AdminPedidoController {

    private static final List<String> STATUS_VALIDOS = List.of(
        "Pedido recebido",
        "Pedido em andamento",
        "Pedido concluído"
    );

    private final PedidoRepository repository;

    public AdminPedidoController(PedidoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Transactional
    public List<AdminPedidoResponse> listar(
        String codigo,
        String telefone,
        String nome,
        String status
    ) {
        validarStatus(status);
        String codigoBusca = normalizar(codigo);
        String telefoneBusca = normalizarTelefone(telefone);
        String nomeBusca = normalizar(nome);
        return repository.findAllForAdmin().stream()
            .peek(this::garantirMetadados)
            .filter(pedido -> corresponde(pedido, codigoBusca, telefoneBusca, nomeBusca, status))
            .map(AdminPedidoResponse::de)
            .toList();
    }

    @GetMapping("/{id}")
    @Transactional
    public AdminPedidoResponse detalhar(@PathVariable Long id) {
        Pedido pedido = repository.findByIdForAdmin(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));
        garantirMetadados(pedido);
        return AdminPedidoResponse.de(pedido);
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public AdminPedidoResponse atualizarStatus(
        @PathVariable Long id,
        @Valid @RequestBody AdminPedidoStatusRequest request
    ) {
        Pedido pedido = repository.findByIdForAdmin(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));
        String novoStatus = validarStatus(request.status());
        if (!transicaoPermitida(pedido.getStatus(), novoStatus)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Transição de status não permitida: " + pedido.getStatus() + " para " + novoStatus
            );
        }
        pedido.setStatus(novoStatus);
        return AdminPedidoResponse.de(repository.saveAndFlush(pedido));
    }

    private boolean corresponde(Pedido pedido, String codigo, String telefone, String nome, String status) {
        Usuario usuario = pedido.getUsuario();
        String pedidoCodigo = normalizar(pedido.getCodigo());
        String pedidoTelefone = normalizarTelefone(usuario != null ? usuario.getTelefone() : null);
        String pedidoNome = normalizar(usuario != null ? usuario.getNome() : null);
        return (codigo.isBlank() || pedidoCodigo.contains(codigo))
            && (telefone.isBlank() || pedidoTelefone.contains(telefone))
            && (nome.isBlank() || pedidoNome.contains(nome))
            && (status == null || status.isBlank() || status.equals(pedido.getStatus()));
    }

    private void garantirMetadados(Pedido pedido) {
        boolean alterado = false;
        if (pedido.getCodigo() == null || pedido.getCodigo().isBlank()) {
            pedido.setCodigo("FP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
            alterado = true;
        }
        if (pedido.getStatus() == null || pedido.getStatus().isBlank()) {
            pedido.setStatus("Pedido recebido");
            alterado = true;
        }
        if (alterado) repository.save(pedido);
    }

    private String validarStatus(String status) {
        if (status == null || status.isBlank()) return null;
        String valor = status.trim();
        if (!STATUS_VALIDOS.contains(valor)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status inválido");
        }
        return valor;
    }

    private boolean transicaoPermitida(String atual, String proximo) {
        if (atual == null || atual.equals(proximo)) return true;
        int atualIndice = STATUS_VALIDOS.indexOf(atual);
        int proximoIndice = STATUS_VALIDOS.indexOf(proximo);
        return atualIndice >= 0 && proximoIndice == atualIndice + 1;
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizarTelefone(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }
}
