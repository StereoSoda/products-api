package co.com.bancolombia.products.model.shared.cqrs;

public record Command<P, C>(
        P payload,
        C context
) { }