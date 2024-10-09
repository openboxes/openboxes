package org.pih.warehouse.shipping

import org.pih.warehouse.core.IdentifierService
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.identification.BlankIdentifierResolver

class ShipmentIdentifierService extends IdentifierService implements BlankIdentifierResolver<Shipment> {

    @Override
    String getPropertyKey() {
        return "shipment"
    }

    @Override
    protected Integer countDuplicates(String shipmentNumber) {
        // We use shipment.shipmentNumber as location.locationNumber when creating internal locations from shipments
        // (see LocationService.findOrCreateInternalLocation for details) so we need to check that the id is unique
        // for locations as well.
        Integer count = Shipment.countByShipmentNumber(shipmentNumber)

        // Only bother checking location if shipment doesn't already have a duplicate.
        return count > 0 ? count : Location.countByLocationNumber(shipmentNumber)
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
