package org.pih.warehouse.requisition

import grails.gorm.transactions.Transactional

import org.pih.warehouse.core.IdentifierService
import org.pih.warehouse.core.Location
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
        Integer count = Requisition.countByRequestNumber(id)
        if (count > 0) {
            return count
        }

        // We use requisition.requestNumber as shipment.shipmentNumber when performing stock movements so we need
        // to check that the id is unique for shipments as well. See StockMovementService.createShipment for details.
        count = Shipment.countByShipmentNumber(id)
        if (count > 0) {
            return count
        }

        // We also use shipmentNumber as locationNumber when creating internal receiving locations so we need to verify
        // that the id is unique for locations as well. See LocationService.findOrCreateInternalLocation for details.
        return Location.countByLocationNumber(id)
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
