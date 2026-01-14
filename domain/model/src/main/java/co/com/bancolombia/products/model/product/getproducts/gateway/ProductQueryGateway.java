package co.com.bancolombia.products.model.product.getproducts.gateway;

import co.com.bancolombia.products.model.product.model.Product;
import reactor.core.publisher.Flux;

public interface ProductQueryGateway {
    Flux<Product> findAll();
    Flux<Product> findByFilter(String anyFilter, String anyValue);
}