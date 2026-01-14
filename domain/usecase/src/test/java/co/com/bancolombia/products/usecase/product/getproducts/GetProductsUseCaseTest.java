package co.com.bancolombia.products.usecase.product.getproducts;

import co.com.bancolombia.products.model.product.model.Product;
import co.com.bancolombia.products.model.product.getproducts.gateway.ProductQueryGateway;
import co.com.bancolombia.products.model.shared.cqrs.Command;
import co.com.bancolombia.products.model.shared.cqrs.ContextData;
import co.com.bancolombia.products.usecase.product.getproducts.validation.GetProductsQueryValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetProductsUseCaseTest {

    @Mock
    private ProductQueryGateway gateway;

    @Mock
    private GetProductsQueryValidator validator;

    @InjectMocks
    private GetProductsUseCase useCase;

    private final ContextData ctx = new ContextData(
            "11111111-1111-1111-1111-111111111111",
            "22222222-2222-2222-2222-222222222222"
    );

    private Product p1() {
        return new Product(null, "Laptop", "Tecnología", "1", "100", "COP");
    }

    private Product p2() {
        return new Product(null, "Camisa", "Moda", "2", "50", "COP");
    }

    @Test
    void testCase200_noFilter_callsFindAll() {
        when(gateway.findAll()).thenReturn(Flux.just(p1(), p2()));

        Command<GetProductsQuery, ContextData> cmd = new Command<>(new GetProductsQuery(null, null), ctx);

        StepVerifier.create(useCase.execute(cmd))
                .expectNextMatches(list -> list.size() == 2 && "Laptop".equals(list.get(0).name()))
                .verifyComplete();

        verify(validator).validate(null, null, ctx);
        verify(gateway).findAll();
        verify(gateway, never()).findByFilter(anyString(), anyString());
    }

    @Test
    void testCase200_withFilter_callsFindByFilter() {
        when(gateway.findByFilter("tipo", "Tecnología")).thenReturn(Flux.just(p1()));

        Command<GetProductsQuery, ContextData> cmd = new Command<>(new GetProductsQuery("tipo", "Tecnología"), ctx);

        StepVerifier.create(useCase.execute(cmd))
                .expectNextMatches(list -> list.size() == 1 && "Laptop".equals(list.get(0).name()))
                .verifyComplete();

        verify(validator).validate("tipo", "Tecnología", ctx);
        verify(gateway, never()).findAll();
        verify(gateway).findByFilter("tipo", "Tecnología");
    }

    @Test
    void testCase200_blankParams_treatedAsNoFilter_callsFindAll() {
        when(gateway.findAll()).thenReturn(Flux.just(p1()));

        Command<GetProductsQuery, ContextData> cmd = new Command<>(new GetProductsQuery("   ", "   "), ctx);

        StepVerifier.create(useCase.execute(cmd))
                .expectNextMatches(list -> list.size() == 1)
                .verifyComplete();

        verify(validator).validate("   ", "   ", ctx);
        verify(gateway).findAll();
        verify(gateway, never()).findByFilter(anyString(), anyString());
    }
}