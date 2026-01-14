package co.com.bancolombia.products.api.shared.helpers;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@FunctionalInterface
public interface RequestIdGenerator {
    String newId();

    static RequestIdGenerator defaultGenerator() {
        return () -> {
            var r = ThreadLocalRandom.current();
            return new UUID(r.nextLong(), r.nextLong()).toString();
        };
    }
}