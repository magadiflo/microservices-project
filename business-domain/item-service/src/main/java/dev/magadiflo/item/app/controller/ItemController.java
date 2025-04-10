package dev.magadiflo.item.app.controller;

import dev.magadiflo.item.app.model.dto.Item;
import dev.magadiflo.item.app.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(@Qualifier("itemServiceWithRestClientImpl") ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<List<Item>> findProducts() {
        return ResponseEntity.ok(this.itemService.findItems());
    }

    @GetMapping(path = "/filters-gateway")
    public ResponseEntity<Void> findProducts(@RequestParam(name = "color-name") String colorName,
                                             @RequestHeader(name = "X-Request-color") String headerColor) {
        log.info("AddRequestParameter (color-name): {}", colorName);
        log.info("AddRequestHeader (X-Request-color): {}", headerColor);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/{productId}")
    public ResponseEntity<Item> findProduct(@PathVariable Long productId, @RequestParam int quantity) {
        return ResponseEntity.ok(this.itemService.findItemByProductId(productId, quantity));
    }
}
