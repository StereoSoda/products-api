package co.com.bancolombia.products.usecase.product.getproducts.validation;

import co.com.bancolombia.products.model.shared.cqrs.ContextData;
import co.com.bancolombia.products.model.shared.exception.BusinessException;
import co.com.bancolombia.products.model.shared.exception.ErrorCode;
import co.com.bancolombia.products.model.shared.policy.ProductKeyPolicy;

import java.util.Set;
import java.util.regex.Pattern;

public class GetProductsQueryValidator {

    private static final Pattern TEXT_ALLOWED = Pattern.compile("^[\\p{L}\\p{N} ]+$");
    private static final Pattern DIGITS_ONLY = Pattern.compile("^\\d+$");
    private static final Set<String> FILTERS_ALLOWED = Set.of("tipo", "nombre", "precio");
    private static final Set<String> TYPES_ALLOWED_NORMALIZED = Set.of("tecnologia", "moda", "alimento");
    private final ProductKeyPolicy keyPolicy;

    public GetProductsQueryValidator(ProductKeyPolicy keyPolicy) {
        this.keyPolicy = keyPolicy;
    }

    /**
     * Reglas:
     * - Si no vienen filtros: OK (devuelve sin lanzar error)
     * - Si viene solo uno (filter o value): ER400
     * - Si vienen ambos: valida el filtro y el valor
     */
    public void validate(String anyFilter, String anyValue, ContextData ctx) {
        boolean filterBlank = isBlank(anyFilter);
        boolean valueBlank  = isBlank(anyValue);

        // sin filtro
        if (filterBlank && valueBlank) return;

        // incompleto
        if (filterBlank || valueBlank) {
            throw BusinessException.withContext(ErrorCode.ER400, ctx);
        }

        // completo = validar
        String filter = keyPolicy.normalize(anyFilter);
        if (!FILTERS_ALLOWED.contains(filter)) {
            throw BusinessException.withContext(ErrorCode.ER400, ctx);
        }

        switch (filter) {
            case "tipo" -> validateType(anyValue, ctx);
            case "nombre" -> validateName(anyValue, ctx);
            case "precio" -> validatePrice(anyValue, ctx);
            default -> throw BusinessException.withContext(ErrorCode.ER400, ctx);
        }
    }

    private void validateType(String v, ContextData ctx) {
        validateText(v, 30, ctx);
        String norm = keyPolicy.normalize(v);
        if (!TYPES_ALLOWED_NORMALIZED.contains(norm)) {
            throw BusinessException.withContext(ErrorCode.ER400, ctx);
        }
    }

    private void validateName(String v, ContextData ctx) {
        validateText(v, 50, ctx);
    }

    private void validatePrice(String v, ContextData ctx) {
        if (isBlank(v)) throw BusinessException.withContext(ErrorCode.ER400, ctx);

        String t = v.trim();
        if (!DIGITS_ONLY.matcher(t).matches()) throw BusinessException.withContext(ErrorCode.ER400, ctx);

        int n;
        try { n = Integer.parseInt(t); }
        catch (Exception ex) { throw BusinessException.withContext(ErrorCode.ER400, ctx); }

        if (n <= 0) throw BusinessException.withContext(ErrorCode.ER400, ctx);
    }

    private void validateText(String v, int maxLen, ContextData ctx) {
        if (isBlank(v)) throw BusinessException.withContext(ErrorCode.ER400, ctx);

        String t = v.trim();
        if (t.length() > maxLen) throw BusinessException.withContext(ErrorCode.ER400, ctx);
        if (!TEXT_ALLOWED.matcher(t).matches()) throw BusinessException.withContext(ErrorCode.ER400, ctx);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}