package org.pih.warehouse.putaway

import org.pih.warehouse.api.Putaway
import org.pih.warehouse.api.PutawayTaskStatus
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.product.Product

class PutawayTask {


    String id

    // Inventory item to be putaway
    Product product
    InventoryItem inventoryItem
    Location location                   // current location (e.g., inbound staging)
    BigDecimal quantity = 0

    // Putaway Status: PENDING, IN_PROGRESS, IN_TRANSIT, COMPLETED, CANCELED
    PutawayTaskStatus status = PutawayTaskStatus.PENDING
    Date dateStarted
    Date dateCanceled
    Date dateCompleted

    // Associations
    Order putawayOrder
    OrderItem putawayOrderItem
    OrderStatus putawayOrderStatus
    OrderItemStatusCode putawayOrderItemStatus

    // Putaway details
    String identifier
    Person assignee
    Person completedBy
    Person orderedBy
    Location facility
    Location container                  // container scanned during putaway
    Location destination                // target storage or outbound staging
    ReasonCode reasonCode

    // Auditing fields
    Date dateCreated
    Date lastUpdated

    static constraints = {
        status nullable: false //, inList: PutawayTaskStatus.list()
        product nullable: false
        inventoryItem nullable: false
        location nullable: false
        quantity nullable: false
        container nullable: true
        destination nullable: true // should be nullable: false, unless we might want to support an open putaway
        dateStarted nullable: true
        dateCanceled nullable: true
        dateCompleted nullable: true
        orderedBy nullable: true
        assignee nullable: true
        completedBy nullable: true
        putawayOrder nullable: true
        putawayOrderItem nullable: true
        reasonCode nullable: true
    }

    static mapping = {
        table 'putaway_task'
        // FIXME uncomment if/when we move to a materialized putaway task
        //id generator: "uuid"
        // FIXME comment if/when we move to a materialized putaway task
        version false // Important: Disable optimistic locking for views
    }

    Map toJson() {
        return [
                id           : id,
                status       : status?.name(),
                identifier   : identifier,
                inventoryItem: inventoryItem,
                facility     : facility?.toBaseJson(),
                location     : location.toJson(location?.locationType?.locationTypeCode),
                quantity     : quantity,
                container    : container?.toJson(container?.locationType?.locationTypeCode),
                destination  : destination?.toJson(destination?.locationType?.locationTypeCode),
                assignee     : assignee,
                orderedBy    : orderedBy,
                dateStarted  : dateStarted,
                dateCompleted: dateCompleted,
                dateCanceled : dateCanceled,
                lastUpdated  : lastUpdated,
                dateCreated  : dateCreated,
        ]
    }

    static PutawayTask createFromOrderItem(OrderItem orderItem) {
        if (!orderItem) {
            return null
        }

        Order order = orderItem.order
        PutawayTask task = new PutawayTask()

        task.id = orderItem.id
        task.product = orderItem.product
        task.inventoryItem = orderItem.inventoryItem
        task.location = orderItem.originBinLocation
        task.quantity = orderItem.quantity
        task.destination = orderItem.destinationBinLocation
        task.status = convertFromOrderItemStatus(orderItem.orderItemStatusCode)
        task.putawayOrderItemStatus = orderItem.orderItemStatusCode
        task.facility = order.origin
        task.identifier = order.orderNumber
        task.assignee = order.orderedBy
        task.completedBy = order.completedBy
        task.orderedBy = order.orderedBy
        task.putawayOrder = order
        task.putawayOrderStatus = order.status
        task.dateStarted = order.dateOrdered
        task.dateCompleted = order.dateCompleted
        task.dateCanceled = (orderItem.orderItemStatusCode == OrderItemStatusCode.CANCELED) ? orderItem.lastUpdated : null
        task.dateCreated = orderItem.dateCreated
        task.lastUpdated = orderItem.lastUpdated
        task.putawayOrderItem = orderItem

        return task
    }

    static PutawayTaskStatus convertFromOrderItemStatus(OrderItemStatusCode orderItemStatusCode) {
        if (!orderItemStatusCode) {
            return PutawayTaskStatus.PENDING
        }
        switch (orderItemStatusCode) {
            case OrderItemStatusCode.PENDING:
                return PutawayTaskStatus.PENDING
            case OrderItemStatusCode.COMPLETED:
                return PutawayTaskStatus.COMPLETED
            case OrderItemStatusCode.CANCELED:
                return PutawayTaskStatus.CANCELED
            default:
                return PutawayTaskStatus.PENDING
        }
    }
}