package dev.magadiflo.item.app.service;

import dev.magadiflo.item.app.model.dto.Item;

import java.util.List;

public interface ItemService {
    List<Item> findItems();

    Item findItemByProductId(Long productId, int quantity);
}
