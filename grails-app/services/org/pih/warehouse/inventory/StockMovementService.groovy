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

import grails.validation.ValidationException
import org.apache.commons.lang.NotImplementedException
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment

class StockMovementService {

    boolean transactional = true

    def createStockMovement(StockMovement stockMovement) {

        if (!stockMovement.validate()) {
            throw new ValidationException("Invalid stock movement", stockMovement.errors)
        }

        Requisition requisition = Requisition.get(stockMovement.id)
        if (!requisition) {
            requisition = new Requisition()
        }

        // Origin and destination are backwards on purpose. The origin/destination of the requisition are from the
        // perspective of the requisition, while the origin/destination of the stock movement related to the stock
        // being transferred.
        requisition.status = RequisitionStatus.CREATED
        requisition.destination = stockMovement.origin
        requisition.origin = stockMovement.destination
        requisition.name = stockMovement.name
        requisition.description = stockMovement.description
        requisition.requestedBy = stockMovement.requestedBy
        requisition.dateRequested = stockMovement.dateRequested

        // If the user specified a stocklist then we should automatically clone it as long as there are no
        // requisition items already added to the requisition
        if (stockMovement.stocklist && !requisition.requisitionItems) {
            stockMovement.stocklist.requisitionItems.each { stocklistItem ->
                RequisitionItem requisitionItem = new RequisitionItem()
                requisitionItem.product = stocklistItem.product
                requisitionItem.quantity = stocklistItem.quantity
                requisitionItem.orderIndex = stocklistItem.orderIndex
                requisition.addToRequisitionItems(requisitionItem)
            }
        }
        if (requisition.hasErrors() || !requisition.save(flush:true)) {
            throw new ValidationException("Invalid requisition", requisition.errors)
        }

        return StockMovement.createFromRequisition(requisition)
    }

    def updateStockMovement(StockMovement stockMovement) {
        throw new NotImplementedException("Update stock movement has not been implemented")
    }

    def deleteStockMovement(String id) {
        StockMovement stockMovement = getStockMovement(id)
        if (stockMovement?.requisition) {
            stockMovement.requisition.delete()
        }
        if (stockMovement?.shipment) {
            stockMovement.shipment.delete()
        }
    }

    def getStockMovements(Integer maxResults) {
        def requisitions = Requisition.listOrderByDateCreated([max: maxResults, sort: "desc"])
        def stockMovements = requisitions.collect { requisition ->
            return StockMovement.createFromRequisition(requisition)
        }
        return stockMovements
    }

    def getStockMovement(String id) {
        Requisition requisition = Requisition.read(id)
        if (!requisition) {
            throw new ObjectNotFoundException(id, StockMovement.class.toString())
        }
        return StockMovement.createFromRequisition(requisition)
    }
}
