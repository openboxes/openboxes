package org.pih.warehouse.inventory

import grails.validation.Validateable
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.api.StockMovementType
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionSourceType
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.requisition.RequisitionType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentStatusCode
import org.pih.warehouse.shipping.ShipmentType
import org.pih.warehouse.api.StockMovementStatusContext
import util.StockMovementStatusResolver

class OutboundStockMovement implements Serializable, Validateable {

    String id
    String name
    String description
    String identifier

    Location origin
    Location destination

    Person createdBy
    Person updatedBy

    Date dateCreated
    Date lastUpdated

    Date dateRequested
    Person requestedBy

    Integer lineItemCount

    Date dateShipped
    Date expectedDeliveryDate

    ShipmentType shipmentType

    String driverName
    String comments
    String trackingNumber
    ShipmentStatusCode shipmentStatus

    RequisitionStatus status
    Requisition stocklist
    RequisitionType requestType
    RequisitionSourceType sourceType // temporary sourceType field for ELECTRONIC and PAPER types

    StockMovementType stockMovementType
    StockMovementStatusCode statusCode

    Requisition requisition
    Shipment shipment
    Order order

    Integer statusSortOrder

    List<ShipmentStatusCode> receiptStatusCodes // For filtering
    List<RequisitionStatus> requisitionStatusCodes // For filtering

    List<StockMovementItem> lineItems

    Boolean isFromOrder = Boolean.FALSE
    Boolean isShipped = Boolean.FALSE
    Boolean isReceived = Boolean.FALSE

    List documents

    static transients = [
            "receiptStatusCodes",
            "requisitionStatusCodes",
            "lineItems",
            "isFromOrder",
            "isShipped",
            "isReceived",
            "documents",
            "totalValue",
            "pending",
            "electronicType",
            "approvers",
            "pendingApproval",
            "isReturn"
    ]

    static mapping = {
        version false
        cache usage: "read-only"
        table "stock_movement"

        statusSortOrder formula: RequisitionStatus.getStatusSortOrderFormula()
    }

    static constraints = {
        id(nullable: true)
        name(nullable: true)
        description(nullable: true)
        origin(nullable: false)
        destination(nullable: false)
        stocklist(nullable: true)
        requestedBy(nullable: false)
        dateRequested(nullable: false)

        dateShipped(nullable: true)
        expectedDeliveryDate(nullable: true)
        shipmentType(nullable: true)
        trackingNumber(nullable: true)
        driverName(nullable: true)
        comments(nullable: true)

        shipment(nullable:true)
        requisition(nullable:true)
        order(nullable: true)

        dateCreated(nullable: true)
        lastUpdated(nullable: true)
        requestType(nullable: true)
        sourceType(nullable: true)

        stockMovementType(nullable: true)
        statusCode(nullable: true)

        statusSortOrder(nullable: true)
    }


    Map toJson() {
        return [
                id                : id,
                name              : name,
                description       : description,
                stockMovementType : stockMovementType,
                statusCode        : statusCode?.toString(),
                identifier        : identifier,
                origin            : origin,
                destination       : destination,
                hasManageInventory: origin?.supports(ActivityCode.MANAGE_INVENTORY),
                stocklist         : [
                        id  : stocklist?.id,
                        name: stocklist?.name
                ],
                replenishmentType : stocklist?.replenishmentTypeCode,
                dateRequested       : dateRequested?.format("MM/dd/yyyy"),
                dateShipped         : dateShipped?.format("MM/dd/yyyy HH:mm XXX"),
                expectedDeliveryDate: expectedDeliveryDate?.format("MM/dd/yyyy HH:mm XXX"),
                shipmentType        : shipmentType,
                shipmentStatus      : shipmentStatus?.toString(),
                trackingNumber      : trackingNumber,
                driverName          : driverName,
                comments            : comments,
                requestedBy         : requestedBy,
                lineItems           : lineItems,
                lineItemCount       : lineItemCount,
                associations        : [
                        requisition: [id: requisition?.id, requestNumber: requisition?.requestNumber, status: requisition?.status?.name()],
                        shipment   : [id: shipment?.id, shipmentNumber: shipment?.shipmentNumber, status: shipment?.currentStatus?.name()],
                        shipments  : requisition?.shipments?.collect {
                            [id: it?.id, shipmentNumber: it?.shipmentNumber, status: it?.currentStatus?.name()]
                        },
                        documents  : documents
                ],
                isFromOrder         : isFromOrder,
                isShipped           : isShipped,
                isReceived          : isReceived,
                shipped             : isShipped,
                received            : isReceived,
                requestType         : requestType,
                sourceType          : sourceType?.name,
        ]
    }

    List<StockMovementItem> getLineItems() {
        if (!lineItems) {
            buildLineItems()
        }

        return lineItems
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

    List<Person> getApprovers() {
        return requisition?.approvers?.toList()
    }

    Boolean isPendingApproval() {
        return requisition?.status == RequisitionStatus.PENDING_APPROVAL
    }

    Boolean isPending() {
        return shipment?.currentStatus == ShipmentStatusCode.PENDING
    }

    Boolean isElectronicType() {
        sourceType == RequisitionSourceType.ELECTRONIC
    }

    Boolean hasBeenIssued() {
        return requisition?.status == RequisitionStatus.ISSUED
    }

    Boolean hasBeenShipped() {
        return shipment?.currentStatus == ShipmentStatusCode.SHIPPED
    }

    Boolean hasBeenPartiallyReceived() {
        return shipment?.currentStatus == ShipmentStatusCode.PARTIALLY_RECEIVED
    }

    Boolean hasBeenReceived() {
        return shipment?.currentStatus == ShipmentStatusCode.RECEIVED
    }

    @Deprecated
    Map<String, String> getDisplayStatus() {
        StockMovementStatusContext stockMovementContext = new StockMovementStatusContext(
                order: order,
                requisition: requisition,
                shipment: shipment,
                origin: origin,
                destination: destination
        )
        Enum status = StockMovementStatusResolver.getStatus(stockMovementContext)
        return StockMovementStatusResolver.getStatusMetaData(status)
    }

    Boolean isDeleteOrRollbackAuthorized(Location currentLocation) {
        Location origin = requisition?.origin?:shipment?.origin
        Location destination = requisition?.destination?:shipment?.destination
        boolean isOrigin = origin?.id == currentLocation.id
        boolean isDestination = destination?.id == currentLocation.id
        boolean canOriginManageInventory = origin?.supports(ActivityCode.MANAGE_INVENTORY)
        boolean isCentralPurchasingEnabled = currentLocation?.supports(ActivityCode.ENABLE_CENTRAL_PURCHASING)
        return (
                (canOriginManageInventory && isOrigin) ||
                (!canOriginManageInventory && isDestination) ||
                (isCentralPurchasingEnabled && isFromOrder) ||
                (isDestination && electronicType)
        )
    }

    Boolean isEditAuthorized(Location currentLocation) {
        boolean isSameOrigin = origin?.id == currentLocation?.id
        boolean isSameDestination = destination?.id == currentLocation?.id
        boolean isDepot = origin?.isDepot()
        boolean isCentralPurchasingEnabled = currentLocation?.supports(ActivityCode.ENABLE_CENTRAL_PURCHASING)

        // TODO: REFACTOR THIS LINE!
        return !hasBeenReceived() && !hasBeenPartiallyReceived() && (isSameOrigin || (!isDepot && isSameDestination) || !isPending() || isElectronicType() || (isCentralPurchasingEnabled && isFromOrder))
    }

    Boolean isReceivingAuthorized(Location currentLocation) {
        boolean isSameDestination = destination?.id == currentLocation?.id

        return !hasBeenReceived() && (hasBeenIssued() || hasBeenShipped() || hasBeenPartiallyReceived()) && isSameDestination
    }

    void buildLineItems() {
        def lineItems = new ArrayList()

        if (order) {
            if (order.orderItems) {
                order.orderItems.findAll{ it.orderItemStatusCode != OrderItemStatusCode.CANCELED && it.getQuantityRemainingToShip() > 0 }.each { orderItem ->
                    StockMovementItem stockMovementItem = StockMovementItem.createFromOrderItem(orderItem)
                    stockMovementItem.sortOrder = lineItems ? lineItems.size() * 100 : 0
                    lineItems.add(stockMovementItem)
                }
            }
        } else if (requisition) {
            if (requisition.requisitionItems) {
                SortedSet<RequisitionItem> requisitionItems = new TreeSet<RequisitionItem>(requisition.requisitionItems)
                requisitionItems.each { requisitionItem ->
                    if (!requisitionItem.parentRequisitionItem) {
                        StockMovementItem stockMovementItem = StockMovementItem.createFromRequisitionItem(requisitionItem)
                        lineItems.add(stockMovementItem)
                    }
                }
            }
        } else if (shipment && shipment.shipmentItems) {
            shipment.shipmentItems.each { ShipmentItem shipmentItem ->
                StockMovementItem stockMovementItem = StockMovementItem.createFromShipmentItem(shipmentItem)
                if (!stockMovementItem.sortOrder) {
                    stockMovementItem.sortOrder = lineItems ? lineItems.size() * 100 : 0
                }

                lineItems.add(stockMovementItem)
            }
        }

        this.lineItems = lineItems
    }

    Boolean isInApprovalState() {
        return requisition?.status in [RequisitionStatus.APPROVED, RequisitionStatus.REJECTED]
    }

    // Function for checking if user in exact location can edit request
    // (with required approval)
    Boolean canUserEdit(String userId, Location location) {
        User user = User.get(userId)
        Boolean isUserRequestor = user.id == requestedBy?.id
        Boolean isLocationOrigin = origin?.id == location?.id
        Boolean isLocationDestination = destination?.id == location?.id
        return (isUserRequestor &&
                status == RequisitionStatus.PENDING_APPROVAL &&
                (isLocationDestination || isLocationOrigin)) ||
                (status == RequisitionStatus.APPROVED && isLocationOrigin)
    }

    Boolean canRollbackApproval(String userId, Location location) {
        User user = User.get(userId)
        return (isInApprovalState() &&
                (user.hasRoles(location, [RoleType.ROLE_REQUISITION_APPROVER]) ||
                user.hasRoles(location, [RoleType.ROLE_ADMIN]) ||
                user.hasRoles(location, [RoleType.ROLE_SUPERUSER]) ||
                user?.id == requestedBy?.id))
    }

    Boolean isApprovalRequired() {
        // The requisition status has to be lower than PICKING (so comparing them will return -1)
        return requisition?.approvalRequired && origin?.approvalRequired && RequisitionStatus.compare(requisition.status, RequisitionStatus.PICKING) == -1
    }

    // This has to be named with the get prefix to align with the StockMovement DTO
    boolean getIsReturn() {
        return shipment?.isFromReturnOrder
    }
}
