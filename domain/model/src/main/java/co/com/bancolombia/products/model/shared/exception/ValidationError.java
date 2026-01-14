package co.com.bancolombia.products.model.shared.exception;

public record ValidationError(String field, String reason) { }