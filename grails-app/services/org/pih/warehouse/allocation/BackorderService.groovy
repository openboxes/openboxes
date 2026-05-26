package org.pih.warehouse.allocation

import grails.gorm.transactions.Transactional
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

@Transactional
class BackorderService {

    // Special case: we only fulfill a backorder with an inbound item that exactly matches
    // the outbound demand. The general behaviour of backorder fulfillment is partial matching
    // (any inbound quantity reduces the demanded quantity, even if it only partially fulfills
    // the backorder), but our ASN contains a separate shipment item per backordered demand
    // line, so inbound and demanded quantities are expected to match one-to-one.
    // This hard-coded equality rule will eventually be replaced by a strategy pattern
    // (shared with AutomaticBackorderReallocationJob) - here the strategy is equality match.
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
            Collection<ShipmentItem> inboundItems = shipment.shipmentItems.findAll {
                it.backorderReference == requisitionNumber && !it.backorderItem
            }
            Set consumedRequisitionItems = [] as Set
            for (ShipmentItem inboundItem : inboundItems) {
                def matchingBackorderItem = backorder.requisitionItems.find { RequisitionItem demand ->
                    demand.product == inboundItem.product &&
                            demand.quantity == inboundItem.quantity &&
                            !demand.isAllocated() &&
                            !consumedRequisitionItems.contains(demand)
                }
                if (!matchingBackorderItem) {
                    shipment.errors.reject(
                            "backorder.unavailable.message",
                            [inboundItem.product?.productCode, requisitionNumber] as Object[],
                            "No unallocated lines for ${inboundItem.product?.productCode} on ${requisitionNumber}")
                    continue
                }
                consumedRequisitionItems.add(matchingBackorderItem)
            }
        }
    }
}
