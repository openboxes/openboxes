package org.pih.warehouse.api


import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.putaway.PutawayTask

/**
 * Adapter class responsible for mapping putaway task data to order item entities.
 *
 * <p>This adapter handles the transformation between putaway task POJOs (which represent
 * warehouse fulfillment operations) and order item domain objects (which represent
 * customer order line items). The mapping includes state translations, property conversions,
 * and business rule applications specific to the putaway-to-order relationship.
 *
 */
class PutawayTaskAdapter {

    /**
     * Copies putaway task properties onto an existing order item. Primarily used to build the order item state to be
     * persisted. Please note that any changes to the putaway task have likely already been validated so this method
     * does not need to be concerned with whether or not a property is valid.
     *
     * TODO With the current version of the API, we create putaway tasks through the existing putaway order feature.
     *  This means we always start with an existing order item. This order item is retrieved as an association using
     *  the putaway task view. Therefore, the property mapping for existing putaway tasks is handled by the putaway
     *  task view. If we wanted to create a new putaway task from scratch we would need a separate method without the
     *  order item and we'd want to map all of the properties including the order object based on the putaway task
     *  properties.
     *
     * @param task the putaway task being executed
     * @param orderItem the existing order item to copy all change putaway task changes
     * @return
     */
    static OrderItem toOrderItem(PutawayTask task, OrderItem orderItem) {
        orderItem.orderItemStatusCode = toOrderItemStatusCode(task.status)

        // assign - putaway task has been loaded into putaway container (change if user provided override)
        orderItem.containerLocation = task.container

        orderItem.destinationBinLocation = task.destination

        orderItem.reasonCode = task.reasonCode

        // In order to update timestamp on putaway task (order.lastUpdated should be updated on its own)
        orderItem.lastUpdated = new Date()

        return orderItem
    }

    static PutawayTaskStatus toPutawayTaskStatus(OrderStatus orderStatus) {
        switch (orderStatus) {
            case OrderStatus.PENDING: return PutawayTaskStatus.PENDING
            case OrderStatus.APPROVED: return PutawayTaskStatus.IN_PROGRESS
            case OrderStatus.PLACED: return PutawayTaskStatus.IN_TRANSIT
            case OrderStatus.COMPLETED: return PutawayTaskStatus.COMPLETED
            case OrderStatus.CANCELED: return PutawayTaskStatus.CANCELED
            default: return PutawayTaskStatus.PENDING
        }
    }

    static OrderStatus toOrderStatus(PutawayTaskStatus putawayTaskStatus) {
        switch (putawayTaskStatus) {
            case PutawayTaskStatus.PENDING: return OrderStatus.PENDING
            case PutawayTaskStatus.IN_PROGRESS: return OrderStatus.APPROVED
            case PutawayTaskStatus.IN_TRANSIT: return OrderStatus.PLACED
            case PutawayTaskStatus.COMPLETED: return OrderStatus.COMPLETED
            case PutawayTaskStatus.CANCELED: return OrderStatus.CANCELED
            default: return OrderStatus.PENDING
        }
    }

    static OrderItemStatusCode toOrderItemStatusCode(PutawayTaskStatus putawayTaskStatus) {
        switch (putawayTaskStatus) {
            case PutawayTaskStatus.IN_TRANSIT: return OrderItemStatusCode.IN_TRANSIT
            case PutawayTaskStatus.COMPLETED: return OrderItemStatusCode.COMPLETED
            case PutawayTaskStatus.CANCELED: return OrderItemStatusCode.CANCELED
            default: return OrderItemStatusCode.PENDING
        }
    }

    static PutawayTask toPutawayTask(OrderItem orderItem) {
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
            case OrderItemStatusCode.PENDING: return PutawayTaskStatus.PENDING
            case OrderItemStatusCode.IN_PROGRESS: return PutawayTaskStatus.IN_PROGRESS
            case OrderItemStatusCode.IN_TRANSIT: return PutawayTaskStatus.IN_TRANSIT
            case OrderItemStatusCode.COMPLETED: return PutawayTaskStatus.COMPLETED
            case OrderItemStatusCode.CANCELED: return PutawayTaskStatus.CANCELED
            default: return PutawayTaskStatus.PENDING
        }
    }
}
