package org.pih.warehouse.api

import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.order.Order
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment

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
                origin: [id: origin?.id, name: origin?.name],
                destination: [id: destination?.id, name: destination?.name],
                dateRequested: dateRequested?.format("MM/dd/yyyy"),
                requestedBy: requestedBy,
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
                dateRequested: requisition.dateRequested,
                requestedBy: requisition.requestedBy,
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
                dateRequested: shipment?.dateCreated,
                requestedBy: shipment.recipient,
                shipment: shipment
        )

        shipment.shipmentItems.each { shipmentItem ->
            StockMovementItem stockMovementItem = StockMovementItem.createFromShipmentItem(shipmentItem)
            stockMovement.lineItems.add(stockMovementItem)
        }
        return stockMovement
    }
}
