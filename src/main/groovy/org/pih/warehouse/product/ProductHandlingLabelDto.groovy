package org.pih.warehouse.product

import org.pih.warehouse.core.localization.MessageLocalizer

/**
 * A handling label that can be associated with a product.
 */
class ProductHandlingLabelDto {

    /**
     * Identifies the label.
     */
    ProductHandlingLabel labelCode

    /**
     * The display text for the label.
     */
    String labelText

    /**
     * @return the handling labels that apply to the given product, with their display text localized.
     */
    static List<ProductHandlingLabelDto> listFrom(Product product, MessageLocalizer messageLocalizer) {
        return ProductHandlingLabel.of(product).collect {
            new ProductHandlingLabelDto(
                    labelCode: it,
                    labelText: messageLocalizer.localize(it.labelTextCode),
            )
        }
    }
}
