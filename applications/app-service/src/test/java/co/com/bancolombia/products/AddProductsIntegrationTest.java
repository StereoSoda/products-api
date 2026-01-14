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
class AddProductsIntegrationTest {

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
    void testCase201_addProducts_ok() {
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
        .expectStatus().isCreated()
        .expectHeader().valueEquals(X_REQUEST_ID, xRequestId);
    }

    @Test
    void testCase400_emptyProducts_returns400() {
        String messageId = uuid();
        String xRequestId = uuid();

        client.post()
        .uri("/addProducts")
        .header(MESSAGE_ID, messageId)
        .header(X_REQUEST_ID, xRequestId)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue("""
            { "data": { "products": [] } }
        """)
        .exchange()
        .expectStatus().isBadRequest()
        .expectHeader().valueEquals(X_REQUEST_ID, xRequestId)
        .expectBody()
        .jsonPath("$.error.code").isEqualTo("ER400")
        .jsonPath("$.meta.message-id").isEqualTo(messageId);
    }

    @Test
    void testCase400_duplicateInRequest_returns400() {
        String messageId = uuid();
        String xRequestId = uuid();

        // mismo business key => duplicado en request => 400
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
                  { "name":"Laptop", "type":"Tecnología", "quantity":1, "price":100, "currency":"COP" }
                ]
              }
            }
        """)
        .exchange()
        .expectStatus().isBadRequest()
        .expectHeader().valueEquals(X_REQUEST_ID, xRequestId)
        .expectBody()
        .jsonPath("$.error.code").isEqualTo("ER400")
        .jsonPath("$.meta.message-id").isEqualTo(messageId);
    }

    @Test
    void testCase409_alreadyExists_returns409() {
        String messageId = uuid();
        String xRequestId = uuid();

        String body = """
            {
              "data": {
                "products": [
                  { "name":"Laptop", "type":"Tecnología", "quantity":1, "price":100, "currency":"COP" }
                ]
              }
            }
        """;

        // 1) Inserta OK
        client.post()
                .uri("/addProducts")
                .header(MESSAGE_ID, messageId)
                .header(X_REQUEST_ID, xRequestId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isCreated();

        // 2) Reintenta => 409
        client.post()
                .uri("/addProducts")
                .header(MESSAGE_ID, messageId)
                .header(X_REQUEST_ID, xRequestId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectHeader().valueEquals(X_REQUEST_ID, xRequestId)
                .expectBody()
                .jsonPath("$.error.code").isEqualTo("ER409")
                .jsonPath("$.meta.message-id").isEqualTo(messageId);
    }

//    @Test
//    void testCase500_unknownError_returns500() {
//        String body = """
//            {
//              "data": {
//                "products": [
//                  { "name":"", "type":"Tecnología", "quantity":1, "price":100, "currency":"COP" }
//                ]
//              }
//            }
//        """;
//
//        client.post()
//                .uri("/addProducts" + "?forceError=true")
//                .header("message-id", uuid())
//                .header("x-request-id", uuid())
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(body)
//                .exchange()
//                .expectStatus().is5xxServerError()
//                .expectHeader().exists("x-request-id")
//                .expectBody()
//                .jsonPath("$.error.code").isEqualTo("ER500");
//    }

    private static String uuid() {
        return UUID.randomUUID().toString();
    }
}