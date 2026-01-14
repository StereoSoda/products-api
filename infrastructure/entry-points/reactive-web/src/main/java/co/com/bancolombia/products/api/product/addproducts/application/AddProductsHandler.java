package co.com.bancolombia.products.api.product.addproducts.application;

import co.com.bancolombia.products.api.product.addproducts.domain.AddProductsRequestDTO;
import co.com.bancolombia.products.api.shared.helpers.DateTimeProvider;
import co.com.bancolombia.products.api.shared.helpers.HeaderContextExtractor;
import co.com.bancolombia.products.model.shared.cqrs.ContextData;
import co.com.bancolombia.products.model.shared.exception.BusinessException;
import co.com.bancolombia.products.model.shared.exception.ErrorCode;
import co.com.bancolombia.products.usecase.product.addproducts.AddProductsUseCase;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class AddProductsHandler {

    private final HeaderContextExtractor headerExtractor;
    private final AddProductsMapper mapper;
    private final AddProductsUseCase useCase;
    private final DateTimeProvider dateTimeProvider;

    public AddProductsHandler(
            HeaderContextExtractor headerExtractor,
            AddProductsMapper mapper,
            AddProductsUseCase useCase,
            DateTimeProvider dateTimeProvider
    ) {
        this.headerExtractor = headerExtractor;
        this.mapper = mapper;
        this.useCase = useCase;
        this.dateTimeProvider = dateTimeProvider;
    }

    public Mono<ServerResponse> handle(ServerRequest request) {
        ContextData ctx = headerExtractor.extractOrThrow(request);

        return request.bodyToMono(AddProductsRequestDTO.class)
        .switchIfEmpty(
            Mono.error(BusinessException.withContext(
                    ErrorCode.ER400, ctx
                )
            )
        )
        .map(dto -> mapper.toCommand(dto, ctx))
        .flatMap(useCase::execute)
        .then(Mono.defer(() ->
            ServerResponse.status(201)
            .contentType(MediaType.APPLICATION_JSON)
            .header(HeaderContextExtractor.X_REQUEST_ID, ctx.xRequestId())
            .bodyValue(mapper.success(ctx, dateTimeProvider.nowFormatted()))
        ));
    }
}