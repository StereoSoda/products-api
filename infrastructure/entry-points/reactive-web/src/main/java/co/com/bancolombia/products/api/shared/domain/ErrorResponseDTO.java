package co.com.bancolombia.products.api.shared.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ErrorResponseDTO(
        Meta meta,
        Error error
) {
    public record Meta(String executionDate, @JsonProperty("message-id")  String messageId) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Error(String code, String message, List<Detail> details) {}

    public record Detail(String field, String reason) {}
}