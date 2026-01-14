package co.com.bancolombia.products.model.product.model;

public record Product(
        String id,
        String name,
        String type,
        String quantity,
        String price,
        String currency
) {
    public Product withId(String newId) {
        return new Product(newId, name, type, quantity, price, currency);
    }
}
