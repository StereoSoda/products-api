package co.com.bancolombia.products.api.product.addproducts.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AddProductsResponse")
public record AddProductsResponseDTO(
        Meta meta,
        ResponseData data
) {

    @Schema(name = "AddProductsResponseMeta")
    public record Meta(
            String creationDate,
            @Schema(name = "message-id")
            String messageId
    ) {}

    @Schema(name = "AddProductsResponseData")
    public record ResponseData(
            String message
    ) {}
}