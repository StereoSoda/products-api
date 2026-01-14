package co.com.bancolombia.products.api.config;

import co.com.bancolombia.products.api.product.addproducts.application.AddProductsHandler;
import co.com.bancolombia.products.api.product.addproducts.application.AddProductsMapper;
import co.com.bancolombia.products.api.shared.helpers.*;
import co.com.bancolombia.products.usecase.product.addproducts.AddProductsUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReactiveWebConfig {

    @Bean
    public DateTimeProvider dateTimeProvider() {
        return new DateTimeProvider();
    }

    @Bean
    public RequestIdGenerator requestIdGenerator() {
        return RequestIdGenerator.defaultGenerator();
    }

    @Bean
    public HeaderContextExtractor headerContextExtractor(RequestIdGenerator gen) {
        return new HeaderContextExtractor(gen);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder().findAndAddModules().build();
    }

    @Bean
    public ErrorResponseFactory errorResponseFactory(ObjectMapper mapper,
                                                     @Value("${api.errors.include-details:false}") boolean includeDetails) {
        return new JacksonErrorResponseFactory(mapper, includeDetails);
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler(
            ErrorResponseFactory errorResponseFactory,
            DateTimeProvider dateTimeProvider,
            RequestIdGenerator gen
    ) {
        return new GlobalExceptionHandler(errorResponseFactory, dateTimeProvider, gen);
    }

    @Bean
    public AddProductsMapper addProductsMapper() {
        return new AddProductsMapper();
    }

    @Bean
    public AddProductsHandler addProductsHandler(
            HeaderContextExtractor extractor,
            AddProductsMapper mapper,
            AddProductsUseCase useCase,
            DateTimeProvider dt
    ) {
        return new AddProductsHandler(extractor, mapper, useCase, dt);
    }
}