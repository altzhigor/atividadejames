package br.com.socialconnect.api.produtos.service;

import br.com.socialconnect.api.exception.EstoqueNegativoException;
import br.com.socialconnect.api.exception.NomeProdutoDuplicadoException;
import br.com.socialconnect.api.exception.RecursoNaoEncontradoException;
import br.com.socialconnect.api.produtos.dto.ProdutoRequestDTO;
import br.com.socialconnect.api.produtos.dto.ProdutoResponseDTO;
import br.com.socialconnect.api.produtos.model.CategoriaProduto;
import br.com.socialconnect.api.produtos.model.Produto;
import br.com.socialconnect.api.produtos.repository.ProdutoRepository;
import br.com.socialconnect.api.produtos.repository.ProdutoSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoServiceImpl(ProdutoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> listar(String nome, CategoriaProduto categoria, Pageable pageable) {
        return repository
                .findAll(ProdutoSpecifications.filtrar(nome, categoria), pageable)
                .map(this::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidade(id));
    }

    // POST
    @Override
    @Transactional
    public ProdutoResponseDTO criar(ProdutoRequestDTO dto) {
        validarEstoqueNaoNegativo(dto.estoqueAtual());          // 422
        String nome = dto.nome().trim();
        if (repository.existsByNomeIgnoreCase(nome)) {           // 409
            throw new NomeProdutoDuplicadoException(nome);
        }
        Produto entity = Produto.builder()
                .nome(nome)
                .categoria(dto.categoria())
                .estoqueAtual(dto.estoqueAtual())
                .estoqueMinimo(dto.estoqueMinimo())
                .unidadeMedida(dto.unidadeMedida().trim())
                .dataCadastro(LocalDate.now())
                .build();
        return toResponseDTO(repository.save(entity));
    }

    // PUT (substituição total; a data de cadastro original é preservada)
    @Override
    @Transactional
    public ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO dto) {
        Produto entity = buscarEntidade(id);                     // 404
        validarEstoqueNaoNegativo(dto.estoqueAtual());           // 422
        String nome = dto.nome().trim();
        if (repository.existsByNomeIgnoreCaseAndIdProdutoNot(nome, id)) { // 409 (outro produto com o mesmo nome)
            throw new NomeProdutoDuplicadoException(nome);
        }
        entity.setNome(nome);
        entity.setCategoria(dto.categoria());
        entity.setEstoqueAtual(dto.estoqueAtual());
        entity.setEstoqueMinimo(dto.estoqueMinimo());
        entity.setUnidadeMedida(dto.unidadeMedida().trim());
        return toResponseDTO(repository.save(entity));
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Produto não encontrado: " + id);
        }
        repository.deleteById(id);
    }

    private Produto buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado: " + id));
    }

    // Regra de negócio: nenhuma operação pode resultar em estoqueAtual < 0
    private void validarEstoqueNaoNegativo(Integer estoqueAtual) {
        if (estoqueAtual < 0) {
            throw new EstoqueNegativoException(estoqueAtual);
        }
    }

    // Mapeador Entity -> Response DTO (estoqueBaixo é calculado aqui)
    private ProdutoResponseDTO toResponseDTO(Produto e) {
        return new ProdutoResponseDTO(
                e.getIdProduto(), e.getNome(), e.getCategoria(),
                e.getEstoqueAtual(), e.getEstoqueMinimo(), e.getUnidadeMedida(),
                e.getDataCadastro(), e.getEstoqueAtual() < e.getEstoqueMinimo()
        );
    }
}
