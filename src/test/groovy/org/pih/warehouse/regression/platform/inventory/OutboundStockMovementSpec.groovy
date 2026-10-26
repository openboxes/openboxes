package org.pih.warehouse.regression.platform.inventory

import spock.lang.PendingFeature

import org.pih.warehouse.inventory.*
import grails.testing.gorm.DomainUnitTest
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.api.StockMovementType
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.Role
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.ReplenishmentTypeCode
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionItemType
import org.pih.warehouse.requisition.RequisitionSourceType
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentStatusCode
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class OutboundStockMovementSpec extends Specification implements DomainUnitTest<OutboundStockMovement> {

    private static final TimeZone ZONE = TimeZone.getTimeZone('Asia/Kolkata')

    TimeZone originalTimeZone

    Class[] getDomainClassesToMock() {
        return [OutboundStockMovement, User, Role] as Class[]
    }

    void setup() {
        originalTimeZone = TimeZone.default
        TimeZone.default = ZONE
    }

    void cleanup() {
        TimeZone.default = originalTimeZone
    }

    void 'shipment status predicates should follow the shipment status #status'() {
        given:
        domain.shipment = new Shipment(currentStatus: status)

        expect:
        domain.isPending() == pending
        domain.hasBeenShipped() == shipped
        domain.hasBeenPartiallyReceived() == partiallyReceived
        domain.hasBeenReceived() == received

        where:
        status                                || pending | shipped | partiallyReceived | received
        ShipmentStatusCode.CREATED            || false   | false   | false             | false
        ShipmentStatusCode.PENDING            || true    | false   | false             | false
        ShipmentStatusCode.SHIPPED            || false   | true    | false             | false
        ShipmentStatusCode.PARTIALLY_RECEIVED || false   | false   | true              | false
        ShipmentStatusCode.RECEIVED           || false   | false   | false             | true
    }

    void 'shipment status predicates should all be false without a shipment'() {
        expect:
        !domain.isPending()
        !domain.hasBeenShipped()
        !domain.hasBeenPartiallyReceived()
        !domain.hasBeenReceived()
        !domain.hasBeenIssued()
        !domain.isPendingApproval()
        !domain.isInApprovalState()
        !domain.isApprovalRequired()
        !domain.isReturn
    }

    void 'requisition status predicates should follow the requisition status #status'() {
        given:
        domain.requisition = new Requisition(status: status)

        expect:
        domain.hasBeenIssued() == issued
        domain.isPendingApproval() == pendingApproval
        domain.isInApprovalState() == inApprovalState

        where:
        status                             || issued | pendingApproval | inApprovalState
        RequisitionStatus.CREATED          || false  | false           | false
        RequisitionStatus.PENDING_APPROVAL || false  | true            | false
        RequisitionStatus.APPROVED         || false  | false           | true
        RequisitionStatus.REJECTED         || false  | false           | true
        RequisitionStatus.ISSUED           || true   | false           | false
    }

    void 'isElectronicType should be #electronic for source type #sourceType'() {
        given:
        domain.sourceType = sourceType

        expect:
        domain.isElectronicType() == electronic

        where:
        sourceType                       || electronic
        RequisitionSourceType.ELECTRONIC || true
        RequisitionSourceType.PAPER      || false
        null                             || false
    }

    void 'getIsReturn should be #isReturn when the shipment is from a return order is #fromReturnOrder'() {
        given:
        domain.shipment = Stub(Shipment) {
            getIsFromReturnOrder() >> fromReturnOrder
        }

        expect:
        domain.isReturn == isReturn

        where:
        fromReturnOrder || isReturn
        true            || true
        false           || false
    }

    void 'getApprovers should list the approvers of the requisition'() {
        given:
        Person approver = new Person(firstName: 'Ann', lastName: 'Lee')
        domain.requisition = new Requisition()
        domain.requisition.approvers = [approver] as Set

        expect:
        domain.approvers == [approver]
    }

    void 'getTotalValue should sum quantity times unit price for the priced shipment items'() {
        given:
        domain.shipment = Stub(Shipment) {
            getShipmentItems() >> ([
                    new ShipmentItem(quantity: 2, product: new Product(pricePerUnit: 1.5)),
                    new ShipmentItem(quantity: 3, product: new Product(pricePerUnit: 2)),
                    new ShipmentItem(quantity: 50, product: new Product(pricePerUnit: null)),
            ] as Set)
        }

        expect:
        domain.totalValue == 9.0f
    }

    void 'getTotalValue should be zero without a shipment'() {
        expect:
        domain.totalValue == 0
    }

    void 'isApprovalRequired should be #required when the requisition requires approval is #requisitionRequires, the origin supports approval is #originSupports and the status is #status'() {
        given:
        domain.origin = location('origin', LocationTypeCode.DEPOT, originSupports ? [ActivityCode.APPROVE_REQUEST] : [])
        domain.requisition = new Requisition(approvalRequired: requisitionRequires, status: status)

        expect:
        domain.isApprovalRequired() == required

        where:
        requisitionRequires | originSupports | status                             || required
        true                | true           | RequisitionStatus.PENDING_APPROVAL || true
        true                | true           | RequisitionStatus.VERIFYING        || true
        true                | true           | RequisitionStatus.PICKING          || false
        true                | true           | RequisitionStatus.ISSUED           || false
        false               | true           | RequisitionStatus.VERIFYING        || false
        true                | false          | RequisitionStatus.VERIFYING        || false
    }

    void 'isDeleteOrRollbackAuthorized should be #authorized at the #at when the origin manages inventory is #managesInventory'() {
        given:
        Location origin = location('origin', LocationTypeCode.DEPOT, managesInventory ? [ActivityCode.MANAGE_INVENTORY] : [])
        Location destination = location('destination', LocationTypeCode.WARD)
        Location elsewhere = location('elsewhere', LocationTypeCode.DEPOT, centralPurchasing ? [ActivityCode.ENABLE_CENTRAL_PURCHASING] : [])
        domain.requisition = new Requisition(origin: origin, destination: destination)
        domain.sourceType = electronic ? RequisitionSourceType.ELECTRONIC : RequisitionSourceType.PAPER
        domain.isFromOrder = fromOrder
        Location currentLocation = [origin: origin, destination: destination, elsewhere: elsewhere][at]

        expect:
        domain.isDeleteOrRollbackAuthorized(currentLocation) == authorized

        where:
        at            | managesInventory | electronic | fromOrder | centralPurchasing || authorized
        'origin'      | true             | false      | false     | false             || true
        'destination' | true             | false      | false     | false             || false
        'destination' | false            | false      | false     | false             || true
        'origin'      | false            | false      | false     | false             || false
        'destination' | true             | true       | false     | false             || true
        'elsewhere'   | true             | true       | false     | false             || false
        'elsewhere'   | true             | false      | true      | true              || true
        'elsewhere'   | true             | false      | true      | false             || false
        'elsewhere'   | true             | false      | false     | true              || false
    }

    void 'isDeleteOrRollbackAuthorized should fall back to the shipment locations without a requisition'() {
        given:
        Location origin = location('origin', LocationTypeCode.DEPOT, [ActivityCode.MANAGE_INVENTORY])
        Location destination = location('destination', LocationTypeCode.WARD)
        domain.shipment = new Shipment(origin: origin, destination: destination)

        expect:
        domain.isDeleteOrRollbackAuthorized(origin)
        !domain.isDeleteOrRollbackAuthorized(destination)
    }

    void 'isEditAuthorized should be #authorized at the #at for a #shipmentStatus shipment from a #originType'() {
        given:
        Location origin = location('origin', originType)
        Location destination = location('destination', LocationTypeCode.WARD)
        Location elsewhere = location('elsewhere', LocationTypeCode.DEPOT, centralPurchasing ? [ActivityCode.ENABLE_CENTRAL_PURCHASING] : [])
        domain.origin = origin
        domain.destination = destination
        domain.shipment = new Shipment(currentStatus: shipmentStatus)
        domain.sourceType = electronic ? RequisitionSourceType.ELECTRONIC : null
        domain.isFromOrder = fromOrder
        Location currentLocation = [origin: origin, destination: destination, elsewhere: elsewhere][at]

        expect:
        domain.isEditAuthorized(currentLocation) == authorized

        where:
        at            | originType             | shipmentStatus                        | electronic | fromOrder | centralPurchasing || authorized
        'origin'      | LocationTypeCode.DEPOT | ShipmentStatusCode.PENDING            | false      | false     | false             || true
        'destination' | LocationTypeCode.WARD  | ShipmentStatusCode.PENDING            | false      | false     | false             || true
        'destination' | LocationTypeCode.DEPOT | ShipmentStatusCode.PENDING            | false      | false     | false             || false
        'elsewhere'   | LocationTypeCode.DEPOT | ShipmentStatusCode.PENDING            | false      | false     | false             || false
        'elsewhere'   | LocationTypeCode.DEPOT | ShipmentStatusCode.PENDING            | true       | false     | false             || true
        'elsewhere'   | LocationTypeCode.DEPOT | ShipmentStatusCode.PENDING            | false      | true      | true              || true
        'elsewhere'   | LocationTypeCode.DEPOT | ShipmentStatusCode.PENDING            | false      | true      | false             || false
        'elsewhere'   | LocationTypeCode.DEPOT | ShipmentStatusCode.SHIPPED            | false      | false     | false             || true
        'origin'      | LocationTypeCode.DEPOT | ShipmentStatusCode.PARTIALLY_RECEIVED | false      | false     | false             || false
        'origin'      | LocationTypeCode.DEPOT | ShipmentStatusCode.RECEIVED           | false      | false     | false             || false
        'elsewhere'   | LocationTypeCode.DEPOT | ShipmentStatusCode.RECEIVED           | true       | false     | false             || false
    }

    void 'isReceivingAuthorized should be #authorized at the #at with requisition #requisitionStatus and shipment #shipmentStatus'() {
        given:
        Location origin = location('origin', LocationTypeCode.DEPOT)
        Location destination = location('destination', LocationTypeCode.WARD)
        domain.origin = origin
        domain.destination = destination
        domain.requisition = new Requisition(status: requisitionStatus)
        domain.shipment = new Shipment(currentStatus: shipmentStatus)
        Location currentLocation = [origin: origin, destination: destination][at]

        expect:
        domain.isReceivingAuthorized(currentLocation) == authorized

        where:
        at            | requisitionStatus          | shipmentStatus                        || authorized
        'destination' | RequisitionStatus.ISSUED   | ShipmentStatusCode.PENDING            || true
        'destination' | RequisitionStatus.PICKING  | ShipmentStatusCode.SHIPPED            || true
        'destination' | RequisitionStatus.PICKING  | ShipmentStatusCode.PARTIALLY_RECEIVED || true
        'destination' | RequisitionStatus.ISSUED   | ShipmentStatusCode.RECEIVED           || false
        'destination' | RequisitionStatus.PICKING  | ShipmentStatusCode.PENDING            || false
        'origin'      | RequisitionStatus.ISSUED   | ShipmentStatusCode.SHIPPED            || false
    }

    void 'canUserEdit should be #canEdit for the #who with status #status at the #at'() {
        given:
        User requestor = saveUser('requestor')
        User other = saveUser('other')
        Location origin = location('origin', LocationTypeCode.DEPOT)
        Location destination = location('destination', LocationTypeCode.WARD)
        Location elsewhere = location('elsewhere', LocationTypeCode.DEPOT)
        domain.requestedBy = requestor
        domain.origin = origin
        domain.destination = destination
        domain.status = status
        String userId = [requestor: requestor, other: other][who].id
        Location location = [origin: origin, destination: destination, elsewhere: elsewhere][at]

        expect:
        domain.canUserEdit(userId, location) == canEdit

        where:
        who         | status                             | at            || canEdit
        'requestor' | RequisitionStatus.PENDING_APPROVAL | 'destination' || true
        'requestor' | RequisitionStatus.PENDING_APPROVAL | 'origin'      || true
        'requestor' | RequisitionStatus.PENDING_APPROVAL | 'elsewhere'   || false
        'other'     | RequisitionStatus.PENDING_APPROVAL | 'destination' || false
        'other'     | RequisitionStatus.APPROVED         | 'origin'      || true
        'requestor' | RequisitionStatus.APPROVED         | 'destination' || false
        'requestor' | RequisitionStatus.PICKING          | 'origin'      || false
    }

    void 'canRollbackApproval should be #canRollback for a #who with requisition status #status'() {
        given:
        Location location = location('origin', LocationTypeCode.DEPOT)
        User requestor = saveUser('requestor')
        User user = who == 'requestor' ? requestor : saveUser('user', roleType)
        domain.requestedBy = requestor
        domain.requisition = new Requisition(status: status)

        expect:
        domain.canRollbackApproval(user.id, location) == canRollback

        where:
        who         | roleType                           | status                     || canRollback
        'requestor' | null                               | RequisitionStatus.APPROVED || true
        'approver'  | RoleType.ROLE_REQUISITION_APPROVER | RequisitionStatus.APPROVED || true
        'admin'     | RoleType.ROLE_ADMIN                | RequisitionStatus.REJECTED || true
        'superuser' | RoleType.ROLE_SUPERUSER            | RequisitionStatus.APPROVED || true
        'manager'   | RoleType.ROLE_MANAGER              | RequisitionStatus.APPROVED || false
        'admin'     | RoleType.ROLE_ADMIN                | RequisitionStatus.PICKING  || false
        'requestor' | null                               | RequisitionStatus.ISSUED   || false
    }

    void 'getLineItems should build line items from the original requisition items in sort order'() {
        given:
        Requisition requisition = new Requisition()
        RequisitionItem second = requisitionItem('ri-2', requisition, 2, 20)
        RequisitionItem first = requisitionItem('ri-1', requisition, 1, 10)
        RequisitionItem change = requisitionItem('ri-3', requisition, 1, 5)
        change.requisitionItemType = RequisitionItemType.QUANTITY_CHANGE
        change.parentRequisitionItem = first
        requisition.requisitionItems = [second, change, first] as Set
        domain.requisition = requisition

        when:
        List<StockMovementItem> lineItems = domain.lineItems

        then:
        lineItems*.id == ['ri-1', 'ri-2']
        lineItems*.quantityRequested == [10, 20]
        lineItems*.sortOrder == [1, 2]
        lineItems*.requisitionItem == [first, second]
    }

    void 'getLineItems should build line items from the open order items of an order'() {
        given:
        Order order = new Order(orderNumber: 'PO-1')
        order.orderItems = new LinkedHashSet([
                orderItem('oi-1', order, OrderItemStatusCode.PENDING, 4),
                orderItem('oi-2', order, OrderItemStatusCode.CANCELED, 4),
                orderItem('oi-3', order, OrderItemStatusCode.PENDING, 0),
                orderItem('oi-4', order, OrderItemStatusCode.PENDING, 6),
        ])
        domain.order = order
        domain.requisition = new Requisition()
        domain.requisition.requisitionItems = [requisitionItem('ri-1', domain.requisition, 0, 1)] as Set

        when:
        List<StockMovementItem> lineItems = domain.lineItems

        then:
        lineItems*.id == ['oi-1', 'oi-4']
        lineItems*.orderItemId == ['oi-1', 'oi-4']
        lineItems*.quantityRequested == [8, 12]
        lineItems*.orderNumber == ['PO-1', 'PO-1']
        lineItems*.sortOrder == [0, 100]
    }

    void 'getLineItems should build line items from the shipment items and keep their sort order when they have one'() {
        given:
        ShipmentItem unsorted1 = shipmentItem('si-1', null, 3)
        ShipmentItem sorted = shipmentItem('si-2', 7, 4)
        ShipmentItem unsorted2 = shipmentItem('si-3', null, 5)
        domain.shipment = Stub(Shipment) {
            getShipmentItems() >> new LinkedHashSet([unsorted1, sorted, unsorted2])
        }

        when:
        List<StockMovementItem> lineItems = domain.lineItems

        then:
        lineItems*.id == ['si-1', 'si-2', 'si-3']
        lineItems*.quantityRequested == [3, 4, 5]
        lineItems*.sortOrder == [0, 7, 200]
    }

    void 'getLineItems should be empty without an order, requisition or shipment'() {
        expect:
        domain.lineItems == []
    }

    void 'getLineItems should keep line items that were already set'() {
        given:
        StockMovementItem existing = new StockMovementItem(id: 'existing')
        domain.lineItems = [existing]
        domain.requisition = new Requisition()
        domain.requisition.requisitionItems = [requisitionItem('ri-1', domain.requisition, 0, 1)] as Set

        expect:
        domain.lineItems == [existing]
    }

    void 'toJson should describe the stock movement with dates formatted in the default time zone'() {
        given:
        Location origin = location('origin', LocationTypeCode.DEPOT, [ActivityCode.MANAGE_INVENTORY])
        Location destination = location('destination', LocationTypeCode.WARD)
        Person requestor = new Person(firstName: 'Ann', lastName: 'Lee')
        Requisition stocklist = new Requisition(name: 'Ward stock', replenishmentTypeCode: ReplenishmentTypeCode.PULL)
        stocklist.id = 'stocklist-1'
        Shipment shipped = new Shipment(shipmentNumber: 'S-1', currentStatus: ShipmentStatusCode.SHIPPED)
        shipped.id = 'shipment-1'
        Requisition requisition = new Requisition(requestNumber: 'R-1', status: RequisitionStatus.ISSUED)
        requisition.shipments = [shipped] as Set
        requisition.id = 'requisition-1'
        List<StockMovementItem> lineItems = [new StockMovementItem(id: 'line-1')]

        domain.id = 'sm-1'
        domain.name = 'Ward request'
        domain.description = 'weekly'
        domain.identifier = 'R-1'
        domain.stockMovementType = StockMovementType.STOCK_MOVEMENT
        domain.statusCode = StockMovementStatusCode.DISPATCHED
        domain.origin = origin
        domain.destination = destination
        domain.stocklist = stocklist
        domain.requestedBy = requestor
        domain.dateRequested = zoned(2024, Calendar.MARCH, 5, 23, 30)
        domain.dateShipped = zoned(2024, Calendar.MARCH, 6, 9, 15)
        domain.expectedDeliveryDate = zoned(2024, Calendar.MARCH, 8, 17, 45)
        domain.shipmentStatus = ShipmentStatusCode.SHIPPED
        domain.trackingNumber = 'TRK'
        domain.driverName = 'Driver'
        domain.comments = 'Fragile'
        domain.lineItems = lineItems
        domain.lineItemCount = 1
        domain.requisition = requisition
        domain.shipment = shipped
        domain.documents = ['doc']
        domain.isShipped = true

        when:
        Map json = domain.toJson()

        then:
        json.id == 'sm-1'
        json.name == 'Ward request'
        json.description == 'weekly'
        json.identifier == 'R-1'
        json.stockMovementType == StockMovementType.STOCK_MOVEMENT
        json.statusCode == 'DISPATCHED'
        json.origin.is(origin)
        json.destination.is(destination)
        json.hasManageInventory == true
        json.stocklist == [id: 'stocklist-1', name: 'Ward stock']
        json.replenishmentType == ReplenishmentTypeCode.PULL
        json.dateRequested == '03/05/2024'
        json.dateShipped == '03/06/2024 09:15 +05:30'
        json.expectedDeliveryDate == '03/08/2024 17:45 +05:30'
        json.shipmentStatus == 'SHIPPED'
        json.trackingNumber == 'TRK'
        json.driverName == 'Driver'
        json.comments == 'Fragile'
        json.requestedBy.is(requestor)
        json.lineItems.is(lineItems)
        json.lineItemCount == 1
        json.associations.requisition == [id: 'requisition-1', requestNumber: 'R-1', status: 'ISSUED']
        json.associations.shipment == [id: 'shipment-1', shipmentNumber: 'S-1', status: 'SHIPPED']
        json.associations.shipments == [[id: 'shipment-1', shipmentNumber: 'S-1', status: 'SHIPPED']]
        json.associations.documents == ['doc']
        json.isFromOrder == false
        json.isShipped == true
        json.shipped == true
        json.isReceived == false
        json.received == false
        json.sourceType == null
    }

    @PendingFeature(reason = 'OBAF toJson reads sourceType?.name, a property the enum lacks (MissingPropertyException)')
    void 'toJson should name the source type: #sourceType'() {
        given:
        domain.origin = location('origin', LocationTypeCode.WARD)
        domain.sourceType = sourceType

        expect:
        domain.toJson().sourceType == expected

        where:
        sourceType                       || expected
        RequisitionSourceType.ELECTRONIC || 'ELECTRONIC'
        RequisitionSourceType.PAPER      || 'PAPER'
        null                             || null
    }

    void 'toJson should leave the optional associations empty'() {
        given:
        domain.origin = location('origin', LocationTypeCode.WARD)

        when:
        Map json = domain.toJson()

        then:
        !json.hasManageInventory
        json.stocklist == [id: null, name: null]
        json.dateRequested == null
        json.dateShipped == null
        json.associations.requisition == [id: null, requestNumber: null, status: null]
        json.associations.shipment == [id: null, shipmentNumber: null, status: null]
        json.associations.shipments == null
    }

    void 'getReferenceDocument should be null because a stock movement is not referenceable itself'() {
        expect:
        domain.referenceDocument == null
    }

    private static Date zoned(int year, int month, int day, int hour, int minute) {
        Calendar calendar = Calendar.getInstance(ZONE)
        calendar.clear()
        calendar.set(year, month, day, hour, minute, 0)
        return calendar.time
    }

    private static Location location(String id, LocationTypeCode locationTypeCode, List<ActivityCode> activities = []) {
        Location location = new Location(name: id, locationType: new LocationType(locationTypeCode: locationTypeCode))
        location.id = id
        location.supportedActivities = activities*.id as Set
        return location
    }

    private static User saveUser(String username, RoleType roleType = null) {
        User user = new User(username: username, firstName: username, lastName: 'User')
        if (roleType) {
            Role role = new Role(roleType: roleType)
            role.save(validate: false, flush: true)
            user.roles = [role] as Set
        }
        user.save(validate: false, flush: true)
        return user
    }

    private static RequisitionItem requisitionItem(String id, Requisition requisition, Integer orderIndex, Integer quantity) {
        RequisitionItem item = new RequisitionItem(orderIndex: orderIndex, quantity: quantity)
        item.id = id
        item.requisition = requisition
        return item
    }

    private OrderItem orderItem(String id, Order order, OrderItemStatusCode statusCode, Integer quantityRemaining) {
        return Stub(OrderItem) {
            getId() >> id
            getOrder() >> order
            getOrderItemStatusCode() >> statusCode
            getQuantityRemainingToShip() >> quantityRemaining
            getQuantityPerUom() >> 2
        }
    }

    private ShipmentItem shipmentItem(String id, Integer sortOrder, Integer quantity) {
        return Stub(ShipmentItem) {
            getId() >> id
            getSortOrder() >> sortOrder
            getQuantity() >> quantity
        }
    }
}
