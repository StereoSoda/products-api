package co.com.bancolombia.products.usecase.product.getproducts.validation;

import co.com.bancolombia.products.model.shared.cqrs.ContextData;
import co.com.bancolombia.products.model.shared.exception.BusinessException;
import co.com.bancolombia.products.model.shared.exception.ErrorCode;
import co.com.bancolombia.products.model.shared.policy.ProductKeyPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GetProductsQueryValidatorTest {

    private final ProductKeyPolicy keyPolicy = new ProductKeyPolicy();
    private final GetProductsQueryValidator validator = new GetProductsQueryValidator(keyPolicy);

    private final ContextData ctx = new ContextData(
            "11111111-1111-1111-1111-111111111111",
            "22222222-2222-2222-2222-222222222222"
    );

    @Test
    void testCase200_noFilterParams_ok() {
        assertDoesNotThrow(() -> validator.validate(null, null, ctx));
        assertDoesNotThrow(() -> validator.validate("   ", "   ", ctx));
    }

    @Test
    void testCase400_onlyFilterProvided_returns400() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validate("tipo", null, ctx));

        assertEquals(ErrorCode.ER400, ex.errorCode());
    }

    @Test
    void testCase400_onlyValueProvided_returns400() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validate(null, "tecnologia", ctx));

        assertEquals(ErrorCode.ER400, ex.errorCode());
    }

    @Test
    void testCase400_filterNotAllowed_returns400() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validate("marca", "sony", ctx));

        assertEquals(ErrorCode.ER400, ex.errorCode());
    }

    @Test
    void testCase200_filterTipo_validType_ok() {
        assertDoesNotThrow(() -> validator.validate("tipo", "Tecnología", ctx));
    }

    @Test
    void testCase400_filterTipo_invalidType_returns400() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validate("tipo", "Hogar", ctx));

        assertEquals(ErrorCode.ER400, ex.errorCode());
    }

    @Test
    void testCase200_filterNombre_valid_ok() {
        assertDoesNotThrow(() -> validator.validate("nombre", "Laptop Pro 14", ctx));
    }

    @Test
    void testCase400_filterNombre_invalidChars_returns400() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validate("nombre", "Laptop@@@", ctx));

        assertEquals(ErrorCode.ER400, ex.errorCode());
    }

    @Test
    void testCase200_filterPrecio_digits_ok() {
        assertDoesNotThrow(() -> validator.validate("precio", "100", ctx));
    }

    @Test
    void testCase400_filterPrecio_notDigits_returns400() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validate("precio", "10A", ctx));

        assertEquals(ErrorCode.ER400, ex.errorCode());
    }

    @Test
    void testCase400_filterPrecio_zeroOrNegative_returns400() {
        assertEquals(ErrorCode.ER400,
                assertThrows(BusinessException.class, () -> validator.validate("precio", "0", ctx)).errorCode());

        assertEquals(ErrorCode.ER400,
                assertThrows(BusinessException.class, () -> validator.validate("precio", "-1", ctx)).errorCode());
    }
}