package co.com.bancolombia.products.model.shared.model;

public record ProductMainDTO(
        String id,
        String name,
        String type,
        String quantity,
        String price,
        String currency
) {
    public ProductMainDTO withId(String newId) {
        return new ProductMainDTO(newId, name, type, quantity, price, currency);
    }
}
