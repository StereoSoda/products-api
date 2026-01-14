package co.com.bancolombia.products.model.shared.cqrs;

public record ContextData(
        String messageId,
        String xRequestId
) { }