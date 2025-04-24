package dev.magadiflo.item.app.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ItemConstant {
    public static final String NO_SUCH_ELEMENT_MESSAGE = "The item cannot be displayed because the product with id %d does not exist";
    public static final String NO_FOUND_ELEMENT_MESSAGE = "The product with id %d does not exist in product-service";
    public static final String NO_SUCH_LIST_ELEMENTS_MESSAGE = "Error listing products from the product-service";
    public static final String COMMUNICATION_MESSAGE = "An error occurred in the product-service: %s";
    public static final String ILEGAL_STATE_PRODUCT_MESSAGE = "The response from products is null";
}
