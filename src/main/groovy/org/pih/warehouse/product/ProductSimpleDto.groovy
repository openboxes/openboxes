package org.pih.warehouse.product

import org.pih.warehouse.core.http.HasResponseMapper

/**
 * A simplified representation of a Product, containing only the fields that are required
 * to display the product in its most basic form.
 */
class ProductSimpleDto implements HasResponseMapper<ProductSimpleDtoMapper> {

    String id
    String productCode
    String name

    // You could argue that the handling labels aren't necessary to display a product in its most "basic" form,
    // but we're trying to standardize on *always* showing the labels, so we opt to include them here.
    List<ProductHandlingLabelDto> handlingLabels = []
}
