package org.pih.warehouse.putaway

import org.pih.warehouse.api.Putaway
import org.pih.warehouse.api.PutawayTaskStatus
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.Product

class PutawayTask {

    String id

    // Inventory item
    Product product
    InventoryItem inventoryItem
    BigDecimal quantity = 0

    // Status: PENDING, IN_PROGRESS, IN_TRANSIT, COMPLETED, CANCELED
    PutawayTaskStatus status = PutawayTaskStatus.PENDING


    Order putawayOrder
    OrderItem putawayOrderItem

    // Putaway instructions
    Location facility
    Location location                   // current location (e.g., inbound staging)
    Location container                  // container scanned during putaway
    Location destination                // target storage or outbound staging

    Date dateCreated
    Date lastUpdated

    static constraints = {
        product nullable: true
        quantity nullable: false
        status nullable: false //, inList: PutawayTaskStatus.list()
        container nullable: true
        location nullable: false
        destination nullable: false
    }

    static mapping = {
        table 'putaway_task'
        version false
        container updateable: false

    }

    // In order to persist the task, we need to convert the task into the putaway order and update fields on the putaway
    Putaway toPutaway() {
        Putaway putaway = Putaway.createFromOrder(putawayOrder)
        putaway.putawayAssignee = putawayOrder.orderedBy
        putaway.putawayStatus = status.toPutawayStatus()
        return putaway
    }

    Map toJson() {
        return [
                id           : id,
                status       : status?.name(),
                inventoryItem: inventoryItem,
                facility     : facility?.toBaseJson(),
                location     : location.toJson(location?.locationType?.locationTypeCode),
                container    : container?.toJson(container?.locationType?.locationTypeCode),
                destination  : destination?.toJson(destination?.locationType?.locationTypeCode),
                quantity     : quantity,
                // FIXME for debugging purposes only
                putaway      : toPutaway()

        ]
    }
}