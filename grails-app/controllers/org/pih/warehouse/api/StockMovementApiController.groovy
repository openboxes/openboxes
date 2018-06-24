/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api

import grails.converters.JSON
import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.order.Order
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

class StockMovementApiController {

    def stockMovementService

    def list = {
        def stockMovements = stockMovementService.getStockMovements(10)
//        if (params.fields) {
//            String [] propertyNames = params.fields?.split(",")
//            stockMovements = stockMovements.collect { StockMovement stockMovement ->
//                return stockMovement.getPropertyMap(propertyNames)
//            }
//        }
        render ([stockMovements.collect { StockMovement stockMovement -> stockMovement.toJson() }] as JSON)
        render ([data:stockMovements] as JSON)
    }

    def read = {
        StockMovement stockMovement = stockMovementService.getStockMovement(params.id)
        render ([data:stockMovement] as JSON)
    }

    def create = { StockMovement stockMovement ->
        stockMovement = stockMovementService.createStockMovement(stockMovement)
        response.status = 201
        render ([data:stockMovement] as JSON)
	}

    def update = { StockMovement stockMovement ->
        stockMovementService.updateStockMovement(stockMovement)
    }

    def delete = {
        stockMovementService.deleteStockMovement(params.id)
        render status: 204
    }

}

enum StockMovementType {

    INBOUND('Inbound'),
    OUTBOUND('Outbound'),
    OUTBOUND_STOCKLIST('Outbound with stocklist');

    String name

    StockMovementType(String name) { this.name = name; }

    static list() {
        [ INBOUND, OUTBOUND, OUTBOUND_STOCKLIST]
    }
}

class StockMovement {

    String id
    String name
    String description
    Location origin
    Location destination
    Person requestedBy
    Date dateRequested

    StockMovementType stockMovementType

    List<StockMovementItem> lineItems =
            LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(StockMovementItem.class));

    Requisition stocklist
    Requisition requisition
    Order order
    Shipment shipment

    static constraints = {
        id(nullable:true)
        name(nullable:false)
        description(nullable:true)
        origin(nullable:false)
        destination(nullable:false)
        stocklist(nullable:true)
        requestedBy(nullable:false)
        dateRequested(nullable:false)
        stockMovementType(nullable:true)
    }


    Map getPropertyMap(String [] propertyNames) {
        Map propertyMap = [:]

        propertyNames.each { propertyName ->
            propertyMap << ["${propertyName}": this."${propertyName}"]
        }
        return propertyMap;
    }

    Map toJson() {
        return [
                id: id,
                name: name,
                description: description,
                identifier: requisition?.requestNumber,
                origin: origin.id,
                destination: destination.id,
                dateRequested: dateRequested,
                lineItems: lineItems.collect { it.toJson() }
        ]
    }


    static StockMovement createFromRequisition(Requisition requisition) {
        StockMovement stockMovement = new StockMovement(
                id: requisition.id,
                name: requisition.name,
                description: requisition.name,
                origin: requisition.origin,
                destination: requisition.destination,
                dateRequested: requisition.dateCreated,
                requisition: requisition
        )

        requisition.requisitionItems.each { requisitionItem ->
            StockMovementItem stockMovementItem = StockMovementItem.createFromRequisitionItem(requisitionItem)
            stockMovement.lineItems.add(stockMovementItem)
        }
        return stockMovement

    }

    static StockMovement createFromShipment(Shipment shipment) {
        StockMovement stockMovement = new StockMovement(
                id: shipment.id,
                name: shipment.name,
                description: shipment.name,
                origin: shipment.origin,
                destination: shipment.destination,
                dateRequested: shipment.dateCreated,
                shipment: shipment
        )

        shipment.shipmentItems.each { shipmentItem ->
            StockMovementItem stockMovementItem = StockMovementItem.createFromShipmentItem(shipmentItem)
            stockMovement.lineItems.add(stockMovementItem)
        }
        return stockMovement
    }
}

class StockMovementItem {

    String id
    String productCode
    Product product
    InventoryItem inventoryItem
    BigDecimal quantityRequested
    BigDecimal quantityAllowed
    BigDecimal quantityAvailable
    Person recipient

    String palletName
    String boxName

    Integer sortOrder


    static constraints = {
        id(nullable:true)
        productCode(nullable:false)
        product(nullable:true)
        inventoryItem(nullable:true)
        quantityRequested(nullable:false)
        quantityAllowed(nullable:true)
        quantityAvailable(nullable:true)
        recipient(nullable:true)
        palletName(nullable:true)
        boxName(nullable:true)
        sortOrder(nullable:true)
    }

    Map toJson() {
        return [
                id: id,
                productCode: productCode,
                product: product,
                palletName: palletName,
                boxName: boxName,
                quantityRequested: quantityRequested,
                quantityAllowed: quantityAllowed,
                quantityAvailable: quantityAvailable,
                recipient: recipient,
                sortOrder: sortOrder
        ]
    }

    static StockMovementItem createFromShipmentItem(ShipmentItem shipmentItem) {

        String palletName, boxName
        if(shipmentItem?.container?.parentContainer) {
            palletName = shipmentItem?.container?.parentContainer?.name
            boxName = shipmentItem?.container?.name
        } else if (shipmentItem.container) {
            palletName = shipmentItem?.container?.name
        }

        return new StockMovementItem(id: shipmentItem?.id,
                productCode: shipmentItem?.product?.productCode,
                product: shipmentItem?.inventoryItem?.product,
                inventoryItem: shipmentItem?.inventoryItem,
                quantityRequested: shipmentItem?.quantity,
                quantityAllowed: null,
                quantityAvailable: null,
                palletName:palletName,
                boxName:boxName,
                recipient: shipmentItem.recipient,
                sortOrder: null

        )
    }

    static StockMovementItem createFromRequisitionItem(RequisitionItem requisitionItem) {
        return new StockMovementItem(id: requisitionItem.id,
                productCode: requisitionItem?.product?.productCode,
                product: requisitionItem?.product,
                inventoryItem: requisitionItem?.inventoryItem,
                quantityRequested: requisitionItem.quantity,
                quantityAllowed: null,
                quantityAvailable: null,
                palletName:null,
                boxName:null,
                recipient: requisitionItem.recipient,
                sortOrder: requisitionItem.orderIndex

        )
    }

}