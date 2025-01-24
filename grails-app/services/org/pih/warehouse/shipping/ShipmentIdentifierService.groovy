package org.pih.warehouse.shipping

import grails.gorm.transactions.Transactional

import org.pih.warehouse.core.IdentifierService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.identification.BlankIdentifierResolver

@Transactional
class ShipmentIdentifierService extends IdentifierService<Shipment> implements BlankIdentifierResolver<Shipment> {

    @Override
    String getIdentifierName() {
        return "shipment"
    }

    @Override
    protected Integer countByIdentifier(String id) {
        Integer count = Shipment.countByShipmentNumber(id)

        // TODO: Refactor how ids are generated for locations so that we don't need to do this check. Uniqueness
        //       of ids on one service shouldn't depend on another. The easiest solution is to override the format
        //       for generating location ids to be "${custom.shipmentNumber}${random}" so that it's like the
        //       shipment number but still unique.

        // We use shipment.shipmentNumber as location.locationNumber when creating internal locations from shipments
        // (see LocationService.findOrCreateInternalLocation for details) so we need to check that the id is unique
        // for locations as well. Only bother checking location if shipment doesn't already have a duplicate though.
        return count > 0 ? count : Location.countByLocationNumber(id)
    }

    @Override
    List<Shipment> getAllUnassignedEntities() {
        return Shipment.findAll("from Shipment as s where shipmentNumber is null or shipmentNumber = ''")
    }

    @Override
    void setIdentifierOnEntity(String id, Shipment entity) {
        entity.shipmentNumber = id
    }
}
