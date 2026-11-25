package org.pih.warehouse.product

import org.pih.warehouse.core.dtos.DomainDto

/**
 * The full representation of a Product: its own fields, without the relationships to other entities.
 *
 * Use {@link ProductSimpleDto} when the product only needs to be displayed. To include only the id, map it with
 * {@link org.pih.warehouse.core.mapper.HydrationLevel#ID_ONLY}.
 */
class ProductDto implements DomainDto<Product> {

    String productCode
    String name
    String description
    Boolean active

    Boolean lotControl
    Boolean lotAndExpiryControl
    Boolean serialized

    Boolean coldChain
    Boolean controlledSubstance
    Boolean hazardousMaterial
    Boolean reconditioned
    Boolean essential

    String abcClass
    String unitOfMeasure
    String upc
    String ndc
    Integer packageSize
    String color

    List<ProductHandlingLabelDto> handlingLabels = []
}
