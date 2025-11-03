package org.pih.warehouse.product

import org.springframework.context.ApplicationEvent

/**
 * Event triggered when a product's barcode (UPC) is updated.
 */
class ProductBarcodeUpdatedEvent extends ApplicationEvent {

    final String oldUpc
    final String newUpc

    ProductBarcodeUpdatedEvent(Product product, String oldUpc, String newUpc) {
        super(product)
        this.oldUpc = oldUpc
        this.newUpc = newUpc
    }
}