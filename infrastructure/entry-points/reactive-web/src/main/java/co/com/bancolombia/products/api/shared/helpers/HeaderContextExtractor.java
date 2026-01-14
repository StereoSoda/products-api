package co.com.bancolombia.products.api.shared.helpers;

import co.com.bancolombia.products.model.shared.cqrs.ContextData;
import co.com.bancolombia.products.model.shared.exception.BusinessException;
import co.com.bancolombia.products.model.shared.exception.ErrorCode;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.UUID;

public class HeaderContextExtractor {

    public static final String MESSAGE_ID = "message-id";
    public static final String X_REQUEST_ID = "x-request-id";

    private final RequestIdGenerator requestIdGenerator;

    public HeaderContextExtractor(RequestIdGenerator requestIdGenerator) {
        this.requestIdGenerator = requestIdGenerator;
    }

    public ContextData extractOrThrow(ServerRequest request) {
        String messageIdRaw = request.headers().firstHeader(MESSAGE_ID);
        String xRequestIdRaw = request.headers().firstHeader(X_REQUEST_ID);

        UUID messageId = tryParse(messageIdRaw);
        UUID xRequestId = tryParse(xRequestIdRaw);

        if (messageId == null || xRequestId == null) {
            ContextData generated = new ContextData(requestIdGenerator.newId(), requestIdGenerator.newId());
            throw BusinessException.withContext(ErrorCode.ER400, generated);
        }
        return new ContextData(messageId.toString(), xRequestId.toString());
    }

    private UUID tryParse(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try { return UUID.fromString(raw.trim()); }
        catch (IllegalArgumentException ex) { return null; }
    }
}