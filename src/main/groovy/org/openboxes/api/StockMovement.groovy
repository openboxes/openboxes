package org.pih.warehouse.api

import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.validation.Validateable
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.StockMovementStatusCode
import org.pih.warehouse.order.Order
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.ReferenceNumber
import org.pih.warehouse.shipping.ReferenceNumberType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
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
    Person updatedBy

    Date dateRequested
    Date dateShipped
    Date dateCreated
    Date lastUpdated

    ShipmentType shipmentType
    ShipmentStatusCode receiptStatusCode
    String trackingNumber
    String driverName
    String comments
    String currentStatus
    Float totalValue

    StockMovementType stockMovementType
    StockMovementStatusCode stockMovementStatusCode


    List<StockMovementItem> lineItems =
            LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(StockMovementItem.class))

    Boolean isFromOrder = Boolean.FALSE
    Boolean isShipped = Boolean.FALSE
    Boolean isReceived = Boolean.FALSE

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
        stockMovementStatusCode(nullable: true)
        receiptStatusCode(nullable: true)
        dateShipped(nullable: true)
        shipmentType(nullable: true)
        trackingNumber(nullable: true)
        driverName(nullable: true)
        comments(nullable: true)
        totalValue(nullable: true)

        shipment(nullable:true)
        requisition(nullable:true)
        order(nullable: true)

        dateCreated(nullable: true)
        lastUpdated(nullable: true)
    }


    Map toJson() {
        return [
                id                : id,
                name              : name,
                description       : description,
                statusCode        : statusCode,
                identifier        : identifier,
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
                associations: [
                        requisition: [id: requisition?.id, requestNumber: requisition?.requestNumber, status: requisition?.status?.name()],
                        shipment   : [id: shipment?.id, shipmentNumber: shipment?.shipmentNumber, status: shipment?.currentStatus?.name()],
                        shipments  : requisition?.shipments?.collect {
                            [id: it?.id, shipmentNumber: it?.shipmentNumber, status: it?.currentStatus?.name()]
                        },
                        documents  : documents
                ],
                isFromOrder : isFromOrder,
                isShipped   : isShipped,
                isReceived  : isReceived,
                shipped     : isShipped,
                received    : isReceived,
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

    Boolean isDeleteOrRollbackAuthorized(Location currentLocation) {
        Location origin = requisition?.origin?:shipment?.origin
        Location destination = requisition?.destination?:shipment?.destination
        boolean isOrigin = origin?.id == currentLocation.id
        boolean isDestination = destination?.id == currentLocation.id
        boolean canOriginManageInventory = origin?.supports(ActivityCode.MANAGE_INVENTORY)
        return ((canOriginManageInventory && isOrigin) || (!canOriginManageInventory && isDestination))
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

    static StockMovement createFromShipment(Shipment shipment) {

        String statusCode = (shipment.status.code == ShipmentStatusCode.SHIPPED) ?
                RequisitionStatus.ISSUED.toString() : RequisitionStatus.PENDING.toString()

        ReferenceNumber trackingNumber = shipment?.referenceNumbers?.find { ReferenceNumber rn ->
            rn.referenceNumberType.id == Constants.TRACKING_NUMBER_TYPE_ID
        }

        StockMovement stockMovement = new StockMovement(
                id: shipment.id,
                name: shipment.name,
                description: shipment.description,
                shipmentType: shipment.shipmentType,
                statusCode: statusCode,
                dateShipped: shipment?.expectedShippingDate,
                identifier: shipment.shipmentNumber,
                origin: shipment.origin,
                destination: shipment.destination,
                dateRequested: shipment.dateCreated,
                dateCreated: shipment.dateCreated,
                lastUpdated: shipment.lastUpdated,
                requestedBy: shipment.createdBy,
                createdBy: shipment.createdBy,
                updatedBy: shipment.updatedBy,
                shipment: shipment,
                isFromOrder: shipment?.isFromPurchaseOrder,
                isShipped: shipment?.status?.code >= ShipmentStatusCode.SHIPPED,
                isReceived: shipment?.status?.code >= ShipmentStatusCode.RECEIVED,
                driverName: shipment.driverName,
                trackingNumber: trackingNumber?.identifier,
                comments: shipment.additionalInformation,
        )

        if (shipment.shipmentItems) {
            shipment.shipmentItems.each { ShipmentItem shipmentItem ->
                StockMovementItem stockMovementItem = StockMovementItem.createFromShipmentItem(shipmentItem)
                if (!stockMovementItem.sortOrder) {
                    stockMovementItem.sortOrder = stockMovement.lineItems ? stockMovement.lineItems.size() * 100 : 0
                }

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
                statusCode: RequisitionStatus.toStockMovementStatus(requisition.status)?.name(),
                origin: requisition.origin,
                destination: requisition.destination,
                dateRequested: requisition.dateRequested,
                dateCreated: requisition.dateCreated,
                lastUpdated: requisition.lastUpdated,
                requestedBy: requisition.requestedBy,
                createdBy: requisition.createdBy,
                updatedBy: requisition.updatedBy,
                requisition: requisition,
                shipment: shipment,
                comments: shipment?.additionalInformation,
                shipmentType: shipment?.shipmentType,
                dateShipped: shipment?.expectedShippingDate,
                driverName: shipment?.driverName,
                trackingNumber: trackingNumber?.identifier,
                currentStatus: shipment?.currentStatus,
                stocklist: requisition?.requisitionTemplate,
                isFromOrder: Boolean.FALSE,
                isShipped: shipment?.status?.code >= ShipmentStatusCode.SHIPPED,
                isReceived: shipment?.status?.code >= ShipmentStatusCode.RECEIVED

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
