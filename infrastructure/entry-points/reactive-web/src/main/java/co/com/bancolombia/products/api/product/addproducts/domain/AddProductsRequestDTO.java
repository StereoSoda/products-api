package co.com.bancolombia.products.api.product.addproducts.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "AddProductsRequest")
public record AddProductsRequestDTO(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        RequestData data
) {

    @Schema(name = "AddProductsRequestData")
    public record RequestData(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
            List<ProductDTO> products
    ) {}

    @Schema(name = "AddProductsRequestProduct")
    public record ProductDTO(
            @Schema(maxLength = 50, description = "Nombre del producto (sin caracteres especiales)")
            String name,

            @Schema(description = "Tipos permitidos: Tecnología, Moda y Alimento (string)")
            String type,

            @Schema(description = "Cantidad máxima: 1000 (string)")
            String quantity,

            @Schema(description = "Mayor a 0 y numérico (string)")
            String price,

            @Schema(description = "Debe aceptar COP (string)")
            String currency
    ) {}
}