package org.pih.warehouse.allocation

import grails.gorm.transactions.Transactional
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentException
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
    List<ShipmentException> validateBackorderReferences(Shipment shipment) {
        List<ShipmentException> errors = []
        for (String requisitionNumber : shipment.uniqueBackorderReferences) {
            Requisition backorder = Requisition.findByRequestNumber(requisitionNumber)
            if (!backorder) {
                errors << new ShipmentException(
                        shipment: shipment,
                        messageCode: "backorder.notFound.message",
                        messageArgs: [requisitionNumber] as Object[]
                )
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
                    errors << new ShipmentException(
                            shipment: shipment,
                            messageCode: "backorder.unavailable.message",
                            messageArgs: [inboundItem.product?.productCode, requisitionNumber] as Object[]
                    )
                    continue
                }
                consumedRequisitionItems.add(matchingBackorderItem)
            }
        }
        return errors
    }
}
