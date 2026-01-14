package co.com.bancolombia.products.api.shared.helpers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeProvider {
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss:SSS");

    public String nowFormatted() {
        return LocalDateTime.now().format(FORMAT);
    }
}