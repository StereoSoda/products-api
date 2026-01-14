package co.com.bancolombia.products.config;

import co.com.bancolombia.products.model.product.addproducts.gateway.ProductRepositoryGateway;
import co.com.bancolombia.products.model.product.getproducts.gateway.ProductQueryGateway;
import co.com.bancolombia.products.usecase.product.addproducts.AddProductsUseCase;
import co.com.bancolombia.products.usecase.product.getproducts.GetProductsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class UseCasesConfigTest {

    @Test
    void testUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(TestConfig.class)) {

            assertNotNull(context.getBean(AddProductsUseCase.class));
            assertNotNull(context.getBean(GetProductsUseCase.class));
        }
    }

    @Configuration
    @Import(UseCasesConfig.class)
    static class TestConfig {

        @Bean
        ProductRepositoryGateway productRepositoryGateway() {
            return mock(ProductRepositoryGateway.class);
        }

        @Bean
        ProductQueryGateway productQueryGateway() {
            return mock(ProductQueryGateway.class);
        }
    }
}