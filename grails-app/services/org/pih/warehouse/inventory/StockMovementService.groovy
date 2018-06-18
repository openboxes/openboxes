/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.inventory

import org.apache.commons.lang.NotImplementedException
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.shipping.Shipment

class StockMovementService {

    boolean transactional = true

    def createStockMovement(StockMovement stockMovement) {
        throw new NotImplementedException("Create stock movement has not been implemented")
    }

    def updateStockMovement(StockMovement stockMovement) {
        throw new NotImplementedException("Update stock movement has not been implemented")
    }

    def deleteStockMovement(String id) {
        throw new NotImplementedException("Create stock movement has not been implemented")
    }

    def getStockMovements(Integer maxResults) {
        def shipments = Shipment.listOrderByDateCreated([max: maxResults, sort: "desc"])
        def stockMovements = shipments.collect { shipment ->
            return StockMovement.createFromShipment(shipment)
        }
        return stockMovements
    }

    def getStockMovement(String id) {
        Shipment shipment = Shipment.read(id)
        return StockMovement.createFromShipment(shipment)
    }
}
