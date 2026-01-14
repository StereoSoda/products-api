package co.com.bancolombia.products.usecase.product.addproducts.validation;

import co.com.bancolombia.products.model.product.model.Product;
import co.com.bancolombia.products.model.shared.cqrs.ContextData;
import co.com.bancolombia.products.model.shared.exception.BusinessException;
import co.com.bancolombia.products.model.shared.exception.ErrorCode;
import co.com.bancolombia.products.model.shared.exception.ValidationError;
import co.com.bancolombia.products.model.shared.policy.ProductKeyPolicy;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ProductValidator {

    // Permite letras (incluye acentos), números y espacio
    private static final Pattern TEXT_ALLOWED = Pattern.compile("^[\\p{L}\\p{N} ]+$");
    private static final Pattern DIGITS_ONLY = Pattern.compile("^\\d+$");
    private static final Set<String> TYPES_ALLOWED_NORMALIZED = Set.of("tecnologia", "moda", "alimento");

    private final ProductKeyPolicy keyPolicy;

    public ProductValidator(ProductKeyPolicy keyPolicy) {
        this.keyPolicy = keyPolicy;
    }

    public void validate(Product p, ContextData ctx) {
        if (p == null) {
            throw invalid(ctx, List.of(new ValidationError("product", "Producto es requerido")));
        }

        validateTextField("name", p.name(), 50, ctx);
        validateTextField("type", p.type(), 30, ctx);
        validateDigitsField("quantity", p.quantity(), 1, 1000, ctx, "Cantidad máxima permitida es 1000");
        validatePriceField("price", p.price(), ctx);
        validateCurrency(p.currency(), ctx);

        // Type allowed
        String normalizedType = keyPolicy.normalize(p.type());
        if (!TYPES_ALLOWED_NORMALIZED.contains(normalizedType)) {
            throw invalid(ctx, List.of(
                    new ValidationError("type", "Tipo inválido. Permitidos: Tecnología, Moda y Alimento")
            ));
        }
    }

    private void validateTextField(String field, String value, int maxLen, ContextData ctx) {
        if (value == null || value.isBlank()) {
            throw invalid(ctx, List.of(new ValidationError(field, "Campo requerido")));
        }
        if (value.length() > maxLen) {
            throw invalid(ctx, List.of(new ValidationError(field, "Longitud máxima " + maxLen)));
        }
        if (!TEXT_ALLOWED.matcher(value).matches()) {
            throw invalid(ctx, List.of(new ValidationError(field, "No se permiten caracteres especiales")));
        }
    }

    private void validateDigitsField(String field, String value, int min, int max, ContextData ctx, String maxMsg) {
        if (value == null || value.isBlank()) {
            throw invalid(ctx, List.of(new ValidationError(field, "Campo requerido")));
        }
        if (!DIGITS_ONLY.matcher(value).matches()) {
            throw invalid(ctx, List.of(new ValidationError(field, "Debe ser numérico")));
        }

        int n;
        try {
            n = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw invalid(ctx, List.of(new ValidationError(field, "Debe ser numérico")));
        }

        if (n < min) {
            throw invalid(ctx, List.of(new ValidationError(field, "Debe ser mayor o igual a " + min)));
        }
        if (n > max) {
            throw invalid(ctx, List.of(new ValidationError(field, maxMsg)));
        }
    }

    private void validatePriceField(String field, String value, ContextData ctx) {
        if (value == null || value.isBlank()) {
            throw invalid(ctx, List.of(new ValidationError(field, "Campo requerido")));
        }
        if (!DIGITS_ONLY.matcher(value).matches()) {
            throw invalid(ctx, List.of(new ValidationError(field, "Debe ser numérico")));
        }
        int n;
        try {
            n = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw invalid(ctx, List.of(new ValidationError(field, "Debe ser numérico")));
        }
        if (n <= 0) {
            throw invalid(ctx, List.of(new ValidationError(field, "Debe ser mayor a 0")));
        }
    }

    private void validateCurrency(String currency, ContextData ctx) {
        if (currency == null || currency.isBlank()) {
            throw invalid(ctx, List.of(new ValidationError("currency", "Campo requerido")));
        }
        if (!TEXT_ALLOWED.matcher(currency).matches()) {
            throw invalid(ctx, List.of(new ValidationError("currency", "No se permiten caracteres especiales")));
        }
        if (!"cop".equalsIgnoreCase(currency.trim())) {
            throw invalid(ctx, List.of(new ValidationError("currency", "Moneda inválida. Debe ser COP")));
        }
    }

    private BusinessException invalid(ContextData ctx, List<ValidationError> details) {
        return BusinessException.withContext(ErrorCode.ER400, ctx, details);
    }
}