package co.com.bancolombia.products.config;

import co.com.bancolombia.products.model.product.addproducts.gateway.ProductRepositoryGateway;
import co.com.bancolombia.products.model.product.getproducts.gateway.ProductQueryGateway;
import co.com.bancolombia.products.model.shared.policy.ProductKeyPolicy;
import co.com.bancolombia.products.usecase.product.addproducts.AddProductsUseCase;
import co.com.bancolombia.products.usecase.product.addproducts.validation.ProductValidator;
import co.com.bancolombia.products.usecase.product.getproducts.GetProductsUseCase;
import co.com.bancolombia.products.usecase.product.getproducts.validation.GetProductsQueryValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCasesConfig {

        @Bean
        public ProductKeyPolicy productKeyPolicy() {
                return new ProductKeyPolicy();
        }

        @Bean
        public ProductValidator productValidator(ProductKeyPolicy keyPolicy) {
                return new ProductValidator(keyPolicy);
        }

        @Bean
        public AddProductsUseCase addProductsUseCase(
                ProductRepositoryGateway gateway,
                ProductValidator validator,
                ProductKeyPolicy keyPolicy
        ) {
                return new AddProductsUseCase(gateway, validator, keyPolicy);
        }

        @Bean
        public GetProductsQueryValidator getProductsQueryValidator(ProductKeyPolicy keyPolicy) {
                return new GetProductsQueryValidator(keyPolicy);
        }

        @Bean
        public GetProductsUseCase getProductsUseCase(
                ProductQueryGateway gateway,
                GetProductsQueryValidator validator
        ) {
                return new GetProductsUseCase(gateway, validator);
        }
}
