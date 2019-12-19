/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 * */
package org.pih.warehouse.reporting

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.Shipment

class GoodsReceiptNoteController {

    StockMovementService stockMovementService

    def print = {
        Location currentLocation = Location.get(session.warehouse.id)
        Shipment shipment = Shipment.get(params.id)
        if (!shipment) {
            throw new IllegalStateException("Unable to locate a shipment associated with stock movement ${params.id}")
        }

        def binLocations = []
        shipment?.shipmentItems?.sort()?.each { shipmentItem ->
            shipmentItem.receiptItems.sort().each {
                binLocations += it.binLocation
            }
        }


        [shipment: shipment, binLocations: binLocations.unique(), currentLocation: currentLocation]
    }

}
