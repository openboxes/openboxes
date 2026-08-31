package org.pih.warehouse.allocation

import grails.gorm.transactions.Transactional
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

@Transactional
class BackorderService {

    BackorderMatchingService backorderMatchingService

    void validateBackorderReferences(Shipment shipment) {
        for (String requisitionNumber : shipment.uniqueBackorderReferences) {
            Requisition backorder = Requisition.findByRequestNumber(requisitionNumber)
            if (!backorder) {
                shipment.errors.reject(
                        "backorder.notFound.message",
                        [requisitionNumber] as Object[],
                        "Backorder ${requisitionNumber} not found")
                continue
            }

            Collection<ShipmentItem> inboundItems = backorderMatchingService
                    .findInboundItems(shipment, requisitionNumber)
            List<BackorderMatch> matches = backorderMatchingService.match(backorder, inboundItems)

            for (ShipmentItem inboundItem : inboundItems) {
                if (!matches.any { it.inboundItem == inboundItem }) {
                    shipment.errors.reject(
                            "backorder.unavailable.message",
                            [inboundItem.product?.productCode, requisitionNumber] as Object[],
                            "No demand left for ${inboundItem.product?.productCode} on ${requisitionNumber}")
                }
            }
        }
    }
}
