package org.pih.warehouse.api

import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import grails.util.Holders
import grails.validation.Validateable
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.StockMovementStatusCode
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionSourceType
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.requisition.RequisitionType
import org.pih.warehouse.shipping.ReferenceNumber
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentStatusCode
import org.pih.warehouse.shipping.ShipmentType
import org.pih.warehouse.auth.AuthService
import util.ConfigHelper
import util.StockMovementStatusResolver


class StockMovement implements Validateable{

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
    Date expectedDeliveryDate
    Date dateCreated
    Date lastUpdated
    Date dateDeliveryRequested

    ShipmentType shipmentType
    ShipmentStatusCode receiptStatusCode
    List<ShipmentStatusCode> receiptStatusCodes // For filtering
    List<RequisitionStatus> requisitionStatusCodes // For filtering
    String trackingNumber
    String driverName
    String comments
    String currentStatus
    Float totalValue

    StockMovementDirection stockMovementDirection
    StockMovementStatusCode stockMovementStatusCode


    List<StockMovementItem> lineItems =
            LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(StockMovementItem.class))

    Integer lineItemCount

    Boolean isFromOrder = Boolean.FALSE
    Boolean isShipped = Boolean.FALSE
    Boolean isReceived = Boolean.FALSE
    Boolean isReturn = Boolean.FALSE

    Requisition stocklist
    Requisition requisition
    RequisitionType requestType
    RequisitionSourceType sourceType // temporary sourceType field for ELECTRONIC and PAPER types
    Order order
    Shipment shipment
    List documents

    // Request approval fields
    List<Person> approvers

    static transients = [
            "electronicType",
            "pendingApproval"
    ]

    static constraints = {
        id(nullable: true)
        identifier(nullable: true)
        name(nullable: true)
        description(nullable: true)
        statusCode(nullable: true)
        currentStatus(nullable: true)
        status(nullable: true)
        origin(nullable: false)
        destination(nullable: false)
        stocklist(nullable: true)
        requestedBy(nullable: false)
        createdBy(nullable: true)
        updatedBy(nullable: true)
        dateRequested(nullable: false)
        dateDeliveryRequested(nullable: true)

        stockMovementDirection(nullable: true)
        stockMovementStatusCode(nullable: true)
        receiptStatusCode(nullable: true)
        dateShipped(nullable: true)
        expectedDeliveryDate(nullable: true)
        shipmentType(nullable: true)
        trackingNumber(nullable: true)
        driverName(nullable: true)
        comments(nullable: true)
        totalValue(nullable: true)
        lineItemCount(nullable: true)

        shipment(nullable:true)
        requisition(nullable:true)
        order(nullable: true)

        dateCreated(nullable: true)
        lastUpdated(nullable: true)
        requestType(nullable: true)
        sourceType(nullable: true)
    }

    Map toJson() {
        return [
            id                  : id,
            name                : name,
            description         : description,
            statusCode          : statusCode,
            displayStatus       : displayStatus,
            identifier          : identifier,
            origin              : [
                id                  : origin?.id,
                name                : origin?.name,
                locationNumber      : origin?.locationNumber,
                locationType        : origin?.locationType,
                locationGroup       : origin?.locationGroup,
                organizationName    : origin?.organization?.name,
                organizationCode    : origin?.organization?.code,
                isDepot             : origin?.isDepot(),
                supportedActivities : origin?.supportedActivities,
            ],
            destination         : [
                id                  : destination?.id,
                name                : destination?.name,
                locationNumber      : destination?.locationNumber,
                locationType        : destination?.locationType,
                locationGroup       : destination?.locationGroup,
                organizationName    : destination?.organization?.name,
                supportedActivities : destination?.supportedActivities,
                organizationCode    : destination?.organization?.code,
            ],
            order                : [
                id                  : shipment?.returnOrder?.id,
                name                : shipment?.returnOrder?.name,
                orderNumber         : shipment?.returnOrder?.orderNumber
            ],
            hasManageInventory  : origin?.supports(ActivityCode.MANAGE_INVENTORY),
            stocklist           : [
                id  : stocklist?.id,
                name: stocklist?.name
            ],
            replenishmentType   : stocklist?.replenishmentTypeCode,
            dateRequested       : dateRequested?.format("MM/dd/yyyy"),
            dateCreated         : dateCreated?.format("MM/dd/yyyy"),
            dateShipped         : dateShipped?.format("MM/dd/yyyy HH:mm XXX"),
            expectedDeliveryDate: expectedDeliveryDate?.format("MM/dd/yyyy HH:mm XXX"),
            dateDeliveryRequested : requisition?.dateDeliveryRequested,
            lastUpdated         : lastUpdated,
            shipmentType        : shipmentType,
            currentStatus       : currentStatus,
            shipmentStatus      : shipment?.status?.name,
            trackingNumber      : trackingNumber,
            driverName          : driverName,
            comments            : comments,
            currentEvent        : requisition?.mostRecentEvent,
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
            approvers           : approvers,
            isFromOrder         : isFromOrder,
            isReturn            : isReturn,
            isShipped           : isShipped,
            isReceived          : isReceived,
            isPartiallyReceived : hasBeenPartiallyReceived(),
            isElectronicType    : electronicType,
            isPending           : pending,
            shipped             : isShipped,
            received            : isReceived,
            requestType         : requestType,
            sourceType          : sourceType?.name,
            picklist            : [
                id: requisition?.picklist?.id
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
     * Return total value of the issued shipment
     *
     * @return
     */
    Float getTotalValue() {
        def itemsWithPrice = shipment?.shipmentItems?.findAll { it.product.pricePerUnit }
        return itemsWithPrice.collect { it?.quantity * it?.product?.pricePerUnit }.sum() ?: 0
    }

    /**
     * Return the stock movement directions based on a given location
     *
     * @return
     */
    StockMovementDirection getStockMovementDirection (Location currentLocation) {
        if(currentLocation == origin)
            return StockMovementDirection.OUTBOUND
        else if(currentLocation == destination || origin?.isSupplier())
            return StockMovementDirection.INBOUND
        else
            return null
    }

    Boolean isPending() {
        return shipment?.currentStatus == ShipmentStatusCode.PENDING
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

    Boolean isElectronicType() {
        requisition?.sourceType == RequisitionSourceType.ELECTRONIC
    }

    Boolean isPendingApproval() {
        return requisition?.status == RequisitionStatus.PENDING_APPROVAL
    }

    Boolean isDeleteOrRollbackAuthorized(Location currentLocation) {
        Location origin = requisition?.origin?:shipment?.origin
        Location destination = requisition?.destination?:shipment?.destination
        boolean isOrigin = origin?.id == currentLocation.id
        boolean isDestination = destination?.id == currentLocation.id
        boolean canOriginManageInventory = origin?.supports(ActivityCode.MANAGE_INVENTORY)
        boolean isCentralPurchasingEnabled = currentLocation?.supports(ActivityCode.ENABLE_CENTRAL_PURCHASING)

        // stock request /stockRequest/remove/:id
        if (electronicType) {
            User user = AuthService.currentUser
            def accessRule = ConfigHelper.findAccessRule("stockRequest", "remove")
            def userService = Holders.grailsApplication.mainContext.getBean("userService")
            if (!userService.isUserInRole(user, accessRule?.accessRules?.minimumRequiredRole)) {
                throw new IllegalAccessException("You don't have minimum required role to perform this action")
            }
        }

        return ((canOriginManageInventory && isOrigin) || (!canOriginManageInventory && isDestination) || (isCentralPurchasingEnabled && isFromOrder) || (electronicType && isDestination))
    }

    Boolean isEditAuthorized(Location currentLocation) {
        boolean isSameOrigin = origin?.id == currentLocation?.id
        boolean isSameDestination = destination?.id == currentLocation?.id
        boolean isDepot = origin?.isDepot()
        boolean isCentralPurchasingEnabled = currentLocation?.supports(ActivityCode.ENABLE_CENTRAL_PURCHASING)

        return !hasBeenReceived() && !hasBeenPartiallyReceived() && (isSameOrigin || (!isDepot && isSameDestination) || (!isFromOrder && !isPending()) || isElectronicType() || (isCentralPurchasingEnabled && isFromOrder))
    }

    Boolean isReceivingAuthorized(Location currentLocation) {
        boolean isSameDestination = destination?.id == currentLocation?.id

        return !hasBeenReceived() && (hasBeenIssued() || hasBeenShipped() || hasBeenPartiallyReceived()) && isSameDestination
    }

    /**
     * “FROM.TO.DATEREQUESTED.STOCKLIST.TRACKING#.DESCRIPTION”
     *
     * @return
     */
    String generateName() {
        final String separator =
                Holders.getConfig().getProperty("openboxes.generateName.separator") ?: Constants.DEFAULT_NAME_SEPARATOR

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
        return createFromShipment(shipment, Boolean.TRUE)
    }

    static StockMovement createFromShipment(Shipment shipment, Boolean includeStockMovementItems) {

        String statusCode = (shipment.status.code == ShipmentStatusCode.PENDING) ?
                RequisitionStatus.PENDING.toString() : RequisitionStatus.ISSUED.toString()

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
                expectedDeliveryDate: shipment?.expectedDeliveryDate,
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
                order: shipment?.returnOrder,
                isReturn: shipment?.isFromReturnOrder,
                isFromOrder: shipment?.isFromPurchaseOrder,
                isShipped: shipment?.status?.code >= ShipmentStatusCode.SHIPPED,
                isReceived: shipment?.status?.code >= ShipmentStatusCode.RECEIVED,
                driverName: shipment.driverName,
                trackingNumber: trackingNumber?.identifier,
                comments: shipment.additionalInformation,
                lineItemCount: shipment.shipmentItemCount
        )

        if (includeStockMovementItems && shipment.shipmentItems) {
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
        return createFromRequisition(requisition, Boolean.TRUE)
    }

    static StockMovement createFromRequisition(Requisition requisition, Boolean includeStockMovementItems) {
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
            expectedDeliveryDate: shipment?.expectedDeliveryDate,
            driverName: shipment?.driverName,
            trackingNumber: trackingNumber?.identifier,
            currentStatus: shipment?.currentStatus,
            stocklist: requisition?.requisitionTemplate,
            isFromOrder: Boolean.FALSE,
            isReturn: Boolean.FALSE,
            isShipped: shipment?.status?.code >= ShipmentStatusCode.SHIPPED,
            isReceived: shipment?.status?.code >= ShipmentStatusCode.RECEIVED,
            requestType: requisition?.type,
            lineItemCount: requisition.requisitionItemCount,
            approvers: requisition.approvers?.toList()
        )

        // Include all requisition items except those that are substitutions or modifications because the
        // original requisition item will represent these changes
        if (includeStockMovementItems && requisition.requisitionItems) {
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

    static StockMovement createFromOrder(Order order) {
        StockMovement stockMovement = new StockMovement(
                destination: order.destination,
                origin: order.origin,
                dateRequested: new Date(),
                requestedBy: order.orderedBy,
                description: order.orderNumber + ' ' + order.name,
                statusCode:"CREATED"
        )

        if (order.orderItems) {
            order.orderItems.findAll{ it.orderItemStatusCode != OrderItemStatusCode.CANCELED && it.getQuantityRemainingToShip() > 0 }.each { orderItem ->
                StockMovementItem stockMovementItem = StockMovementItem.createFromOrderItem(orderItem)
                stockMovementItem.sortOrder = stockMovement.lineItems ? stockMovement.lineItems.size() * 100 : 0
                stockMovement.lineItems.add(stockMovementItem)
            }
        }

        return stockMovement
    }

    static Map buildCsvRow(StockMovementItem lineItem = null) {
        return [
                "Requisition item id"               : lineItem?.id ?: "",
                "Product code (required)"           : lineItem?.product?.productCode ?: "",
                "Product name"                      : lineItem?.product?.displayNameWithLocaleCode ?: "",
                "Pack level 1"                      : lineItem?.palletName ?: "",
                "Pack level 2"                      : lineItem?.boxName ?: "",
                "Lot number"                        : lineItem?.lotNumber ?: "",
                "Expiration date (MM/dd/yyyy)"      : lineItem?.expirationDate ? lineItem?.expirationDate?.format("MM/dd/yyyy") : "",
                "Quantity (required)"               : lineItem?.quantityRequested ?: "",
                "Recipient id"                      : lineItem?.recipient?.id ?: ""
        ]
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
        Enum status = StockMovementStatusResolver.getListStatus(stockMovementContext)
        return StockMovementStatusResolver.getStatusMetaData(status)
    }

    Boolean canUserRollbackApproval(User user) {
        // Approval can be rolled back by user who is approver or requestor
        return approvers?.contains(user) || requestedBy?.id == user?.id
    }

    Boolean isInApprovalState() {
        return requisition?.status in [RequisitionStatus.APPROVED, RequisitionStatus.REJECTED]
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

    // Function for checking if user in exact location can edit request
    // (with required approval)
    Boolean canUserEdit(String userId, Location location) {
        User user = User.get(userId)
        Boolean isUserRequestor = user.id == requestedBy?.id
        Boolean isLocationOrigin = origin?.id == location?.id
        Boolean isLocationDestination = destination?.id == location?.id
        return (isUserRequestor &&
                requisition?.status == RequisitionStatus.PENDING_APPROVAL &&
                (isLocationDestination || isLocationOrigin)) ||
                (requisition?.status == RequisitionStatus.APPROVED && isLocationOrigin)
    }
}
