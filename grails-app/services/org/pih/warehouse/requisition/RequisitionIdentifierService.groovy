package org.pih.warehouse.requisition

import grails.gorm.transactions.Transactional

import org.pih.warehouse.core.IdentifierService
import org.pih.warehouse.core.identification.BlankIdentifierResolver
import org.pih.warehouse.shipping.Shipment

@Transactional
class RequisitionIdentifierService extends IdentifierService<Requisition> implements BlankIdentifierResolver<Requisition> {

    @Override
    String getIdentifierName() {
        return "requisition"
    }

    @Override
    protected Integer countByIdentifier(String id) {
        // We use requisition.requestNumber as shipment.shipmentNumber when performing stock movements so we need
        // to check that the id is unique for shipments as well. See StockMovementService.createShipment for details.
        Integer count = Requisition.countByRequestNumber(id)

        // Only bother checking shipment if requisition doesn't already have a duplicate.
        return count > 0 ? count : Shipment.countByShipmentNumber(id)
    }

    @Override
    List<Requisition> getAllUnassignedEntities() {
        return Requisition.findAll("from Requisition as r where (requestNumber is null or requestNumber = '') and (isTemplate is null or isTemplate = false)")
    }

    @Override
    void setIdentifierOnEntity(String id, Requisition entity) {
        entity.requestNumber = id
    }
}
