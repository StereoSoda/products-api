package co.com.bancolombia.products.usecase.product.addproducts;

import co.com.bancolombia.products.model.product.addproducts.gateway.ProductRepositoryGateway;
import co.com.bancolombia.products.model.shared.model.ProductMainDTO;
import co.com.bancolombia.products.model.shared.cqrs.Command;
import co.com.bancolombia.products.model.shared.cqrs.ContextData;
import co.com.bancolombia.products.model.shared.exception.BusinessException;
import co.com.bancolombia.products.model.shared.exception.ErrorCode;
import co.com.bancolombia.products.model.shared.policy.ProductKeyPolicy;
import co.com.bancolombia.products.model.product.addproducts.validation.ProductValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddProductsUseCaseTest {

    @Mock
    ProductRepositoryGateway repository;

    @Mock
    ProductValidator validator;

    @Mock
    ProductKeyPolicy keyPolicy;

    AddProductsUseCase useCase;

    private ContextData ctx;

    @BeforeEach
    void setUp() {
        useCase = new AddProductsUseCase(repository, validator, keyPolicy);
        ctx = new ContextData(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    private ProductMainDTO p(String name, String type, String quantity, String price, String currency) {
        return new ProductMainDTO(null, name, type, quantity, price, currency);
    }

    private Command<AddProductsPayload, ContextData> cmd(List<ProductMainDTO> products) {
        return new Command<>(new AddProductsPayload(products), ctx);
    }

    @Test
    void testCase201_success_savesAll_whenNoDuplicatesAndNotExists() {
        // Arrange
        ProductMainDTO p1 = p("Laptop", "Tecnología", "1", "100", "COP");
        ProductMainDTO p2 = p("Camisa", "Moda", "2", "200", "COP");
        List<ProductMainDTO> products = List.of(p1, p2);

        when(keyPolicy.buildKey(p1)).thenReturn("k1");
        when(keyPolicy.buildKey(p2)).thenReturn("k2");

        when(repository.existsByKey("k1")).thenReturn(Mono.just(false));
        when(repository.existsByKey("k2")).thenReturn(Mono.just(false));

        when(repository.saveAll(products)).thenReturn(Mono.empty());

        Mono<Void> result = useCase.execute(cmd(products));

        StepVerifier.create(result)
                .verifyComplete();

        verify(validator).validate(p1, ctx);
        verify(validator).validate(p2, ctx);

        verify(repository, times(1)).saveAll(products);
    }

    @Test
    void testCase400_payloadNull() {
        Command<AddProductsPayload, ContextData> command = new Command<>(null, ctx);

        StepVerifier.create(useCase.execute(command))
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof BusinessException);
                    BusinessException be = (BusinessException) ex;
                    assertEquals(ErrorCode.ER400, be.errorCode());
                })
                .verify();

        verifyNoInteractions(validator, keyPolicy, repository);
    }

    @Test
    void testCase400_productsNull() {
        Command<AddProductsPayload, ContextData> command = new Command<>(new AddProductsPayload(null), ctx);

        StepVerifier.create(useCase.execute(command))
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof BusinessException);
                    BusinessException be = (BusinessException) ex;
                    assertEquals(ErrorCode.ER400, be.errorCode());
                })
                .verify();

        verifyNoInteractions(validator, keyPolicy, repository);
    }

    @Test
    void testCase400_productsEmpty() {
        StepVerifier.create(useCase.execute(cmd(List.of())))
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof BusinessException);
                    BusinessException be = (BusinessException) ex;
                    assertEquals(ErrorCode.ER400, be.errorCode());
                })
                .verify();

        verifyNoInteractions(validator, keyPolicy, repository);
    }

    @Test
    void testCase400_validatorThrows() {
        ProductMainDTO p1 = p("Laptop", "Tecnología", "1", "100", "COP");
        List<ProductMainDTO> products = List.of(p1);

        doThrow(BusinessException.withContext(ErrorCode.ER400, ctx))
                .when(validator).validate(p1, ctx);

        StepVerifier.create(useCase.execute(cmd(products)))
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof BusinessException);
                    BusinessException be = (BusinessException) ex;
                    assertEquals(ErrorCode.ER400, be.errorCode());
                })
                .verify();

        verify(validator).validate(p1, ctx);
        verifyNoInteractions(keyPolicy, repository);
    }

    @Test
    void testCase400_duplicateInRequest_returns400() {
        ProductMainDTO p1 = p("Laptop", "Tecnología", "1", "100", "COP");
        ProductMainDTO p2 = p("Laptop", "Tecnología", "1", "100", "COP");
        List<ProductMainDTO> products = List.of(p1, p2);

        when(keyPolicy.buildKey(any(ProductMainDTO.class))).thenReturn("k1");

        StepVerifier.create(useCase.execute(cmd(products)))
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof BusinessException);
                    BusinessException be = (BusinessException) ex;
                    assertEquals(ErrorCode.ER400, be.errorCode());
                })
                .verify();

        verify(validator, times(2)).validate(any(ProductMainDTO.class), eq(ctx));

        verify(keyPolicy, times(2)).buildKey(any(ProductMainDTO.class));

        verify(repository, never()).existsByKey(anyString());
        verify(repository, never()).saveAll(anyList());
    }

    @Test
    void testCase409_productAlreadyExists_returns409() {
        ProductMainDTO p1 = p("Laptop", "Tecnología", "1", "100", "COP");
        List<ProductMainDTO> products = List.of(p1);

        when(keyPolicy.buildKey(p1)).thenReturn("k1");

        when(repository.existsByKey("k1")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.execute(cmd(products)))
                .expectErrorSatisfies(ex -> {
                    assertTrue(ex instanceof BusinessException);
                    BusinessException be = (BusinessException) ex;
                    assertEquals(ErrorCode.ER409, be.errorCode());
                })
                .verify();

        verify(validator).validate(p1, ctx);
        verify(repository, times(1)).existsByKey("k1");
        verify(repository, never()).saveAll(anyList());
    }

    @Test
    void testCase500_repositoryExistsByKeyErrors_propagates500() {
        ProductMainDTO p1 = p("Laptop", "Tecnología", "1", "100", "COP");
        List<ProductMainDTO> products = List.of(p1);

        when(keyPolicy.buildKey(p1)).thenReturn("k1");

        when(repository.existsByKey("k1")).thenReturn(Mono.error(new RuntimeException("db down")));

        StepVerifier.create(useCase.execute(cmd(products)))
                .expectError(RuntimeException.class)
                .verify();

        verify(repository, times(1)).existsByKey("k1");
        verify(repository, never()).saveAll(anyList());
    }

    @Test
    void testCase500_saveAllErrors_propagates500() {
        ProductMainDTO p1 = p("Laptop", "Tecnología", "1", "100", "COP");
        List<ProductMainDTO> products = List.of(p1);

        when(keyPolicy.buildKey(p1)).thenReturn("k1");
        when(repository.existsByKey("k1")).thenReturn(Mono.just(false));

        when(repository.saveAll(products)).thenReturn(Mono.error(new RuntimeException("write failed")));

        StepVerifier.create(useCase.execute(cmd(products)))
                .expectError(RuntimeException.class)
                .verify();

        verify(repository, times(1)).saveAll(products);
    }
}