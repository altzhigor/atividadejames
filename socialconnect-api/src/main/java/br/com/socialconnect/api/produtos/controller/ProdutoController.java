package br.com.socialconnect.api.produtos.controller;

import br.com.socialconnect.api.exception.ProblemDetail;
import br.com.socialconnect.api.produtos.dto.ProdutoRequestDTO;
import br.com.socialconnect.api.produtos.dto.ProdutoResponseDTO;
import br.com.socialconnect.api.produtos.model.CategoriaProduto;
import br.com.socialconnect.api.produtos.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/produtos")
@Tag(name = "Produtos", description = "API para controle do estoque de produtos doados")
public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista produtos",
            description = "Lista paginada com filtros opcionais por nome (parcial, sem diferenciar maiúsculas) e categoria. "
                    + "Exemplo: /api/v1/produtos?page=0&size=10&sort=nome,asc&categoria=ALIMENTO")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @ApiResponse(responseCode = "400", description = "Parâmetros inválidos (categoria ou ordenação inexistente)",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Page<ProdutoResponseDTO>> listar(
            @Parameter(description = "Nome para filtrar (parcial)", example = "arroz")
            @RequestParam(required = false) String nome,
            @Parameter(description = "Categoria para filtrar", example = "ALIMENTO")
            @RequestParam(required = false) CategoriaProduto categoria,
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(service.listar(nome, categoria, pageable));
    }

    @GetMapping("/{idProduto}")
    @Operation(summary = "Busca produto por ID")
    @ApiResponse(responseCode = "200", description = "Produto encontrado")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ProdutoResponseDTO> buscarPorId(
            @Parameter(description = "ID do produto", example = "1") @PathVariable Long idProduto) {
        return ResponseEntity.ok(service.buscarPorId(idProduto));
    }

    @PostMapping
    @Operation(summary = "Cria um novo produto",
            description = "Cadastra um produto no estoque. O nome deve ser único e o estoque atual não pode ser negativo")
    @ApiResponse(responseCode = "201", description = "Produto criado com sucesso (cabeçalho Location com a URL do recurso)")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Já existe produto com este nome",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "Estoque atual negativo",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ProdutoResponseDTO> criar(@Valid @RequestBody ProdutoRequestDTO dto) {
        ProdutoResponseDTO salvo = service.criar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{idProduto}")
                .buildAndExpand(salvo.idProduto())
                .toUri();
        return ResponseEntity.created(location).body(salvo);
    }

    @PutMapping("/{idProduto}")
    @Operation(summary = "Substitui um produto", description = "Atualização total: todos os campos do corpo substituem os atuais")
    @ApiResponse(responseCode = "200", description = "Produto atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Produto não encontrado",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Já existe outro produto com este nome",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "Estoque atual negativo",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ProdutoResponseDTO> atualizar(
            @Parameter(description = "ID do produto", example = "1") @PathVariable Long idProduto,
            @Valid @RequestBody ProdutoRequestDTO dto) {
        return ResponseEntity.ok(service.atualizar(idProduto, dto));
    }

    @DeleteMapping("/{idProduto}")
    @Operation(summary = "Remove um produto")
    @ApiResponse(responseCode = "204", description = "Produto removido")
    @ApiResponse(responseCode = "404", description = "Produto não encontrado",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do produto", example = "1") @PathVariable Long idProduto) {
        service.deletar(idProduto);
        return ResponseEntity.noContent().build();
    }
}
