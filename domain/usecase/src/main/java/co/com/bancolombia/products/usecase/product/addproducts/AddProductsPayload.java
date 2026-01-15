package co.com.bancolombia.products.usecase.product.addproducts;

import co.com.bancolombia.products.model.shared.model.ProductMainDTO;
import java.util.List;

public record AddProductsPayload(List<ProductMainDTO> products) { }
