package co.com.bancolombia.products.inmemory.product.infra;

import java.security.SecureRandom;

public class ProductIdGenerator {

    private static final String PREFIX = "PRO";
    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SUFFIX_LEN = 7;
    private final SecureRandom random = new SecureRandom();

    public String nextId() {
        StringBuilder sb = new StringBuilder(PREFIX);
        for (int i = 0; i < SUFFIX_LEN; i++) {
            sb.append(ALPHANUM.charAt(random.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }
}