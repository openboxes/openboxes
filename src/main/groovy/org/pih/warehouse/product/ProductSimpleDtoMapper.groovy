package org.pih.warehouse.product

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.pih.warehouse.core.localization.MessageLocalizer
import org.pih.warehouse.core.mapper.Mapper
import org.pih.warehouse.core.mapper.MapperConfig
import org.pih.warehouse.core.mapper.ResponseMapper

@Component
class ProductSimpleDtoMapper implements Mapper<Product, ProductSimpleDto>, ResponseMapper<ProductSimpleDto> {

    @Autowired
    MessageLocalizer messageLocalizer

    @Override
    ProductSimpleDto map(Product product, MapperConfig config) {
        return !product ? null : new ProductSimpleDto(
                id: product.id,
                productCode: product.productCode,
                name: product.name,
                handlingLabels: mapHandlingLabelsToDto(product),
        )
    }

    @Override
    Map<String, Object> asResponseBody(ProductSimpleDto source) {
        return [
                id            : source.id,
                productCode   : source.productCode,
                name          : source.name,
                handlingLabels: source.handlingLabels,
        ]
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
