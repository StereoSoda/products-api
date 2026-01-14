package co.com.bancolombia.products.api.product.addproducts.infra;

import co.com.bancolombia.products.api.product.addproducts.application.AddProductsHandler;
import co.com.bancolombia.products.api.product.addproducts.domain.AddProductsRequestDTO;
import co.com.bancolombia.products.api.product.addproducts.domain.AddProductsResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.*;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;


@Configuration
public class AddProductsRouter {

    @Bean
    @RouterOperations({
        @RouterOperation(
            path = "/addProducts",
            method = RequestMethod.POST,
            beanClass = AddProductsHandler.class,
            beanMethod = "handle",
            operation = @Operation(
                operationId = "addProducts",
                summary = "Agregar productos",
                parameters = {
                    @Parameter(
                        name = "message-id",
                        in = ParameterIn.HEADER,
                        required = true,
                        schema = @Schema(type = "string", format = "uuid")
                    ),
                    @Parameter(
                        name = "x-request-id",
                        in = ParameterIn.HEADER,
                        required = true,
                        schema = @Schema(type = "string", format = "uuid")
                    )
                },
                requestBody = @RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = AddProductsRequestDTO.class))
                ),
                responses = {
                    @ApiResponse(
                        responseCode = "201",
                        description = "Creado",
                        headers = {
                            @Header(name = "x-request-id", schema = @Schema(type = "string", format = "uuid"))
                        },
                        content = @Content(schema = @Schema(implementation = AddProductsResponseDTO.class))
                    ),
                    @ApiResponse(
                        responseCode = "400",
                        description = "Datos inválidos",
                        headers = {
                            @Header(name = "x-request-id", schema = @Schema(type = "string", format = "uuid"))
                        }
                    ),
                    @ApiResponse(
                        responseCode = "409",
                        description = "Producto ya existe",
                        headers = {
                            @Header(name = "x-request-id", schema = @Schema(type = "string", format = "uuid"))
                        }
                    ),
                    @ApiResponse(
                        responseCode = "500",
                        description = "Error interno",
                        headers = {
                            @Header(name = "x-request-id", schema = @Schema(type = "string", format = "uuid"))
                        }
                    )
                }
            )
        )
    })
    public RouterFunction<ServerResponse> addProductsRoutes(AddProductsHandler handler) {
        return RouterFunctions.route(POST("/addProducts"), handler::handle);
    }
}