package co.com.bancolombia.products.api.product.getproducts.application;

import co.com.bancolombia.products.api.product.getproducts.domain.GetProductsResponseDTO;
import co.com.bancolombia.products.model.product.model.Product;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetProductsMapper {

    public List<GetProductsResponseDTO.ProductDTO> toDtoList(List<Product> products) {
        return products.stream()
                .map(p -> new GetProductsResponseDTO.ProductDTO(
                        p.name(), p.type(), p.quantity(), p.price(), p.currency()
                ))
                .toList();
    }
}