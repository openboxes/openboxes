package org.pih.warehouse.product
import org.pih.warehouse.core.User

import org.springframework.context.ApplicationEvent

/**
 * Event triggered when a product's barcode (UPC) is updated.
 */
class ProductBarcodeUpdatedEvent extends ApplicationEvent {

    final User user
    final String oldUpc
    final String newUpc

    ProductBarcodeUpdatedEvent(User user, Product product, String oldUpc, String newUpc) {
        super(product)
        this.user = user
        this.oldUpc = oldUpc
        this.newUpc = newUpc
    }
}
