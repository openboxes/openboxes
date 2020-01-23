package org.pih.warehouse.api

import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.validation.Validateable
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.ReferenceNumber
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatusCode
import org.pih.warehouse.shipping.ShipmentType

enum StockMovementType {

    INBOUND('Inbound'),
    OUTBOUND('Outbound')

    String name

    StockMovementType(String name) { this.name = name }

    static list() {
        [INBOUND, OUTBOUND]
    }
}

@Validateable
class StockMovement {

    String id
    String name
    String description
    String identifier
    String statusCode

    Location origin
    Location destination
    Person requestedBy
    Person createdBy
    Date dateRequested

    // Shipment information
    Date dateShipped
    ShipmentType shipmentType
    ShipmentStatusCode receiptStatusCode
    String trackingNumber
    String driverName
    String comments
    String currentStatus
    Float totalValue

    StockMovementType stockMovementType

    PickPage pickPage
    EditPage editPage
    PackPage packPage

    List<StockMovementItem> lineItems =
            LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(StockMovementItem.class))

    Requisition stocklist
    Requisition requisition
    Order order
    Shipment shipment
    List documents

    static constraints = {
        id(nullable: true)
        name(nullable: true)
        description(nullable: true)
        statusCode(nullable: true)
        origin(nullable: false)
        destination(nullable: false)
        stocklist(nullable: true)
        requestedBy(nullable: false)
        dateRequested(nullable: false)
        stockMovementType(nullable: true)
        receiptStatusCode(nullable: true)
        shipment(nullable: true)
        dateShipped(nullable: true)
        shipmentType(nullable: true)
        trackingNumber(nullable: true)
        driverName(nullable: true)
        comments(nullable: true)
        totalValue(nullable: true)
    }


    Map toJson() {
        return [
                id                : id,
                name              : name,
                description       : description,
                statusCode        : statusCode,
                identifier        : requisition?.requestNumber,
                origin            : origin,
                destination       : destination,
                hasManageInventory: origin?.supports(ActivityCode.MANAGE_INVENTORY),
                stocklist         : [id: stocklist?.id, name: stocklist?.name],
                dateRequested     : dateRequested?.format("MM/dd/yyyy"),
                dateShipped       : dateShipped?.format("MM/dd/yyyy HH:mm XXX"),
                shipmentType      : shipmentType,
                shipmentStatus    : currentStatus,
                trackingNumber    : trackingNumber,
                driverName        : driverName,
                comments          : comments,
                requestedBy       : requestedBy,
                lineItems         : lineItems,
                pickPage          : pickPage,
                editPage          : editPage,
                packPage          : packPage,
                associations      : [
                        requisition: [id: requisition?.id, requestNumber: requisition?.requestNumber, status: requisition?.status?.name()],
                        shipment   : [id: shipment?.id, shipmentNUmber: shipment?.shipmentNumber, status: shipment?.currentStatus?.name()],
                        shipments  : requisition?.shipments?.collect {
                            [id: it?.id, shipmentNumber: it?.shipmentNumber, status: it?.currentStatus?.name()]
                        },
                        documents  : documents
                ],
        ]
    }

    /**
     * Return the requisition status of the stock movement.
     *
     * @return
     */
    String getStatus() {
        return requisition?.status
    }

    /**
     * Return the receipt status of the associated stock movement.
     *
     * @return
     */
    ShipmentStatusCode getShipmentStatusCode() {
        return shipment?.status?.code ?: ShipmentStatusCode.PENDING

    }

    /**
     * Return total value of the issued shipment
     *
     * @return
     */
    Float getTotalValue() {
        def itemsWithPrice = shipment?.shipmentItems?.findAll { it.product.pricePerUnit }
        return itemsWithPrice.collect { it?.quantity * it?.product?.pricePerUnit }.sum() ?: 0
    }

    /**
     * “FROM.TO.DATEREQUESTED.STOCKLIST.TRACKING#.DESCRIPTION”
     *
     * @return
     */
    String generateName() {
        final String separator =
                ConfigurationHolder.config.openboxes.generateName.separator ?: Constants.DEFAULT_NAME_SEPARATOR

        String originIdentifier = origin?.locationNumber ?: origin?.name
        String destinationIdentifier = destination?.locationNumber ?: destination?.name
        String name = "${originIdentifier}${separator}${destinationIdentifier}"
        if (dateRequested) name += "${separator}${dateRequested?.format("ddMMMyyyy")}"
        if (stocklist?.name) name += "${separator}${stocklist.name}"
        if (trackingNumber) name += "${separator}${trackingNumber}"
        if (description) name += "${separator}${description}"
        name = name.replace(" ", "")
        return name
    }

    static StockMovement createFormOrder(Order order) {
        StockMovement stockMovement = new StockMovement(
            destination: order.destination,
            origin: order.origin,
            dateRequested: new Date(),
            requestedBy: order.orderedBy,
            description: order.name,
            statusCode:"CREATED"
        )

        if (order.orderItems) {
            order.orderItems.each { orderItem ->
                StockMovementItem stockMovementItem = StockMovementItem.createFromOrderItem(orderItem)
                stockMovementItem.sortOrder = stockMovement.lineItems ? stockMovement.lineItems.size() * 100 : 0
                stockMovement.lineItems.add(stockMovementItem)
            }
        }

        return stockMovement
    }


    static StockMovement createFromRequisition(Requisition requisition) {
        Shipment shipment = Shipment.findByRequisition(requisition)
        ReferenceNumber trackingNumber = shipment?.referenceNumbers?.find { ReferenceNumber rn ->
            rn.referenceNumberType.id == Constants.TRACKING_NUMBER_TYPE_ID
        }

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
                requisition: requisition,
                shipment: shipment,
                comments: shipment?.additionalInformation,
                shipmentType: shipment?.shipmentType,
                dateShipped: shipment?.expectedShippingDate,
                driverName: shipment?.driverName,
                trackingNumber: trackingNumber?.identifier,
                currentStatus: shipment?.currentStatus,
                stocklist: requisition?.requisitionTemplate
        )

        // Include all requisition items except those that are substitutions or modifications because the
        // original requisition item will represent these changes
        if (requisition.requisitionItems) {
            SortedSet<RequisitionItem> requisitionItems = new TreeSet<RequisitionItem>(requisition.requisitionItems)
            requisitionItems.each { requisitionItem ->
                if (!requisitionItem.parentRequisitionItem) {
                    StockMovementItem stockMovementItem = StockMovementItem.createFromRequisitionItem(requisitionItem)
                    stockMovement.lineItems.add(stockMovementItem)
                }
            }
        }
        return stockMovement
    }
}

enum DocumentGroupCode {

    EXPORT('Export'),
    INVOICE('Invoice'),
    PICKLIST('Pick list'),
    PACKING_LIST('Packing List'),
    CERTIFICATE_OF_DONATION('Certificate of Donation'),
    DELIVERY_NOTE('Delivery Note'),
    GOODS_RECEIPT_NOTE('Goods Receipt Note'),

    final String description

    DocumentGroupCode(String description) {
        this.description = description
    }

    static list() {
        return [EXPORT, INVOICE, PICKLIST, PACKING_LIST, CERTIFICATE_OF_DONATION, DELIVERY_NOTE, GOODS_RECEIPT_NOTE]
    }

}

class PickPage {
    List<PickPageItem> pickPageItems = []

    static constraints = {
        pickPageItems(nullable: true)
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
        editPageItems(nullable: true)
    }

    Map toJson() {
        return [
                editPageItems: editPageItems
        ]
    }
}

class PackPage {
    List<PackPageItem> packPageItems = []

    static constraints = {
        packPageItems(nullable: true)
    }

    Map toJson() {
        return [
                packPageItems: packPageItems
        ]
    }
}
