package co.com.bancolombia.products.usecase.product.addproducts;

import co.com.bancolombia.products.model.product.model.Product;
import java.util.List;

public record AddProductsPayload(List<Product> products) { }
