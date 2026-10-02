package br.com.socialconnect.api.exception;

// Regra de negócio: o nome do produto é único (HTTP 409)
public class NomeProdutoDuplicadoException extends RuntimeException {

    private final String nome;

    public NomeProdutoDuplicadoException(String nome) {
        super("Já existe um produto com o nome: " + nome);
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
