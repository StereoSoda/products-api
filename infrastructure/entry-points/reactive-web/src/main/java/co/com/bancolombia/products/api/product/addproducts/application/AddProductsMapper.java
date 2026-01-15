package co.com.bancolombia.products.api.product.addproducts.application;

import co.com.bancolombia.products.api.product.addproducts.domain.AddProductsRequestDTO;
import co.com.bancolombia.products.api.product.addproducts.domain.AddProductsResponseDTO;
import co.com.bancolombia.products.model.shared.model.ProductMainDTO;
import co.com.bancolombia.products.model.shared.cqrs.Command;
import co.com.bancolombia.products.model.shared.cqrs.ContextData;
import co.com.bancolombia.products.model.shared.exception.BusinessException;
import co.com.bancolombia.products.model.shared.exception.ErrorCode;
import co.com.bancolombia.products.usecase.product.addproducts.AddProductsPayload;

import java.util.List;

public class AddProductsMapper {

    public Command<AddProductsPayload, ContextData> toCommand(AddProductsRequestDTO dto, ContextData ctx) {
        if (dto == null || dto.data() == null || dto.data().products() == null || dto.data().products().isEmpty()) {
            throw BusinessException.withContext(ErrorCode.ER400, ctx);
        }

        List<ProductMainDTO> products = dto.data().products().stream()
                .map(p -> new ProductMainDTO(
                        null,
                        p.name(),
                        p.type(),
                        p.quantity(),
                        p.price(),
                        p.currency()
                ))
                .toList();

        return new Command<>(new AddProductsPayload(products), ctx);
    }

    public AddProductsResponseDTO success(ContextData ctx, String creationDate) {
        return new AddProductsResponseDTO(
                new AddProductsResponseDTO.Meta(creationDate, ctx.messageId()),
                new AddProductsResponseDTO.ResponseData("Los productos fueron guardados exitosamente")
        );
    }
}