package co.com.bancolombia.products.api.shared.helpers;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class FastRequestIdGenerator implements RequestIdGenerator {

    @Override
    public String newId() {
        long msb = ThreadLocalRandom.current().nextLong();
        long lsb = ThreadLocalRandom.current().nextLong();
        return new UUID(msb, lsb).toString();
    }
}