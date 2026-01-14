package co.com.bancolombia.products.api.product.getproducts.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "GetProductsResponse")
public record GetProductsResponseDTO(
        Meta meta,
        Data data
) {
    public record Meta(
            String executionDate,
            @Schema(name = "message-id")
            String messageId
    ) {}

    public record Data(
            List<ProductDTO> products
    ) {}

    public record ProductDTO(
            String name,
            String type,
            String quantity,
            String price,
            String currency
    ) {}
}