package org.pih.warehouse.api

import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.order.Order
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentType

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
    String identifier
    Location origin
    Location destination
    Person requestedBy
    Date dateRequested

    // Status
    String stepNumber

    // Shipment information
    Date dateShipped
    ShipmentType shipmentType
    String trackingNumber
    String driverName
    String comments

    StockMovementType stockMovementType

    PickPage pickPage

    List<StockMovementItem> lineItems =
            LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(StockMovementItem.class));

    Requisition stocklist
    Requisition requisition
    Order order
    Shipment shipment

    static constraints = {
        id(nullable:true)
        name(nullable:true)
        description(nullable:true)
        origin(nullable:false)
        destination(nullable:false)
        stocklist(nullable:true)
        requestedBy(nullable:false)
        dateRequested(nullable:false)
        stockMovementType(nullable:true)
        shipment(nullable:true)
        dateShipped(nullable:true)
        shipmentType(nullable:true)
        trackingNumber(nullable:true)
        driverName(nullable:true)
        comments(nullable:true)
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
                lineItems: lineItems,
                pickPage: pickPage,
        ]
    }

    /**
     * “FROM.TO.DATEREQUESTED.STOCKLIST.TRACKING#.DESCRIPTION”
     *
     * @return
     */
    String generateName() {
        String name = "${origin?.name}.${destination?.name}.${dateRequested.format("dd/MMM/yyyy")}"
        if (stocklist?.name) name += ".${stocklist.name}"
        if (trackingNumber) name += ".${trackingNumber}"
        if (description) name += ".${description}"
        name = name.toUpperCase()
        return name
    }


    static StockMovement createFromRequisition(Requisition requisition) {
        StockMovement stockMovement = new StockMovement(
                id: requisition.id,
                name: requisition.name,
                identifier: requisition.requestNumber,
                description: requisition.description,
                origin: requisition.origin,
                destination: requisition.destination,
                dateRequested: requisition.dateRequested,
                requestedBy: requisition.requestedBy,
                requisition: requisition
        )

        // Include all requisition items except those that are substitutions or modifications because the
        // original requisition item will represent these changes
        requisition.requisitionItems.each { requisitionItem ->
            if (!requisitionItem.parentRequisitionItem) {
                StockMovementItem stockMovementItem = StockMovementItem.createFromRequisitionItem(requisitionItem)
                stockMovement.lineItems.add(stockMovementItem)
            }
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

class PickPage {
    List<PickPageItem> pickPageItems = []

    static constraints = {
        pickPageItems(nullable:true)
    }

    Map toJson() {
        return [
                pickPageItems: pickPageItems
        ]
    }

}