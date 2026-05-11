package org.pih.warehouse.allocation

import grails.gorm.transactions.Transactional
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentException
import org.pih.warehouse.shipping.ShipmentItem

@Transactional
class BackorderService {

    void validateBackorderReferences(Shipment shipment) {
        for (String reference : shipment.uniqueBackorderReferences) {
            Requisition backorder = Requisition.findByRequestNumber(reference)
            if (!backorder) {
                throw new ShipmentException(
                        shipment: shipment,
                        messageCode: "backorder.notFound.message",
                        messageArgs: [reference] as Object[]
                )
            }
            List<ShipmentItem> relatedItems = shipment.shipmentItems.findAll {
                it.backorderReference == reference && !it.backorderItem
            }
            for (ShipmentItem shipmentItem : relatedItems) {
                boolean hasAvailableBackorderLine = backorder.requisitionItems.any {
                    it.product == shipmentItem.product && it.quantity <= shipmentItem.quantity && !it.isAllocated()
                }
                if (!hasAvailableBackorderLine) {
                    throw new ShipmentException(
                            shipment: shipment,
                            messageCode: "backorder.unavailable.message",
                            messageArgs: [shipmentItem.product?.productCode, reference] as Object[]
                    )
                }
            }
        }
    }
}
