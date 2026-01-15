package co.com.bancolombia.products.usecase.product.addproducts.validation;

import co.com.bancolombia.products.model.shared.model.ProductMainDTO;
import co.com.bancolombia.products.model.shared.cqrs.ContextData;
import co.com.bancolombia.products.model.shared.exception.BusinessException;
import co.com.bancolombia.products.model.shared.exception.ErrorCode;
import co.com.bancolombia.products.model.shared.policy.ProductKeyPolicy;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductValidatorTest {

    private final ProductKeyPolicy keyPolicy = new ProductKeyPolicy() {

        @Override
        public String normalize(String value) {
            if (value == null) return "";
            return value.trim().toLowerCase()
                    .replace("á","a").replace("é","e").replace("í","i").replace("ó","o").replace("ú","u");
        }

    };

    private final co.com.bancolombia.products.model.product.addproducts.validation.ProductValidator validator = new co.com.bancolombia.products.model.product.addproducts.validation.ProductValidator(keyPolicy);

    private ContextData ctx() {
        return new ContextData(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    @Test
    void testCase400_typeNotAllowed() {
        ProductMainDTO p = new ProductMainDTO(null,"Laptop", "Hogar", "1", "100", "COP");
        BusinessException ex = assertThrows(BusinessException.class, () -> validator.validate(p, ctx()));
        assertEquals(ErrorCode.ER400, ex.errorCode());
        assertFalse(ex.details().isEmpty());
        assertEquals("type", ex.details().get(0).field());
    }

    @Test
    void testCase400_nameHasSpecialChars() {
        ProductMainDTO p = new ProductMainDTO(null, "Lap@top", "Tecnología", "1", "100", "COP");
        BusinessException ex = assertThrows(BusinessException.class, () -> validator.validate(p, ctx()));
        assertEquals(ErrorCode.ER400, ex.errorCode());
        assertEquals("name", ex.details().get(0).field());
    }

    @Test
    void testCase400_nameTooLong() {
        String longName = "a".repeat(51);
        ProductMainDTO p = new ProductMainDTO(null, longName, "Moda", "1", "100", "COP");
        BusinessException ex = assertThrows(BusinessException.class, () -> validator.validate(p, ctx()));
        assertEquals(ErrorCode.ER400, ex.errorCode());
        assertEquals("name", ex.details().get(0).field());
    }

    @Test
    void testCase400_quantityGreaterThan1000() {
        ProductMainDTO p = new ProductMainDTO(null, "Arroz", "Alimento", "1001", "100", "COP");
        BusinessException ex = assertThrows(BusinessException.class, () -> validator.validate(p, ctx()));
        assertEquals(ErrorCode.ER400, ex.errorCode());
        assertEquals("quantity", ex.details().get(0).field());
    }

    @Test
    void testCase400_priceNotNumeric() {
        ProductMainDTO p = new ProductMainDTO(null, "Camiseta", "Moda", "1", "10a", "COP");
        BusinessException ex = assertThrows(BusinessException.class, () -> validator.validate(p, ctx()));
        assertEquals(ErrorCode.ER400, ex.errorCode());
        assertEquals("price", ex.details().get(0).field());
    }

    @Test
    void testCase400_priceLessOrEqualZero() {
        ProductMainDTO p = new ProductMainDTO(null, "Camiseta", "Moda", "1", "0", "COP");
        BusinessException ex = assertThrows(BusinessException.class, () -> validator.validate(p, ctx()));
        assertEquals(ErrorCode.ER400, ex.errorCode());
        assertEquals("price", ex.details().get(0).field());
    }

    @Test
    void testCase400_currencyNotCOP() {
        ProductMainDTO p = new ProductMainDTO(null, "Camiseta", "Moda", "1", "100", "USD");
        BusinessException ex = assertThrows(BusinessException.class, () -> validator.validate(p, ctx()));
        assertEquals(ErrorCode.ER400, ex.errorCode());
        assertEquals("currency", ex.details().get(0).field());
    }

    @Test
    void testCase201_validProductPassesValidation() {
        ProductMainDTO p = new ProductMainDTO(null, "Camiseta", "Moda", "10", "100", "COP");
        assertDoesNotThrow(() -> validator.validate(p, ctx()));
    }
}