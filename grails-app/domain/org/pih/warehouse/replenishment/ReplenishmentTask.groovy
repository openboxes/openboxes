package org.pih.warehouse.replenishment

import org.pih.warehouse.api.PickTaskStatus
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.product.Product

class ReplenishmentTask {

    String id
    String identifier

    Order order
    String orderNumber
    OrderStatus orderStatus
    OrderType orderType
    Location facility
    Date dateRequested
    User requestedBy

    OrderItem orderItem
    Product product

    Location binLocation
    Location outboundContainer
    Location stagingLocation
    InventoryItem inventoryItem
    BigDecimal quantityRequired = 0
    BigDecimal quantityPicked = 0
    Person assignee
    Date dateAssigned
    Date dateStarted
    Person pickedBy
    Date datePicked
    Person stagedBy
    Date dateStaged
    String reasonCode
    PickTaskStatus status = PickTaskStatus.PENDING

    Date dateCreated
    Date lastUpdated

    static constraints = {
        order nullable: false
        orderNumber nullable: true
        orderStatus nullable: true
        orderType nullable: true
        facility nullable: true
        dateRequested nullable: true
        facility nullable: true
        orderItem nullable: true
        product nullable: false
        binLocation nullable: true
        outboundContainer nullable: true
        stagingLocation nullable: true
        inventoryItem nullable: false
        quantityRequired nullable: false
        quantityPicked nullable: true
        assignee nullable: true
        dateAssigned nullable: true
        dateStarted nullable: true
        pickedBy nullable: true
        datePicked nullable: true
        stagedBy nullable: true
        dateStaged nullable: true
        reasonCode nullable: true
        status nullable: false
    }

    static mapping = {
        table 'replenishment_task'
        version false
    }

    Map toJson() {
        [
                id              : id,
                identifier      : identifier,
                orderId         : order?.id,
                orderItemId     : orderItem?.id,
                orderNumber     : orderNumber,
                orderStatus     : orderStatus,
                origin          : origin?.toBaseJson(),
                destination     : destination?.toBaseJson(),
                dateRequested   : dateRequested,
                facility        : facility?.toBaseJson(),
                product         : product?.toJson(),
                location        : location?.toJson(location?.locationType?.locationTypeCode),
                outboundContainer: outboundContainer?.toJson(location?.locationType?.locationTypeCode),
                stagingLocation : stagingLocation?.toJson(location?.locationType?.locationTypeCode),
                inventoryItem   : inventoryItem?.toJson(),
                quantityRequired: quantityRequired,
                quantityPicked  : quantityPicked,
                assignee        : assignee,
                dateAssigned    : dateAssigned,
                dateStarted     : dateStarted,
                pickedBy        : pickedBy,
                datePicked      : datePicked,
                stagedBy        : stagedBy,
                dateStaged      : dateStaged,
                reasonCode      : reasonCode,
                status          : status?.name(),
                dateCreated     : dateCreated,
                lastUpdated     : lastUpdated,
        ]
    }
}
