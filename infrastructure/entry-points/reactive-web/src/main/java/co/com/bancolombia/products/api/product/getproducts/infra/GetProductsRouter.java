package co.com.bancolombia.products.api.product.getproducts.infra;

import co.com.bancolombia.products.api.product.getproducts.application.GetProductsHandler;
import co.com.bancolombia.products.api.product.getproducts.domain.GetProductsResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.*;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
public class GetProductsRouter {

    @Bean
    @RouterOperations({
        @RouterOperation(
            path = "/getProducts",
            method = RequestMethod.GET,
            beanClass = GetProductsHandler.class,
            beanMethod = "handle",
            operation = @Operation(
                operationId = "getProducts",
                summary = "Listar productos",
                parameters = {
                    @Parameter(name = "message-id", in = ParameterIn.HEADER, required = true,
                            schema = @Schema(type = "string", format = "uuid")),
                    @Parameter(name = "x-request-id", in = ParameterIn.HEADER, required = true,
                            schema = @Schema(type = "string", format = "uuid")),
                    @Parameter(name = "any_filter", in = ParameterIn.QUERY, required = false,
                            schema = @Schema(type = "string", allowableValues = {"tipo","nombre","precio"})),
                    @Parameter(name = "any_value", in = ParameterIn.QUERY, required = false,
                            schema = @Schema(type = "string"))
                },
                responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                        headers = { @Header(name = "x-request-id", schema = @Schema(type="string", format="uuid")) },
                        content = @Content(schema = @Schema(implementation = GetProductsResponseDTO.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Query inválido",
                        headers = { @Header(name = "x-request-id", schema = @Schema(type="string", format="uuid")) }
                    ),
                    @ApiResponse(responseCode = "409", description = "No se encontraron productos",
                        headers = { @Header(name = "x-request-id", schema = @Schema(type="string", format="uuid")) }
                    ),
                    @ApiResponse(responseCode = "500", description = "Error interno",
                        headers = { @Header(name = "x-request-id", schema = @Schema(type="string", format="uuid")) }
                    )
                }
            )
        )
    })
    public RouterFunction<ServerResponse> routes(GetProductsHandler handler) {
        return RouterFunctions.route(GET("/getProducts"), handler::handle);
    }
}