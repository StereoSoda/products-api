package co.com.bancolombia.products.api.product.getproducts.application;

import co.com.bancolombia.products.api.product.getproducts.domain.GetProductsResponseDTO;
import co.com.bancolombia.products.api.shared.helpers.DateTimeProvider;
import co.com.bancolombia.products.api.shared.helpers.HeaderContextExtractor;
import co.com.bancolombia.products.model.shared.cqrs.Command;
import co.com.bancolombia.products.model.shared.cqrs.ContextData;
import co.com.bancolombia.products.usecase.product.getproducts.GetProductsQuery;
import co.com.bancolombia.products.usecase.product.getproducts.GetProductsUseCase;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class GetProductsHandler {

    private final GetProductsUseCase useCase;
    private final HeaderContextExtractor headerExtractor;
    private final DateTimeProvider dateTimeProvider;
    private final GetProductsMapper mapper;

    public GetProductsHandler(GetProductsUseCase useCase,
                              HeaderContextExtractor headerExtractor,
                              DateTimeProvider dateTimeProvider,
                              GetProductsMapper mapper) {
        this.useCase = useCase;
        this.headerExtractor = headerExtractor;
        this.dateTimeProvider = dateTimeProvider;
        this.mapper = mapper;
    }

    public Mono<ServerResponse> handle(ServerRequest request) {
        ContextData ctx = headerExtractor.extractOrThrow(request);

        String anyFilter = request.queryParam("any_filter").orElse(null);
        String anyValue  = request.queryParam("any_value").orElse(null);

        var command = new Command<>(new GetProductsQuery(anyFilter, anyValue), ctx);

        return useCase.execute(command)
            .flatMap(products -> {
                var body = new GetProductsResponseDTO(
                    new GetProductsResponseDTO.Meta(dateTimeProvider.nowFormatted(), ctx.messageId()),
                    new GetProductsResponseDTO.Data(mapper.toDtoList(products))
                );

                return ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HeaderContextExtractor.X_REQUEST_ID, ctx.xRequestId())
                    .bodyValue(body);
            });
    }
}