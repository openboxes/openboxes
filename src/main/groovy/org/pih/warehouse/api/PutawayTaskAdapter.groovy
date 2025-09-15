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
        // In most cases, we just want to make sure we're setting the most recent status
        orderItem.orderItemStatusCode = toOrderItemStatusCode(task.status)

        // assign - putaway task has been loaded into putaway container (change if user provided override)
        orderItem.containerLocation = task.container

        orderItem.destinationBinLocation = task.destination

        // In order to update timestamp on putaway task (order.lastUpdated should be updated on its own)
        orderItem.lastUpdated = new Date()

        return orderItem
    }


    /**
     * Takes a putaway order and converts to a putaway task. This method expects there to be only one putaway item
     * on the order item, so putaways that have been split may not work properly. This mostly acts as a wrapper
     * around the method that takes an order item, but with  
     *
     * @param order the putaway order to be converted
     * @return
     */
    static PutawayTask toPutawayTask(Order order) {
        // FIXME once we implement the state machine at the order item level.
        if (order.orderItems.size() != 1) {
            throw new IllegalStateException("Putaway must have only one item")
        }

        // FIXME once we implement the state machine and allow support for multiple putaway tasks we also need to
        //  handle split items
        OrderItem orderItem = order.orderItems.first()
        return toPutawayTask(orderItem)
    }

    // FIXME We might not need this because we get this data mapped through the view-backed domain. We can use this to
    //  compare the two for now to make sure the view is returning the right data.
    static PutawayTask toPutawayTask(OrderItem orderItem) {
        Order order = orderItem.order
        return new PutawayTask(
                id: orderItem.id,
                status: toPutawayTaskStatus(order.status),
                // FIXME eventually we want the status to be coming from the order item so we can support a more
                //  complex scenarios like splitting putaway tasks
                // toPutawayTaskStatus(orderItem.order.status) ?: PutawayTaskStatus.PENDING
                facility: order.destination,
                identifier: order.orderNumber,
                location: orderItem.originBinLocation,
                destination: orderItem.destinationBinLocation,
                quantity: orderItem.quantity,
                assignee: order.completedBy,
                dateCompleted: order.dateCompleted,
                dateStarted: order.dateApproved,
                dateCanceled: order.status == OrderStatus.CANCELED ? order.dateCompleted : null,
                // FIXME need to add a container to the order item data model
                container: null,

                // Debugging properties
                putawayOrderItem: orderItem,
                putawayOrder: order,
        )
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

    static OrderItemStatusCode toOrderItemStatusCode(PutawayTaskStatus putawayTaskStatus) {
        switch (putawayTaskStatus) {
            case PutawayTaskStatus.COMPLETED: return OrderItemStatusCode.COMPLETED
            case PutawayTaskStatus.CANCELED: return OrderItemStatusCode.CANCELED
            default: return OrderItemStatusCode.PENDING
        }
    }
}
