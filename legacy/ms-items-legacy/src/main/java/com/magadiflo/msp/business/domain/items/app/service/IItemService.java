package com.magadiflo.msp.business.domain.items.app.service;

import com.magadiflo.msp.business.domain.items.app.models.Item;

import java.util.List;

public interface IItemService {
    List<Item> findAll();

    Item findByProductId(Long productId, Integer cantidad);
}
