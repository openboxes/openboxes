package org.pih.warehouse.order

import grails.databinding.BindUsing
import grails.validation.Validateable
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.product.Product

class CreatePurchaseOrderItemCommand implements Validateable {

    @BindUsing({ obj, source ->
        def productCode = source['product']['code']
        def productName = source['product']['name']
        return Product.findByProductCodeOrName(productCode, productName)
    })
    Product product

    Integer quantity

    BigDecimal unitPrice

    @BindUsing({ obj, source ->
        def quantityUom = source['quantityUom']
        return UnitOfMeasure.findByName(quantityUom) ?: UnitOfMeasure.findByCode(quantityUom)
    })
    UnitOfMeasure quantityUom

    void applyDefaults() {
        if (!quantityUom) {
            quantityUom = UnitOfMeasure.findByName("Each")
        }
    }

    static constraints = {
        product(nullable: false)
        quantity(nullable: false)
        unitPrice(nullable: true)
        quantityUom(nullable: true)
    }
}
