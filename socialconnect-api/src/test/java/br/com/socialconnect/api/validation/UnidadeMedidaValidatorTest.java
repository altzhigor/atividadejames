package br.com.socialconnect.api.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UnidadeMedidaValidatorTest {

    private final UnidadeMedidaValidator validator = new UnidadeMedidaValidator();

    @Test
    @DisplayName("Deve aceitar unidades de medida comuns")
    void deveAceitarUnidadesValidas() {
        // ARRANGE + ACT + ASSERT
        Assertions.assertTrue(validator.isValid("kg", null));
        Assertions.assertTrue(validator.isValid("unidade", null));
        Assertions.assertTrue(validator.isValid("cx/12", null));
    }

    @Test
    @DisplayName("Deve rejeitar unidades que começam com número ou têm símbolos")
    void deveRejeitarUnidadesInvalidas() {
        // ARRANGE + ACT + ASSERT
        Assertions.assertFalse(validator.isValid("5kg", null));
        Assertions.assertFalse(validator.isValid("kg;DROP", null));
    }

    @Test
    @DisplayName("Deve deixar nulo/vazio para o @NotBlank tratar")
    void deveIgnorarValorNuloOuVazio() {
        // ARRANGE + ACT + ASSERT
        Assertions.assertTrue(validator.isValid(null, null));
        Assertions.assertTrue(validator.isValid("  ", null));
    }
}
