package co.com.bancolombia.products.api.shared.helpers;

import co.com.bancolombia.products.api.shared.domain.ErrorResponseDTO;
import co.com.bancolombia.products.model.shared.cqrs.ContextData;
import co.com.bancolombia.products.model.shared.exception.ErrorCode;
import co.com.bancolombia.products.model.shared.exception.ValidationError;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class JacksonErrorResponseFactory extends ErrorResponseFactory {

    private final ObjectMapper mapper;
    private final boolean includeDetails;

    public JacksonErrorResponseFactory(ObjectMapper mapper, boolean includeDetails) {
        this.mapper = mapper;
        this.includeDetails = includeDetails;
    }

    @Override
    public byte[] build(ErrorCode code, ContextData ctx, String executionDate, List<ValidationError> details) {
        try {
            List<ErrorResponseDTO.Detail> mappedDetails = includeDetails
                    ? details.stream().map(d -> new ErrorResponseDTO.Detail(d.field(), d.reason())).toList()
                    : null;

            var body = new ErrorResponseDTO(
                    new ErrorResponseDTO.Meta(executionDate, ctx.messageId()),
                    new ErrorResponseDTO.Error(code.code(), code.message(), mappedDetails)
            );

            return mapper.writeValueAsBytes(body);
        } catch (Exception e) {
            // fallback mínimo para no romper el handler
            String fallback = """
          {"meta":{"executionDate":"%s","message-id":"%s"},"error":{"code":"ER500","message":"Hay un error interno en el sistema"}}
          """.formatted(executionDate, ctx.messageId());
            return fallback.getBytes();
        }
    }
}