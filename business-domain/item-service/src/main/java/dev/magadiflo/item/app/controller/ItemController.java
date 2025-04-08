package dev.magadiflo.item.app.controller;

import dev.magadiflo.item.app.model.dto.Item;
import dev.magadiflo.item.app.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping(path = "/{productId}")
    public ResponseEntity<Item> findProduct(@PathVariable Long productId, @RequestParam int quantity) {
        return ResponseEntity.ok(this.itemService.findItemByProductId(productId, quantity));
    }
}
