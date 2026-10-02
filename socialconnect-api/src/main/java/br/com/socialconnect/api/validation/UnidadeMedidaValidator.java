package br.com.socialconnect.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

// Aceita unidades como "kg", "unidade", "litro", "pacote", "cx/12": começa com letra e só tem letras, espaço, ponto ou barra
public class UnidadeMedidaValidator implements ConstraintValidator<UnidadeMedidaValida, String> {

    private static final Pattern PADRAO = Pattern.compile("^\\p{L}[\\p{L}0-9 ./]*$");

    @Override
    public boolean isValid(String unidade, ConstraintValidatorContext context) {
        if (unidade == null || unidade.isBlank()) return true; // @NotBlank cuida da obrigatoriedade
        return PADRAO.matcher(unidade.trim()).matches();
    }
}
