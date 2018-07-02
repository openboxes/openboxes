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
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.product.ProductAssociation
import org.pih.warehouse.product.ProductAssociationTypeCode
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
        requisition.destination = stockMovement.destination
        requisition.origin = stockMovement.origin
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

        requisition = requisition.refresh()

        return StockMovement.createFromRequisition(requisition)
    }

    def updateStockMovement(StockMovement stockMovement) {
        log.info "Update stock movement " + stockMovement + " stockMovement.lineItems = " + stockMovement?.lineItems

        if (!stockMovement.validate()) {
            throw new ValidationException("Invalid stock movement", stockMovement.errors)
        }

        Requisition requisition = Requisition.get(stockMovement.id)
        if (!requisition) {
            requisition = new Requisition()
        }

        if (stockMovement.destination) requisition.destination = stockMovement.destination
        if (stockMovement.origin) requisition.origin = stockMovement.origin
        if (stockMovement.name) requisition.name = stockMovement.name
        if (stockMovement.description) requisition.description = stockMovement.description
        if (stockMovement.requestedBy) requisition.requestedBy = stockMovement.requestedBy
        if (stockMovement.dateRequested) requisition.dateRequested = stockMovement.dateRequested
        //if (stockMovement.identifier) requisition.requestNumber = stockMovement.identifier

        if (stockMovement.lineItems) {
            stockMovement.lineItems.each { StockMovementItem stockMovementItem ->
                RequisitionItem requisitionItem
                // Try to find a matching stock movement item
                if (stockMovementItem.id) {
                    requisitionItem = requisition.requisitionItems.find { it.id == stockMovementItem.id }
                    // We should not just assume that if
                    if (!requisitionItem) {
                        throw new IllegalArgumentException("Could not find stock movement item with ID ${stockMovementItem.id}")
                    }
                }

                // If requisition item is found, we update it
                if (requisitionItem) {
                    log.info "Item found " + requisitionItem.id

                    if (stockMovementItem.delete) {
                        log.info "Item deleted " + requisitionItem.id
                        requisitionItem.undoChanges()
                        requisition.removeFromRequisitionItems(requisitionItem)
                        requisitionItem.delete(flush:true)
                    }
                    else if (stockMovementItem.revert) {
                        log.info "Item reverted " + requisitionItem.id
                        requisitionItem.undoChanges()
                    }
                    else if (stockMovementItem.cancel) {
                        log.info "Item canceled " + requisitionItem.id
                        requisitionItem.cancelQuantity(stockMovementItem.reasonCode, stockMovementItem.comments)
                    }
                    else if (stockMovementItem.substitute) {
                        log.info "Item substituted " + requisitionItem.id
                        log.info "Substitutions: " + requisitionItem.product.substitutions
                        if (!requisitionItem.product.isValidSubstitution(stockMovementItem?.newProduct)) {
                            throw new IllegalArgumentException("Product ${stockMovementItem?.newProduct?.productCode} " +
                                    "${stockMovementItem?.newProduct?.name} is not a valid substitution of " +
                                    "${requisitionItem?.product?.productCode} ${requisitionItem?.product?.name}")
                        }
                        requisitionItem.chooseSubstitute(
                                stockMovementItem.newProduct,
                                null,
                                stockMovementItem.newQuantity?.intValueExact(),
                                stockMovementItem.reasonCode,
                                stockMovementItem.comments)
                    }
                    else {
                        log.info "Item updated " + requisitionItem.id
                        if (stockMovementItem.product) requisitionItem.product = stockMovementItem.product
                        if (stockMovementItem.inventoryItem) requisitionItem.inventoryItem = stockMovementItem.inventoryItem
                        if (stockMovementItem.quantityRequested) requisitionItem.quantity = stockMovementItem.quantityRequested
                        //if (stockMovementItem.recipient) requisitionItem.recipient = stockMovementItem.recipient
                        if (stockMovementItem.sortOrder) requisitionItem.orderIndex = stockMovementItem.sortOrder
                        if (stockMovementItem.quantityRevised) {
                            requisitionItem.changeQuantity(
                                    stockMovementItem?.quantityRevised?.intValueExact(),
                                    stockMovementItem.reasonCode,
                                    stockMovementItem.comments)
                        }
                    }
                    requisitionItem.save()
                }
                // Otherwise we create a new one
                else {
                    log.info "Item not found"
                    if (stockMovementItem.quantityRevised) {
                        throw new IllegalArgumentException("Cannot specify quantityRevised when creating a new item")
                    }
                    requisitionItem = new RequisitionItem()
                    requisitionItem.product = stockMovementItem.product
                    requisitionItem.inventoryItem = stockMovementItem.inventoryItem
                    requisitionItem.quantity = stockMovementItem.quantityRequested
                    //requisitionItem.recipient = stockMovementItem.recipient
                    requisitionItem.orderIndex = stockMovementItem.sortOrder
                    requisition.addToRequisitionItems(requisitionItem)
                }
            }
        }


        if (requisition.hasErrors() || !requisition.save(flush:true)) {
            throw new ValidationException("Invalid requisition", requisition.errors)
        }

        requisition = requisition.refresh()

        return StockMovement.createFromRequisition(requisition)
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

    def getStockMovements(Integer maxResults, Integer offset) {
        def requisitions = Requisition.listOrderByDateCreated([max: maxResults, offset: offset, sort: "desc"])
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
