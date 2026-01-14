package co.com.bancolombia.products.api.shared.helpers;

import co.com.bancolombia.products.model.shared.cqrs.ContextData;
import co.com.bancolombia.products.model.shared.exception.BusinessException;
import co.com.bancolombia.products.model.shared.exception.ErrorCode;
import co.com.bancolombia.products.model.shared.exception.ValidationError;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.List;

@Order(-2)
public class GlobalExceptionHandler implements WebExceptionHandler {

    private final ErrorResponseFactory errorFactory;
    private final DateTimeProvider dateTimeProvider;
    private final RequestIdGenerator requestIdGenerator;

    public GlobalExceptionHandler(ErrorResponseFactory errorFactory,
                                  DateTimeProvider dateTimeProvider,
                                  RequestIdGenerator requestIdGenerator) {
        this.errorFactory = errorFactory;
        this.dateTimeProvider = dateTimeProvider;
        this.requestIdGenerator = requestIdGenerator;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) return Mono.error(ex);

        ContextData ctx = resolveContext(ex);
        ErrorCode code = resolveError(ex);
        List<ValidationError> details = resolveDetails(ex);

        exchange.getResponse().setStatusCode(HttpStatusCode.valueOf(code.httpStatus()));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().getHeaders().set(HeaderContextExtractor.X_REQUEST_ID, ctx.xRequestId());

        byte[] body = errorFactory.build(code, ctx, dateTimeProvider.nowFormatted(), details);
        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
    }

    private ErrorCode resolveError(Throwable ex) {
        if (ex instanceof BusinessException be) return be.errorCode();
        return ErrorCode.ER500;
    }

    private ContextData resolveContext(Throwable ex) {
        if (ex instanceof BusinessException be && be.contextData() != null) return be.contextData();
        // OJO: sin UUID.randomUUID()
        return new ContextData(requestIdGenerator.newId(), requestIdGenerator.newId());
    }

    private List<ValidationError> resolveDetails(Throwable ex) {
        if (ex instanceof BusinessException be) return be.details();
        return List.of();
    }
}