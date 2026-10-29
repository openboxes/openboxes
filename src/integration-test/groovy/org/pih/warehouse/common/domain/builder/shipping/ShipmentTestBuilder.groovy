package org.pih.warehouse.common.domain.builder.shipping

import groovy.transform.InheritConstructors

import org.pih.warehouse.common.domain.builder.base.TestBuilder
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentType

@InheritConstructors
class ShipmentTestBuilder extends TestBuilder<Shipment> {

    @Override
    protected Map<String, Object> getDefaults() {
        return [
                name: randomUtil.randomStringFieldValue("Shipment"),
                shipmentType: ShipmentType.get(Constants.DEFAULT_SHIPMENT_TYPE_ID),
                expectedShippingDate: new Date(),
        ] as Map<String, Object>
    }

    ShipmentTestBuilder origin(Location origin) {
        args.origin = origin
        return this
    }

    ShipmentTestBuilder destination(Location destination) {
        args.destination = destination
        return this
    }
}
