package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.model.Store;
import io.nadia.ai.aimud.model.StorePayloads;
import io.nadia.ai.aimud.service.StoreService;
import io.nadia.ai.aimud.service.StoreTradeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreControllerTest {

    @Mock
    private StoreService storeService;

    @Mock
    private StoreTradeService storeTradeService;

    @Test
    void getAllStores_DelegatesToService() {
        StoreController controller = new StoreController(storeService, storeTradeService);
        Store store = new Store();
        store.setId(1L);
        when(storeService.getAllStores()).thenReturn(Flux.just(store));

        StepVerifier.create(controller.getAllStores())
                .expectNext(store)
                .verifyComplete();

        verify(storeService).getAllStores();
    }

    @Test
    void createStore_DelegatesToService() {
        StoreController controller = new StoreController(storeService, storeTradeService);
        Store store = new Store();
        store.setName("General");
        when(storeService.createStore(store)).thenReturn(Mono.just(store));

        StepVerifier.create(controller.createStore(store))
                .expectNext(store)
                .verifyComplete();

        verify(storeService).createStore(store);
    }

    @Test
    void buyItem_DelegatesToTradeService() {
        StoreController controller = new StoreController(storeService, storeTradeService);
        StorePayloads.StoreDialogPayload payload = new StorePayloads.StoreDialogPayload(1L, "Merchant", 50, 10, 100,
                List.of(), List.of());
        when(storeTradeService.buyItem(1L, 2L, 3L)).thenReturn(Mono.just(payload));

        StepVerifier.create(controller.buyItem(1L, 2L, 3L))
                .expectNext(payload)
                .verifyComplete();

        verify(storeTradeService).buyItem(1L, 2L, 3L);
    }
}

