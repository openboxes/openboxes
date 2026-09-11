package org.pih.warehouse.product

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.pih.warehouse.core.localization.MessageLocalizer
import org.pih.warehouse.core.mapper.ResponseMapper

/**
 * Serializes a ProductSimpleDto
 */
@Component
class ProductSimpleDtoMapper implements ResponseMapper<ProductSimpleDto> {

    @Autowired
    MessageLocalizer messageLocalizer

    @Override
    Map<String, Object> asResponseBody(ProductSimpleDto source) {
        return [
                id            : source.id,
                productCode   : source.productCode,
                name          : source.name,
                handlingLabels: source.handlingLabels.collect { ProductHandlingLabel label ->
                    new ProductHandlingLabelDto(
                            labelCode: label,
                            labelText: messageLocalizer.localize(label.messageCode),
                    )
                },
        ]
    }
}
