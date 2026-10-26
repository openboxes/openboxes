package org.pih.warehouse.allocation

import grails.gorm.transactions.Transactional
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment

@Transactional
class BackorderService {

    /**
     * Rejects the shipment when a backorder reference does not resolve to an order. An inbound item with
     * no demand left on its order is still received, and the putaway strategy sends it to storage.
     */
    void validateBackorderReferences(Shipment shipment) {
        for (String requisitionNumber : shipment.uniqueBackorderReferences) {
            if (!Requisition.findByRequestNumber(requisitionNumber)) {
                shipment.errors.reject(
                        "backorder.notFound.message",
                        [requisitionNumber] as Object[],
                        "Backorder ${requisitionNumber} not found")
            }
        }
    }
}
