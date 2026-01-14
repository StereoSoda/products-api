package co.com.bancolombia.products.api.shared.helpers;

import co.com.bancolombia.products.model.shared.cqrs.ContextData;
import co.com.bancolombia.products.model.shared.exception.BusinessException;
import co.com.bancolombia.products.model.shared.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.handler.ExceptionHandlingWebHandler;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

class GlobalExceptionHandlerTest {

    private static final String FIXED_DATE = "11/01/2026 12:45:59:092";

    // UUIDs fijos para aserciones (deben ser UUID válidos)
    private static final String FIXED_MESSAGE_ID = "11111111-1111-1111-1111-111111111111";
    private static final String FIXED_X_REQUEST_ID = "22222222-2222-2222-2222-222222222222";
    private static final String FIXED_GEN_1 = "33333333-3333-3333-3333-333333333333";
    private static final String FIXED_GEN_2 = "44444444-4444-4444-4444-444444444444";

    @Test
    void testCase400_headersMissing_generatesXRequestId() {

        RequestIdGenerator generator = new RequestIdGenerator() {
            private int i = 0;
            @Override public String newId() {
                return (i++ == 0) ? FIXED_MESSAGE_ID : FIXED_X_REQUEST_ID;
            }
        };

        HeaderContextExtractor extractor = new HeaderContextExtractor(generator);
        ErrorResponseFactory factory = new JacksonErrorResponseFactory(new ObjectMapper(), true);
        DateTimeProvider dt = fixedDateTimeProvider();
        GlobalExceptionHandler geh = new GlobalExceptionHandler(factory, dt, generator);

        RouterFunction<ServerResponse> routes = RouterFunctions.route(
                POST("/addProducts"),
                req -> {
                    // forzamos que falle por headers faltantes (debe lanzar BusinessException ER400)
                    extractor.extractOrThrow(req);
                    return ServerResponse.ok().build();
                }
        );

        WebTestClient client = clientFor(routes, geh);

        client.post()
                .uri("/addProducts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"data\":{\"products\":[]}}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().valueEquals(HeaderContextExtractor.X_REQUEST_ID, FIXED_X_REQUEST_ID)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.meta.message-id").isEqualTo(FIXED_MESSAGE_ID)
                .jsonPath("$.meta.executionDate").isEqualTo(FIXED_DATE)
                .jsonPath("$.error.code").isEqualTo("ER400");
    }

    @Test
    void testCase500_unknownException_generatesXRequestId() {
        // generator NO-bloqueante y determinista (evita UUID.randomUUID por BlockHound)
        RequestIdGenerator generator = new RequestIdGenerator() {
            private int i = 0;
            @Override public String newId() {
                return (i++ == 0) ? FIXED_GEN_1 : FIXED_GEN_2;
            }
        };

        HeaderContextExtractor extractor = new HeaderContextExtractor(generator);
        ErrorResponseFactory factory = new JacksonErrorResponseFactory(new ObjectMapper(), true);
        DateTimeProvider dt = fixedDateTimeProvider();
        GlobalExceptionHandler geh = new GlobalExceptionHandler(factory, dt, generator);

        RouterFunction<ServerResponse> routes = RouterFunctions.route(
                POST("/addProducts"),
                req -> {

                    ContextData ctx = extractor.extractOrThrow(req);
                    return Mono.error(BusinessException.withContext(ErrorCode.ER500, ctx, new RuntimeException("boom")));
                }
        );

        WebTestClient client = clientFor(routes, geh);

        client.post()
                .uri("/addProducts")
                .header(HeaderContextExtractor.MESSAGE_ID, FIXED_MESSAGE_ID)
                .header(HeaderContextExtractor.X_REQUEST_ID, FIXED_X_REQUEST_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"data\":{\"products\":[]}}")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().valueEquals(HeaderContextExtractor.X_REQUEST_ID, FIXED_X_REQUEST_ID)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                // como el ctx viene por headers, debe preservarse el message-id del request
                .jsonPath("$.meta.message-id").isEqualTo(FIXED_MESSAGE_ID)
                .jsonPath("$.meta.executionDate").isEqualTo(FIXED_DATE)
                .jsonPath("$.error.code").isEqualTo("ER500");
    }

    private WebTestClient clientFor(RouterFunction<ServerResponse> routes, WebExceptionHandler geh) {
        WebHandler webHandler = RouterFunctions.toWebHandler(routes);

        // Encadena el WebExceptionHandler (GlobalExceptionHandler) al pipeline
        WebHandler decorated = new ExceptionHandlingWebHandler(webHandler, List.of(geh));

        // En tu versión, esto es lo correcto (NO bindToHttpHandler)
        return WebTestClient.bindToWebHandler(decorated).configureClient().build();
    }

    private DateTimeProvider fixedDateTimeProvider() {
        return new DateTimeProvider() {
            @Override public String nowFormatted() {
                return FIXED_DATE;
            }
        };
    }
}