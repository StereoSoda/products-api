package co.com.bancolombia.products.inmemory.product.infra;

import co.com.bancolombia.products.model.shared.model.ProductMainDTO;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStore {

    private final Map<String, ProductMainDTO> store = new ConcurrentHashMap<>();

    public boolean exists(String key) {
        return store.containsKey(key);
    }

    public void put(String key, ProductMainDTO product) {
        store.put(key, product);
    }

    public Collection<ProductMainDTO> values() {
        return store.values();
    }
}