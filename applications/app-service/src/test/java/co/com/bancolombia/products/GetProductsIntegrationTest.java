package co.com.bancolombia.products;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@SpringBootTest(classes = MainApplication.class)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class GetProductsIntegrationTest {

    private static final String MESSAGE_ID = "message-id";
    private static final String X_REQUEST_ID = "x-request-id";

    @Autowired
    ApplicationContext context;

    WebTestClient client;

    @BeforeEach
    void setUp() {
        this.client = WebTestClient
            .bindToApplicationContext(context)
            .configureClient()
            .build();
    }

    @Test
    void testCase200_getProducts_noFilter_ok() {
        // Arrange: inserta datos primero
        seedProducts();

        String messageId = uuid();
        String xRequestId = uuid();

        // Act + Assert
        client.get()
            .uri("/getProducts")
            .header(MESSAGE_ID, messageId)
            .header(X_REQUEST_ID, xRequestId)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals(X_REQUEST_ID, xRequestId)
            .expectBody()
            .jsonPath("$.meta.messageId").isEqualTo(messageId)
            .jsonPath("$.data.products").isArray()
            .jsonPath("$.data.products.length()").isEqualTo(2);
    }

    @Test
    void testCase200_getProducts_withFilter_tipo_ok() {
        seedProducts();

        String messageId = uuid();
        String xRequestId = uuid();

        client.get()
            .uri(uriBuilder -> uriBuilder
                .path("/getProducts")
                .queryParam("any_filter", "tipo")
                .queryParam("any_value", "Tecnología")
                .build())
            .header(MESSAGE_ID, messageId)
            .header(X_REQUEST_ID, xRequestId)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals(X_REQUEST_ID, xRequestId)
            .expectBody()
            .jsonPath("$.meta.messageId").isEqualTo(messageId)
            .jsonPath("$.data.products").isArray()
            .jsonPath("$.data.products.length()").isEqualTo(1)
            .jsonPath("$.data.products[0].name").isEqualTo("Laptop");
    }

    @Test
    void testCase400_missingHeaders_returns400() {
        seedProducts();

        client.get()
            .uri("/getProducts")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest()
            .expectHeader().exists(X_REQUEST_ID)
            .expectBody()
            .jsonPath("$.error.code").isEqualTo("ER400");
    }

    @Test
    void testCase400_invalidQueryParams_returns400() {
        seedProducts();

        String messageId = uuid();
        String xRequestId = uuid();

        // any_filter inválido según tu validator (solo tipo/nombre/precio)
        client.get()
            .uri(uriBuilder -> uriBuilder
                .path("/getProducts")
                .queryParam("any_filter", "invalid_filter")
                .queryParam("any_value", "whatever")
                .build())
            .header(MESSAGE_ID, messageId)
            .header(X_REQUEST_ID, xRequestId)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest()
            .expectHeader().valueEquals(X_REQUEST_ID, xRequestId)
            .expectBody()
            .jsonPath("$.error.code").isEqualTo("ER400")
            .jsonPath("$.meta.message-id").isEqualTo(messageId);
    }

    @Test
    void testCase409_noProductsFound_returns409() {
        // NO seed => repositorio vacío

        String messageId = uuid();
        String xRequestId = uuid();

        client.get()
            .uri("/getProducts")
            .header(MESSAGE_ID, messageId)
            .header(X_REQUEST_ID, xRequestId)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(409)
            .expectHeader().valueEquals(X_REQUEST_ID, xRequestId)
            .expectBody()
            .jsonPath("$.error.code").isEqualTo("ER409")
            .jsonPath("$.meta.message-id").isEqualTo(messageId);
    }

    private void seedProducts() {
        String messageId = uuid();
        String xRequestId = uuid();

        client.post()
        .uri("/addProducts")
        .header(MESSAGE_ID, messageId)
        .header(X_REQUEST_ID, xRequestId)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("""
            {
              "data": {
                "products": [
                  { "name":"Laptop", "type":"Tecnología", "quantity":1, "price":100, "currency":"COP" },
                  { "name":"Camisa", "type":"Moda", "quantity":2, "price":50, "currency":"COP" }
                ]
              }
            }
        """)
        .exchange()
        .expectStatus().isCreated();
    }

    private static String uuid() {
        return UUID.randomUUID().toString();
    }
}