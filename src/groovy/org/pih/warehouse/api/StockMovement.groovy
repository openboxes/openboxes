package org.pih.warehouse.api

import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import org.pih.warehouse.core.DocumentType
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
    String statusCode

    Location origin
    Location destination
    Person requestedBy
    Date dateRequested

    // Shipment information
    Date dateShipped
    ShipmentType shipmentType
    String trackingNumber
    String driverName
    String comments

    StockMovementType stockMovementType

    PickPage pickPage
    EditPage editPage

    List<StockMovementItem> lineItems =
            LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(StockMovementItem.class));

    Requisition stocklist
    Requisition requisition
    Order order
    Shipment shipment
    List documents

    static constraints = {
        id(nullable:true)
        name(nullable:true)
        description(nullable:true)
        statusCode(nullable:true)
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


    Map toJson() {
        return [
                id: id,
                name: generateName(),
                description: description,
                statusCode: statusCode,
                identifier: requisition?.requestNumber,
                origin: [id: origin?.id, name: origin?.name],
                destination: [id: destination?.id, name: destination?.name],
                stocklist: [id: stocklist?.id, name: stocklist?.name],
                dateRequested: dateRequested?.format("MM/dd/yyyy"),
                requestedBy: requestedBy,
                lineItems: lineItems,
                pickPage: pickPage,
                editPage: editPage,
                associations: [
                    requisition: [id: requisition.id, requestNumber: requisition.requestNumber, status: requisition?.status?.name()],
                    shipments: requisition?.shipments?.collect { [id: it.id, shipmentNumber: it.shipmentNumber, status: it?.currentStatus?.name()] },
                    documents: documents
                ],
        ]
    }

    /**
     * Return the status of the associated requisition.
     *
     * @return
     */
    String getStatus() {
        return requisition?.status
    }


    /**
     * “FROM.TO.DATEREQUESTED.STOCKLIST.TRACKING#.DESCRIPTION”
     *
     * @return
     */
    String generateName() {
        String name = "${origin?.name}.${destination?.name}.${dateRequested.format("ddMMMyyyy")}"
        if (stocklist?.name) name += ".${stocklist.name}"
        if (trackingNumber) name += ".${trackingNumber}"
        if (description) name += ".${description}"
        name = name.toUpperCase().replace(" ", "")
        return name
    }


    static StockMovement createFromRequisition(Requisition requisition) {
        StockMovement stockMovement = new StockMovement(
                id: requisition.id,
                name: requisition.name,
                identifier: requisition.requestNumber,
                description: requisition.description,
                statusCode: requisition?.status?.name(),
                origin: requisition.origin,
                destination: requisition.destination,
                dateRequested: requisition.dateRequested,
                requestedBy: requisition.requestedBy,
                requisition: requisition
        )

        stockMovement.shipment = Shipment.findByRequisition(requisition)

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
                statusCode: shipment.status?.name(),
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

enum DocumentGroupCode {
    INVOICE('Invoice'),
    PICKLIST('Pick list'),
    PACKING_LIST('Packing List'),
    CERTIFICATE_OF_DONATION('Certificate of Donation'),
    DELIVERY_NOTE('Delivery Note'),
    GOODS_RECEIPT_NOTE('Goods Receipt Note')

    final String description

    DocumentGroupCode(String description) {
        this.description = description
    }

    static list() {
        return [INVOICE, PICKLIST, PACKING_LIST, CERTIFICATE_OF_DONATION, DELIVERY_NOTE, GOODS_RECEIPT_NOTE]
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

class EditPage {
    List<EditPageItem> editPageItems = []

    static constraints = {
        editPageItems(nullable:true)
    }

    Map toJson() {
        return [
                editPageItems: editPageItems
        ]
    }
}