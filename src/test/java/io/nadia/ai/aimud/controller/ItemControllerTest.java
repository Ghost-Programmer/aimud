package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.service.ItemService;
import io.nadia.ai.aimud.types.ItemType;
import io.nadia.ai.aimud.types.WearLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @Test
    void getItem_NotFoundMapsTo404() {
        ItemController controller = new ItemController(itemService);
        when(itemService.getItem(1L)).thenReturn(Mono.empty());

        StepVerifier.create(controller.getItem(1L))
                .assertNext(resp -> assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND))
                .verifyComplete();
    }

    @Test
    void createItem_DelegatesToService() {
        ItemController controller = new ItemController(itemService);
        Item item = new Item(ItemType.WEAPON, WearLocation.PRIMARY, "Sword", "Desc");
        when(itemService.saveItem(item)).thenReturn(Mono.just(item));

        StepVerifier.create(controller.createItem(item))
                .assertNext(resp -> {
                    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(resp.getBody()).isEqualTo(item);
                })
                .verifyComplete();

        verify(itemService).saveItem(item);
    }

    @Test
    void getItems_BuildsPagedResponse() {
        ItemController controller = new ItemController(itemService);
        Item item = new Item(ItemType.WEAPON, WearLocation.PRIMARY, "Sword", "Desc");
        item.setValue(100);
        when(itemService.getAllItems()).thenReturn(Flux.just(item));

        StepVerifier.create(controller.getItems(0, 10, null, null, null, null, null))
                .assertNext(map -> {
                    assertThat(map).containsKeys("items", "total", "page", "size");
                    assertThat(map.get("total")).isEqualTo(1);
                })
                .verifyComplete();
    }
}

