package co.com.bancolombia.products.usecase.product.addproducts;

import co.com.bancolombia.products.model.product.addproducts.gateway.ProductRepositoryGateway;
import co.com.bancolombia.products.model.shared.model.ProductMainDTO;
import co.com.bancolombia.products.model.shared.cqrs.Command;
import co.com.bancolombia.products.model.shared.cqrs.ContextData;
import co.com.bancolombia.products.model.shared.exception.BusinessException;
import co.com.bancolombia.products.model.shared.exception.ErrorCode;
import co.com.bancolombia.products.model.shared.policy.ProductKeyPolicy;
import co.com.bancolombia.products.model.product.addproducts.validation.ProductValidator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddProductsUseCase {

    private final ProductRepositoryGateway repository;
    private final ProductValidator validator;
    private final ProductKeyPolicy keyPolicy;

    public AddProductsUseCase(ProductRepositoryGateway repository, ProductValidator validator, ProductKeyPolicy keyPolicy) {
        this.repository = repository;
        this.validator = validator;
        this.keyPolicy = keyPolicy;
    }

    public Mono<Void> execute(Command<AddProductsPayload, ContextData> command) {
        return Mono.defer(() -> {
            ContextData ctx = command.context();
            AddProductsPayload payload = command.payload();

            if (payload == null || payload.products() == null || payload.products().isEmpty()) {
                return Mono.error(BusinessException.withContext(ErrorCode.ER400, ctx));
            }

            List<ProductMainDTO> products = payload.products();

            // Validar cada producto
            for (ProductMainDTO p : products) validator.validate(p, ctx);

            // Duplicados dentro del request 400
            Set<String> keys = new HashSet<>();
            for (ProductMainDTO p : products) {
                String key = keyPolicy.buildKey(p);
                if (!keys.add(key)) {
                    return Mono.error(BusinessException.withContext(ErrorCode.ER400, ctx));
                }
            }

            // Existencia en persistencia 409
            return Flux.fromIterable(keys)
                .flatMap(repository::existsByKey)
                .filter(Boolean::booleanValue)
                .hasElements()
                .flatMap(exists -> {
                    if (exists) return Mono.error(BusinessException.withContext(ErrorCode.ER409, ctx));
                    return repository.saveAll(products);
                });
        });
    }
}