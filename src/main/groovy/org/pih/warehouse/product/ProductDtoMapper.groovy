package org.pih.warehouse.product

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.pih.warehouse.core.localization.MessageLocalizer
import org.pih.warehouse.core.mapper.EntityToDtoMapper
import org.pih.warehouse.core.mapper.MapperConfig

@Component
class ProductDtoMapper implements EntityToDtoMapper<Product, ProductDto> {

    @Autowired
    MessageLocalizer messageLocalizer

    @Override
    ProductDto doMap(Product product, MapperConfig config) {
        return !product ? null : new ProductDto(
                id: product.id,
                productCode: product.productCode,
                name: product.name,
                description: product.description,
                active: product.active,
                lotControl: product.lotControl,
                lotAndExpiryControl: product.lotAndExpiryControl,
                serialized: product.serialized,
                coldChain: product.coldChain,
                controlledSubstance: product.controlledSubstance,
                hazardousMaterial: product.hazardousMaterial,
                reconditioned: product.reconditioned,
                essential: product.essential,
                abcClass: product.abcClass,
                unitOfMeasure: product.unitOfMeasure,
                upc: product.upc,
                ndc: product.ndc,
                packageSize: product.packageSize,
                color: product.color,
                handlingLabels: ProductHandlingLabelDto.listFrom(product, messageLocalizer),
        )
    }
}
