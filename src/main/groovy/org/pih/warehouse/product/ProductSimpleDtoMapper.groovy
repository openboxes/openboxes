package org.pih.warehouse.product

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.pih.warehouse.core.localization.MessageLocalizer
import org.pih.warehouse.core.mapper.EntityToDtoMapper
import org.pih.warehouse.core.mapper.MapperConfig

@Component
class ProductSimpleDtoMapper implements EntityToDtoMapper<Product, ProductSimpleDto> {

    @Autowired
    MessageLocalizer messageLocalizer

    @Override
    ProductSimpleDto doMap(Product product, MapperConfig config) {
        return !product ? null : new ProductSimpleDto(
                id: product.id,
                productCode: product.productCode,
                name: product.name,
                handlingLabels: mapHandlingLabelsToDto(product),
        )
    }

    private List<ProductHandlingLabelDto> mapHandlingLabelsToDto(Product product) {
        List<ProductHandlingLabel> handlingLabels = ProductHandlingLabel.of(product)

        return handlingLabels.collect {
            new ProductHandlingLabelDto(
                    labelCode: it,
                    labelText: messageLocalizer.localize(it.labelTextCode),
            )
        }
    }
}
