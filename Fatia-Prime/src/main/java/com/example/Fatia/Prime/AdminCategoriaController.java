package com.example.Fatia.Prime;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Provides ADMIN-only category management endpoints. */
@RestController
@RequestMapping("/api/admin/categorias")
@Transactional
public class AdminCategoriaController {

    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;

    /**
     * Creates the controller with the category and product repositories.
     *
     * @param categoriaRepository repository used to read and persist categories
     * @param produtoRepository repository used to prevent deleting categories assigned to products
     */
    public AdminCategoriaController(CategoriaRepository categoriaRepository, ProdutoRepository produtoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
    }

    /**
     * Lists all categories for authenticated administrators.
     *
     * @return all categories
     */
    @GetMapping
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll().stream()
            .map(CategoriaResponse::de)
            .toList();
    }

    /**
     * Finds an administrative category by its identifier.
     *
     * @param id category identifier
     * @return the matching category
     */
    @GetMapping("/{id}")
    public CategoriaResponse buscarPorId(@PathVariable Long id) {
        return CategoriaResponse.de(buscarCategoria(id));
    }

    /**
     * Creates a category after validating its name and uniqueness.
     *
     * @param request validated category data
     * @return the created category
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaResponse criar(@Valid @RequestBody CategoriaRequest request) {
        String nome = request.nome().trim();
        if (categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma categoria com esse nome.");
        }
        return CategoriaResponse.de(categoriaRepository.saveAndFlush(new Categoria(nome)));
    }

    /**
     * Updates the name of an existing category.
     *
     * @param id category identifier
     * @param request validated category data
     * @return the updated category
     */
    @PutMapping("/{id}")
    public CategoriaResponse editar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        Categoria categoria = buscarCategoria(id);
        String nome = request.nome().trim();
        if (categoriaRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma categoria com esse nome.");
        }
        categoria.setNome(nome);
        return CategoriaResponse.de(categoriaRepository.saveAndFlush(categoria));
    }

    /**
     * Deletes a category when no product references it.
     *
     * @param id category identifier
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        Categoria categoria = buscarCategoria(id);
        if (produtoRepository.existsByCategoria_Id(id)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Não é possível excluir uma categoria vinculada a produtos."
            );
        }
        categoriaRepository.delete(categoria);
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));
    }
}
