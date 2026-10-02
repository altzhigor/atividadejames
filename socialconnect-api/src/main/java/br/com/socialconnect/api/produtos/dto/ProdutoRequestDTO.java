package br.com.socialconnect.api.produtos.dto;

import br.com.socialconnect.api.produtos.model.CategoriaProduto;
import br.com.socialconnect.api.validation.UnidadeMedidaValida;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// DTO de ENTRADA: apenas campos que o cliente pode enviar (idProduto e dataCadastro são gerados pelo servidor)
@Schema(description = "Dados para cadastrar ou substituir um produto do estoque")
public record ProdutoRequestDTO(
        @Schema(description = "Nome do produto (único, sem diferenciar maiúsculas)", example = "Arroz 5kg")
        @NotBlank(message = "{produto.nome.obrigatorio}")
        @Size(max = 150, message = "{produto.nome.tamanho}")
        String nome,

        @Schema(description = "Categoria do produto", example = "ALIMENTO")
        @NotNull(message = "{produto.categoria.obrigatoria}")
        CategoriaProduto categoria,

        // Sem @Min aqui de propósito: valor negativo é regra de negócio e deve virar 422 (feito no Service),
        // não 400 de validação de formato.
        @Schema(description = "Quantidade atual em estoque. Não pode ser negativa (422 se for)", example = "3")
        @NotNull(message = "{produto.estoqueAtual.obrigatorio}")
        Integer estoqueAtual,

        @Schema(description = "Quantidade mínima desejada em estoque (>= 0). Abaixo dela, estoqueBaixo = true", example = "10")
        @NotNull(message = "{produto.estoqueMinimo.obrigatorio}")
        @Min(value = 0, message = "{produto.estoqueMinimo.minimo}")
        Integer estoqueMinimo,

        @Schema(description = "Unidade de medida", example = "unidade")
        @NotBlank(message = "{produto.unidade.obrigatoria}")
        @Size(max = 20, message = "{produto.unidade.tamanho}")
        @UnidadeMedidaValida
        String unidadeMedida
) {}
