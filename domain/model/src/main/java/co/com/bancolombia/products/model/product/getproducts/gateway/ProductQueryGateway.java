package co.com.bancolombia.products.model.product.getproducts.gateway;

import co.com.bancolombia.products.model.shared.model.ProductMainDTO;
import reactor.core.publisher.Flux;

public interface ProductQueryGateway {
    Flux<ProductMainDTO> findAll();
    Flux<ProductMainDTO> findByFilter(String anyFilter, String anyValue);
}