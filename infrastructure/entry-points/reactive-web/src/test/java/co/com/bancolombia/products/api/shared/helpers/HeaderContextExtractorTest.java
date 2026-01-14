package co.com.bancolombia.products.api.shared.helpers;

import co.com.bancolombia.products.model.shared.cqrs.ContextData;
import co.com.bancolombia.products.model.shared.exception.BusinessException;
import co.com.bancolombia.products.model.shared.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HeaderContextExtractorTest {

    // Stub inline: sin crear clases adicionales
    private final RequestIdGenerator requestIdGenerator = new RequestIdGenerator() {
        @Override
        public String newId() {
            return UUID.randomUUID().toString();
        }
    };

    private final HeaderContextExtractor extractor = new HeaderContextExtractor(requestIdGenerator);

    @Test
    void testCase200_headersValid() {
        String messageId = UUID.randomUUID().toString();
        String xRequestId = UUID.randomUUID().toString();

        ServerRequest req = MockServerRequest.builder()
                .header(HeaderContextExtractor.MESSAGE_ID, messageId)
                .header(HeaderContextExtractor.X_REQUEST_ID, xRequestId)
                .build();

        ContextData ctx = extractor.extractOrThrow(req);

        assertEquals(messageId, ctx.messageId());
        assertEquals(xRequestId, ctx.xRequestId());
    }

    @Test
    void testCase400_headersMissing_generatesNewInExceptionContext() {
        ServerRequest req = MockServerRequest.builder().build();

        BusinessException ex = assertThrows(BusinessException.class, () -> extractor.extractOrThrow(req));

        assertEquals(ErrorCode.ER400, ex.errorCode());
        assertNotNull(ex.contextData());

        assertDoesNotThrow(() -> UUID.fromString(ex.contextData().messageId()));
        assertDoesNotThrow(() -> UUID.fromString(ex.contextData().xRequestId()));
    }

    @Test
    void testCase400_headersInvalid_generatesNewInExceptionContext() {
        ServerRequest req = MockServerRequest.builder()
                .header(HeaderContextExtractor.MESSAGE_ID, "no-uuid")
                .header(HeaderContextExtractor.X_REQUEST_ID, "bad")
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> extractor.extractOrThrow(req));

        assertEquals(ErrorCode.ER400, ex.errorCode());
        assertNotNull(ex.contextData());

        assertDoesNotThrow(() -> UUID.fromString(ex.contextData().messageId()));
        assertDoesNotThrow(() -> UUID.fromString(ex.contextData().xRequestId()));
    }
}