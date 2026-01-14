package co.com.bancolombia.products.config;

import co.com.bancolombia.products.inmemory.product.adapter.InMemoryProductRepositoryAdapter;
import co.com.bancolombia.products.inmemory.product.infra.InMemoryStore;
import co.com.bancolombia.products.inmemory.product.infra.ProductIdGenerator;
import co.com.bancolombia.products.model.shared.policy.ProductKeyPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdapterConfig {

    @Bean
    public InMemoryStore inMemoryStore() {
        return new InMemoryStore();
    }

    @Bean
    public ProductIdGenerator productIdGenerator() {
        return new ProductIdGenerator();
    }

    @Bean
    public InMemoryProductRepositoryAdapter inMemoryProductRepositoryAdapter(
            InMemoryStore store,
            ProductIdGenerator generator,
            ProductKeyPolicy keyPolicy
    ) {
        return new InMemoryProductRepositoryAdapter(store, generator, keyPolicy);
    }
}