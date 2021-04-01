/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.invoice

import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.ShipmentItem

class InvoiceItemService {

    InvoiceItem createFromCandidate(InvoiceCandidate candidate) {
        InvoiceItem invoiceItem = new InvoiceItem(
            budgetCode: candidate.budgetCode,
            product: Product.findByProductCode(candidate.productCode),
            glAccount: candidate.glAccount,
            quantity: candidate.quantity,
            quantityUom: candidate.quantityUom,
            quantityPerUom: candidate.quantityPerUom,
        )

        ShipmentItem shipmentItem = ShipmentItem.get(candidate.id)
        if (shipmentItem) {
            invoiceItem.addToShipmentItems(shipmentItem)
        } else {
            OrderAdjustment orderAdjustment = OrderAdjustment.get(candidate.id)
            if (orderAdjustment) {
                invoiceItem.addToOrderAdjustments(orderAdjustment)
            }
        }

        return invoiceItem
    }
}
