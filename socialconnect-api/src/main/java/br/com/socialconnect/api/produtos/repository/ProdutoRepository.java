package br.com.socialconnect.api.produtos.repository;

import br.com.socialconnect.api.produtos.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

// JpaSpecificationExecutor permite combinar filtros opcionais (nome + categoria) com paginação
@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long>, JpaSpecificationExecutor<Produto> {

    // Verificação de unicidade no POST (sem diferenciar maiúsculas)
    boolean existsByNomeIgnoreCase(String nome);

    // Verificação de unicidade no PUT: ignora o próprio produto que está sendo atualizado
    boolean existsByNomeIgnoreCaseAndIdProdutoNot(String nome, Long idProduto);
}
