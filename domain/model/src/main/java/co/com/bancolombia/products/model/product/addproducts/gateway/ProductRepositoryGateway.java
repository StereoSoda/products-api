package co.com.bancolombia.products.model.product.addproducts.gateway;

import co.com.bancolombia.products.model.product.model.Product;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ProductRepositoryGateway {
    Mono<Boolean> existsByKey(String key);
    Mono<Void> saveAll(List<Product> products);
}