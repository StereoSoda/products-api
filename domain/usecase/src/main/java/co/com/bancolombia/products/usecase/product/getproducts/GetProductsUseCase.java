package co.com.bancolombia.products.usecase.product.getproducts;

import co.com.bancolombia.products.model.shared.model.ProductMainDTO;
import co.com.bancolombia.products.model.product.getproducts.gateway.ProductQueryGateway;
import co.com.bancolombia.products.model.shared.cqrs.Command;
import co.com.bancolombia.products.model.shared.cqrs.ContextData;
import co.com.bancolombia.products.model.shared.exception.BusinessException;
import co.com.bancolombia.products.model.shared.exception.ErrorCode;
import co.com.bancolombia.products.usecase.product.getproducts.validation.GetProductsQueryValidator;
import reactor.core.publisher.Mono;

import java.util.List;

public class GetProductsUseCase {

    private final ProductQueryGateway gateway;
    private final GetProductsQueryValidator validator;

    public GetProductsUseCase(ProductQueryGateway gateway, GetProductsQueryValidator validator) {
        this.gateway = gateway;
        this.validator = validator;
    }

    public Mono<List<ProductMainDTO>> execute(Command<GetProductsQuery, ContextData> command) {
        return Mono.defer(() -> {
            ContextData ctx = command.context();
            GetProductsQuery query = command.payload();

            String anyFilter = query == null ? null : query.anyFilter();
            String anyValue  = query == null ? null : query.anyValue();

            validator.validate(anyFilter, anyValue, ctx);

            var flux = (isBlank(anyFilter) || isBlank(anyValue))
                    ? gateway.findAll()
                    : gateway.findByFilter(anyFilter, anyValue);

            return flux.collectList()
                    .flatMap(list -> {
                        if (list == null || list.isEmpty()) {
                            return Mono.error(BusinessException.withContext(ErrorCode.ER409, ctx));
                        }
                        return Mono.just(list);
                    });
        });
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}