package co.com.bancolombia.products.inmemory.product.adapter;

import co.com.bancolombia.products.inmemory.product.infra.InMemoryStore;
import co.com.bancolombia.products.inmemory.product.infra.ProductIdGenerator;
import co.com.bancolombia.products.model.product.addproducts.gateway.ProductRepositoryGateway;
import co.com.bancolombia.products.model.shared.model.ProductMainDTO;
import co.com.bancolombia.products.model.product.getproducts.gateway.ProductQueryGateway;
import co.com.bancolombia.products.model.shared.policy.ProductKeyPolicy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class InMemoryProductRepositoryAdapter implements ProductRepositoryGateway, ProductQueryGateway {

    private final InMemoryStore store;
    private final ProductIdGenerator idGenerator;
    private final ProductKeyPolicy keyPolicy;

    public InMemoryProductRepositoryAdapter(InMemoryStore store, ProductIdGenerator idGenerator, ProductKeyPolicy keyPolicy) {
        this.store = store;
        this.idGenerator = idGenerator;
        this.keyPolicy = keyPolicy;
    }

    @Override
    public Mono<Boolean> existsByKey(String key) {
        return Mono.fromSupplier(() -> store.exists(key));
    }

    @Override
    public Mono<Void> saveAll(List<ProductMainDTO> products) {
        return Mono.fromRunnable(() -> {
            for (ProductMainDTO p : products) {
                String key = keyPolicy.buildKey(p);
                String id = idGenerator.nextId();
                store.put(key, p.withId(id));
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .then();
    }

    @Override
    public Flux<ProductMainDTO> findAll() {
        return Flux.defer(() -> Flux.fromIterable(store.values()));
    }

    @Override
    public Flux<ProductMainDTO> findByFilter(String anyFilter, String anyValue) {
        return findAll().filter(p -> matches(p, anyFilter, anyValue));
    }

    private boolean matches(ProductMainDTO p, String anyFilter, String anyValue) {
        if (anyFilter == null || anyFilter.isBlank() || anyValue == null || anyValue.isBlank()) return true;

        String f = keyPolicy.normalize(anyFilter);
        String v = keyPolicy.normalize(anyValue);

        return switch (f) {
            case "tipo"   -> keyPolicy.normalize(p.type()).contains(v);
            case "nombre" -> keyPolicy.normalize(p.name()).contains(v);
            case "precio" -> safe(p.price()).equals(anyValue.trim());
            default -> false;
        };
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}