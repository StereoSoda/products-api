package co.com.bancolombia.products.api.product.getproducts.application;

import co.com.bancolombia.products.api.shared.helpers.*;
import co.com.bancolombia.products.model.product.model.Product;
import co.com.bancolombia.products.model.shared.exception.ErrorCode;
import co.com.bancolombia.products.usecase.product.getproducts.GetProductsUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.handler.ExceptionHandlingWebHandler;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

class GetProductsHandlerTest {

    private static final String FIXED_DATE = "11/01/2026 12:45:59:092";
    private static final String FIXED_MESSAGE_ID = "11111111-1111-1111-1111-111111111111";
    private static final String FIXED_X_REQUEST_ID = "22222222-2222-2222-2222-222222222222";

    private static final String GEN_MESSAGE_ID = "33333333-3333-3333-3333-333333333333";
    private static final String GEN_X_REQUEST_ID = "44444444-4444-4444-4444-444444444444";

    @Test
    void testCase200_success_returns200AndBody() {
        // mocks
        GetProductsUseCase useCase = mock(GetProductsUseCase.class);

        List<Product> products = List.of(
                new Product(null, "Laptop", "Tecnología", "1", "100", "COP"),
                new Product(null, "Camisa", "Moda", "2", "50", "COP")
        );

        when(useCase.execute(any())).thenReturn(Mono.just(products));

        // deps reales/stubs
        RequestIdGenerator gen = fixedGenerator();
        HeaderContextExtractor extractor = new HeaderContextExtractor(gen);

        DateTimeProvider dt = fixedDateTime();
        GetProductsMapper mapper = new GetProductsMapper();

        GetProductsHandler handler = new GetProductsHandler(useCase, extractor, dt, mapper);

        var routes = RouterFunctions.route(GET("/getProducts"), handler::handle);

        WebTestClient client = clientFor(routes, globalExceptionHandler(gen, dt));

        client.get()
                .uri("/getProducts")
                .header(HeaderContextExtractor.MESSAGE_ID, FIXED_MESSAGE_ID)
                .header(HeaderContextExtractor.X_REQUEST_ID, FIXED_X_REQUEST_ID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HeaderContextExtractor.X_REQUEST_ID, FIXED_X_REQUEST_ID)
                .expectBody()
                .jsonPath("$.meta.executionDate").isEqualTo(FIXED_DATE)
                .jsonPath("$.meta.messageId").isEqualTo(FIXED_MESSAGE_ID) // si tu DTO usa messageId
                .jsonPath("$.data.products.length()").isEqualTo(2)
                .jsonPath("$.data.products[0].name").isEqualTo("Laptop");
    }

    @Test
    void testCase400_headersMissing_returns400AndGeneratedIds() {
        GetProductsUseCase useCase = mock(GetProductsUseCase.class);

        RequestIdGenerator gen = fixedGenerator();
        HeaderContextExtractor extractor = new HeaderContextExtractor(gen);
        DateTimeProvider dt = fixedDateTime();
        GetProductsMapper mapper = new GetProductsMapper();

        GetProductsHandler handler = new GetProductsHandler(useCase, extractor, dt, mapper);
        var routes = RouterFunctions.route(GET("/getProducts"), handler::handle);

        WebTestClient client = clientFor(routes, globalExceptionHandler(gen, dt));

        client.get()
                .uri("/getProducts")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().valueEquals(HeaderContextExtractor.X_REQUEST_ID, GEN_X_REQUEST_ID)
                .expectBody()
                .jsonPath("$.meta.executionDate").isEqualTo(FIXED_DATE)
                // el error handler arma el body; ajusta el path según tu factory:
                .jsonPath("$.error.code").isEqualTo(ErrorCode.ER400.code());
    }

    private WebTestClient clientFor(org.springframework.web.reactive.function.server.RouterFunction<ServerResponse> routes,
                                    WebExceptionHandler geh) {

        WebHandler webHandler = RouterFunctions.toWebHandler(routes);
        WebHandler decorated = new ExceptionHandlingWebHandler(webHandler, List.of(geh));
        return WebTestClient.bindToWebHandler(decorated).build();
    }

    private GlobalExceptionHandler globalExceptionHandler(RequestIdGenerator gen, DateTimeProvider dt) {
        ErrorResponseFactory factory = new JacksonErrorResponseFactory(new ObjectMapper(), true);
        return new GlobalExceptionHandler(factory, dt, gen);
    }

    private DateTimeProvider fixedDateTime() {
        return new DateTimeProvider() {
            @Override
            public String nowFormatted() {
                return FIXED_DATE;
            }
        };
    }

    /**
     * Generador determinístico:
     * 1) message-id generado
     * 2) x-request-id generado
     */
    private RequestIdGenerator fixedGenerator() {
        return new RequestIdGenerator() {
            private int i = 0;

            @Override
            public String newId() {
                return (i++ == 0) ? GEN_MESSAGE_ID : GEN_X_REQUEST_ID;
            }
        };
    }
}