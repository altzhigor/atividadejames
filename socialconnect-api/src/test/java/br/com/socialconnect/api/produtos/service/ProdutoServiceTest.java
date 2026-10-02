package br.com.socialconnect.api.produtos.service;

import br.com.socialconnect.api.exception.EstoqueNegativoException;
import br.com.socialconnect.api.exception.NomeProdutoDuplicadoException;
import br.com.socialconnect.api.exception.RecursoNaoEncontradoException;
import br.com.socialconnect.api.produtos.dto.ProdutoRequestDTO;
import br.com.socialconnect.api.produtos.dto.ProdutoResponseDTO;
import br.com.socialconnect.api.produtos.model.CategoriaProduto;
import br.com.socialconnect.api.produtos.model.Produto;
import br.com.socialconnect.api.produtos.repository.ProdutoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository repository;

    @InjectMocks
    private ProdutoServiceImpl service;

    @Test
    @DisplayName("Deve criar produto com data de cadastro de hoje e alerta de estoque baixo")
    void deveCriarProdutoQuandoDadosValidos() {
        // ==========================================
        // ARRANGE: Preparar o cenário
        // ==========================================
        ProdutoRequestDTO dto = new ProdutoRequestDTO(
                "Arroz 5kg", CategoriaProduto.ALIMENTO, 3, 10, "unidade"
        );
        Mockito.when(repository.existsByNomeIgnoreCase("Arroz 5kg")).thenReturn(false);
        Mockito.when(repository.save(Mockito.any())).thenAnswer(inv -> {
            Produto p = inv.getArgument(0);
            p.setIdProduto(1L);
            return p;
        });

        // ==========================================
        // ACT: Executar a ação
        // ==========================================
        ProdutoResponseDTO resultado = service.criar(dto);

        // ==========================================
        // ASSERT: Verificar o resultado
        // ==========================================
        ArgumentCaptor<Produto> captor = ArgumentCaptor.forClass(Produto.class);
        Mockito.verify(repository, Mockito.times(1)).save(captor.capture());
        Assertions.assertEquals("Arroz 5kg", captor.getValue().getNome());
        Assertions.assertEquals(LocalDate.now(), captor.getValue().getDataCadastro());
        Assertions.assertEquals(1L, resultado.idProduto());
        Assertions.assertTrue(resultado.estoqueBaixo(), "3 < 10 deve disparar o alerta de estoque baixo");
    }

    @Test
    @DisplayName("Deve lançar EstoqueNegativoException quando estoque atual for negativo")
    void deveLancarExcecaoQuandoEstoqueNegativo() {
        // ARRANGE
        ProdutoRequestDTO dto = new ProdutoRequestDTO(
                "Feijão 1kg", CategoriaProduto.ALIMENTO, -1, 5, "kg"
        );

        // ACT + ASSERT
        Assertions.assertThrows(EstoqueNegativoException.class, () -> service.criar(dto));
        Mockito.verify(repository, Mockito.never()).save(Mockito.any());
    }

    @Test
    @DisplayName("Deve lançar NomeProdutoDuplicadoException quando o nome já existir")
    void deveLancarExcecaoQuandoNomeDuplicado() {
        // ARRANGE
        ProdutoRequestDTO dto = new ProdutoRequestDTO(
                "Arroz 5kg", CategoriaProduto.ALIMENTO, 10, 5, "unidade"
        );
        Mockito.when(repository.existsByNomeIgnoreCase("Arroz 5kg")).thenReturn(true);

        // ACT + ASSERT
        Assertions.assertThrows(NomeProdutoDuplicadoException.class, () -> service.criar(dto));
        Mockito.verify(repository, Mockito.never()).save(Mockito.any());
    }

    @Test
    @DisplayName("estoqueBaixo deve ser false quando estoque atual for igual ao mínimo")
    void naoDeveAlertarEstoqueBaixoQuandoEstoqueIgualAoMinimo() {
        // ARRANGE
        Produto existente = Produto.builder()
                .idProduto(1L).nome("Sabonete").categoria(CategoriaProduto.HIGIENE)
                .estoqueAtual(10).estoqueMinimo(10).unidadeMedida("unidade")
                .dataCadastro(LocalDate.of(2026, 9, 1))
                .build();
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(existente));

        // ACT
        ProdutoResponseDTO resultado = service.buscarPorId(1L);

        // ASSERT
        Assertions.assertFalse(resultado.estoqueBaixo(), "Alerta só quando estoqueAtual < estoqueMinimo");
    }

    @Test
    @DisplayName("PUT deve manter a data de cadastro original e aceitar o próprio nome")
    void deveAtualizarProdutoMantendoDataCadastro() {
        // ARRANGE
        Produto existente = Produto.builder()
                .idProduto(1L).nome("Arroz 5kg").categoria(CategoriaProduto.ALIMENTO)
                .estoqueAtual(3).estoqueMinimo(10).unidadeMedida("unidade")
                .dataCadastro(LocalDate.of(2026, 9, 1))
                .build();
        Mockito.when(repository.findById(1L)).thenReturn(Optional.of(existente));
        Mockito.when(repository.existsByNomeIgnoreCaseAndIdProdutoNot("Arroz 5kg", 1L)).thenReturn(false);
        Mockito.when(repository.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));
        ProdutoRequestDTO dto = new ProdutoRequestDTO(
                "Arroz 5kg", CategoriaProduto.ALIMENTO, 50, 10, "unidade"
        );

        // ACT
        ProdutoResponseDTO resultado = service.atualizar(1L, dto);

        // ASSERT
        Assertions.assertEquals(50, resultado.estoqueAtual());
        Assertions.assertEquals(LocalDate.of(2026, 9, 1), resultado.dataCadastro());
        Assertions.assertFalse(resultado.estoqueBaixo());
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao deletar ID inexistente")
    void deveLancarExcecaoAoDeletarInexistente() {
        // ARRANGE
        Mockito.when(repository.existsById(99L)).thenReturn(false);

        // ACT + ASSERT
        Assertions.assertThrows(RecursoNaoEncontradoException.class, () -> service.deletar(99L));
        Mockito.verify(repository, Mockito.never()).deleteById(Mockito.any());
    }
}
