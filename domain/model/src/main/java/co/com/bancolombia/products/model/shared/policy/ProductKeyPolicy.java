package co.com.bancolombia.products.model.shared.policy;

import co.com.bancolombia.products.model.shared.model.ProductMainDTO;

import java.text.Normalizer;

public class ProductKeyPolicy {

    public String buildKey(ProductMainDTO p) {
        // Key estable para existencia/duplicados
        String name = normalize(p.name());
        String type = normalize(p.type());
        String currency = normalize(p.currency()).toUpperCase();
        return name + "|" + type + "|" + currency;
    }

    public String normalize(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim().replaceAll("\\s+", " ");
        String noDiacritics = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noDiacritics.toLowerCase();
    }
}