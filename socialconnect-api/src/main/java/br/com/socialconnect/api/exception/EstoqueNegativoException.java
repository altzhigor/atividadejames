package br.com.socialconnect.api.exception;

// Regra de negócio: nenhuma operação pode resultar em estoqueAtual < 0 (HTTP 422)
public class EstoqueNegativoException extends RuntimeException {

    private final Integer estoque;

    public EstoqueNegativoException(Integer estoque) {
        super("Estoque não pode ser negativo: " + estoque);
        this.estoque = estoque;
    }

    public Integer getEstoque() {
        return estoque;
    }
}
