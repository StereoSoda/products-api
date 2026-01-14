package co.com.bancolombia.products.api.shared.helpers;

import co.com.bancolombia.products.model.shared.cqrs.ContextData;
import co.com.bancolombia.products.model.shared.exception.ErrorCode;
import co.com.bancolombia.products.model.shared.exception.ValidationError;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class ErrorResponseFactory {

    private final ObjectMapper mapper = new ObjectMapper();

    public byte[] build(ErrorCode error, ContextData ctx, String executionDate, List<ValidationError> details) {
        // Para respetar "message-id" exacto sin ensuciar DTOs con anotaciones:
        Map<String, Object> body = Map.of(
            "meta", Map.of(
                "executionDate", executionDate,
                "message-id", ctx.messageId()
            ),
            "error", Map.of(
                "code", error.code(),
                "message", error.message()
            )
        );

        try {
            return mapper.writeValueAsBytes(body);
        } catch (Exception ex) {
            // Último recurso
            return ("{\"meta\":{\"executionDate\":\"" + executionDate + "\",\"message-id\":\"" + ctx.messageId() +
                    "\"},\"error\":{\"code\":\"ER500\",\"message\":\"Hay un error interno en el sistema\"}}").getBytes();
        }
    }
}