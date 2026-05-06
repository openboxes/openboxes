/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.putaway

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import org.apache.commons.beanutils.BeanUtils
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.sql.JoinType
import org.springframework.beans.factory.annotation.Value

import org.pih.warehouse.api.Putaway
import org.pih.warehouse.api.PutawayItem
import org.pih.warehouse.api.PutawayStatus
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.TransferStockCommand
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderEventManager
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.shipping.Shipment

@Transactional
class PutawayService {

    InventoryService inventoryService
    def productAvailabilityService
    GrailsApplication grailsApplication
    OrderEventManager orderEventManager

    @Value('${openboxes.receiving.createReceivingLocation.enabled}')
    Boolean dynamicReceivingBinsEnabled

    def getPutawayCandidates(Location location) {
        List binLocationEntries = productAvailabilityService.getAvailableQuantityOnHandByBinLocation(location)

        List<PutawayItem> putawayItems = binLocationEntries.inject ([], { putawayItems,  binLocationEntry ->
            if (binLocationEntry.binLocation?.supports(ActivityCode.RECEIVE_STOCK)) {
                return putawayItems + new PutawayItem(
                        putawayStatus: PutawayStatus.READY,
                        product: binLocationEntry.product,
                        inventoryItem: binLocationEntry.inventoryItem,
                        currentLocation: binLocationEntry.binLocation,
                        currentFacility: location,
                        putawayFacility: null,
                        putawayLocation: null,
                        availableItems: [],
                        quantity: binLocationEntry.quantity,
                )
            }

            return putawayItems
        })

        List<PutawayItem> pendingPutawayItems = getPendingItems(location)

        putawayItems.removeAll { PutawayItem item ->
            pendingPutawayItems.find {
                item.currentLocation?.id == it.currentLocation?.id && item.inventoryItem?.id == it.inventoryItem?.id &&
                        item.product?.id == it.product?.id
            }
        }

        putawayItems.addAll(pendingPutawayItems)

        return putawayItems.sort { a, b ->
            a.product?.category?.name <=> b.product?.category?.name ?:
                    a.product?.name <=> b.product?.name
        }
    }

    List<PutawayItem> getPendingItems(Location location) {
        List<Order> orders = Order.createCriteria().list {
            setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
            and {
                eq("origin", location)
                eq("orderType", OrderType.findByCode(Constants.PUTAWAY_ORDER))
            }
            createAlias("origin", "o")
            createAlias("picklist", "p", JoinType.LEFT_OUTER_JOIN)
            createAlias("orderItems", "oi", JoinType.LEFT_OUTER_JOIN)
            createAlias("oi.product", "oip", JoinType.LEFT_OUTER_JOIN)
            createAlias("oi.inventoryItem", "ii",JoinType.LEFT_OUTER_JOIN)
            createAlias("oi.recipient", "oir", JoinType.LEFT_OUTER_JOIN)
            createAlias("oip.category", "oipc", JoinType.LEFT_OUTER_JOIN)
            createAlias("oip.synonyms", "oips",JoinType.LEFT_OUTER_JOIN)
            createAlias("oi.originBinLocation", "oibl", JoinType.LEFT_OUTER_JOIN)
            createAlias("oi.destinationBinLocation", "oidl", JoinType.LEFT_OUTER_JOIN)
            createAlias("oi.orderItems", "oioi", JoinType.LEFT_OUTER_JOIN)
        }

        // Filter out items before creating putaways
        List<OrderItem> filteredItems = orders*.orderItems.flatten().findAll { OrderItem it ->
            !it.parentOrderItem && (
                    it.orderItemStatusCode == OrderItemStatusCode.PENDING || (
                            it.orderItemStatusCode == OrderItemStatusCode.CANCELED && it.orderItems?.any {
                                item -> item.orderItemStatusCode == OrderItemStatusCode.PENDING
                            })

            )}

        return filteredItems.collect { PutawayItem.createFromOrderItem(it) }
    }

    /**
     * Returns all putaway orders originating from the given shipment.
     */
    Collection<Order> getPutawayOrders(Shipment shipment) {
        if (!shipment) {
            return []
        }

        // Because we don't have a formal relationship between putaway order and receipt, if we are not creating
        // designated receiving/putaway bins, we can't establish a unique mapping between the receipt and its putaway.
        if (!dynamicReceivingBinsEnabled) {
            return []
        }

        // A shipment can be received multiple times into multiple receiving bins, or multiple times into the same
        // receiving bin, so check each receipt and collect to a unique set of putaways.
        Set<Order> putaways = []
        for (Receipt receipt in (shipment.receipts as SortedSet<Receipt>)) {
            putaways.addAll(getPutawayOrders(receipt))
        }
        return putaways
    }

    /**
     * Returns all putaway orders originating from the given receipt.
     */
    Collection<Order> getPutawayOrders(Receipt receipt) {
        // TODO: This is an imperfect solution. We should model this relationship between receipt and putaways more
        //       concretely in the database, likely via a OrderItem (ie putaway item) <-> ReceiptItem relationship.
        Set<Order> putawayOrders = []
        for (ReceiptItem receiptItem in receipt.receiptItems) {
            // If the item was directly received to a non-receiving bin, there won't be any putaway orders.
            Location receiptBinLocation = receiptItem.binLocation
            if (!receiptBinLocation?.supports(ActivityCode.PUTAWAY_STOCK)) {
                continue
            }

            /*
             * If a location is not using direct putaways, a receipt is made into a newly created receiving bin.
             * The putaway(s) of that receipt are putaway orders originating from that same receiving bin. Because we
             * don't have a direct relationship between a receipt and a putaway, we link the two by relying on the fact
             * that they operate on the same receiving bin location(s).
             *
             * Because we are generating new receiving bins for each receipt, we should be able to assume that the
             * receiving bin does not get used for anything other than the initial receipt and its putaways. However,
             * we don't strictly block users from using receiving bins as the origin for other stock movements, so
             * we need to make sure to specifically filter for only the orders that are putaways.
             */
            List<Order> putawaysForReceipt = OrderItem.createCriteria().listDistinct {
                projections {
                    property "order"
                }
                eq("inventoryItem", receiptItem.inventoryItem)
                eq("originBinLocation", receiptItem.binLocation)
                order {
                    orderType {
                        eq("code", Constants.PUTAWAY_ORDER)
                    }
                }
            } as List<Order>
            putawayOrders.addAll(putawaysForReceipt)
        }

        return putawayOrders
    }

    void processSplitItems(Putaway putaway) {

        putaway.putawayItems.toArray().each { PutawayItem oldPutawayItem ->

            if (oldPutawayItem.splitItems) {

                // Iterate over split items and create new putaway items for them
                // NOTE: The only fields we change from the original are the putaway bin and quantity.
                oldPutawayItem.splitItems.each { PutawayItem splitPutawayItem ->
                    PutawayItem newPutawayItem = new PutawayItem()
                    BeanUtils.copyProperties(newPutawayItem, oldPutawayItem)
                    newPutawayItem.quantity = splitPutawayItem.quantity
                    newPutawayItem.putawayFacility = splitPutawayItem.putawayFacility
                    newPutawayItem.putawayLocation = splitPutawayItem.putawayLocation
                    putaway.putawayItems.add(newPutawayItem)
                }

                // Remove the original putaway item since it was replaced with the above
                putaway.putawayItems.remove(oldPutawayItem)
            }

        }
    }


    Order completePutaway(Putaway putaway) {
        validatePutaway(putaway)

        // Save the putaway as a transfer order
        Order order = savePutaway(putaway)

        // Need to process the split items
        processSplitItems(putaway)

        putaway.putawayItems.each { PutawayItem putawayItem ->
            TransferStockCommand command = new TransferStockCommand()
            command.location = putawayItem.currentFacility
            command.binLocation = putawayItem.currentLocation
            command.inventoryItem = putawayItem.inventoryItem
            command.quantity = putawayItem.quantity
            command.otherLocation = putawayItem.putawayFacility
            command.otherBinLocation = putawayItem.putawayLocation
            command.order = order
            command.transferOut = Boolean.TRUE
            command.disableRefresh = Boolean.TRUE
            inventoryService.transferStock(command)
        }

        createPutawayEvent(order, putaway)

        grailsApplication.mainContext.publishEvent(new PutawayCompletedEvent(putaway))

        return order
    }

    /**
     * Create a putaway event and log its occurrence.
     */
    private void createPutawayEvent(Order order, Putaway putaway) {
        EventCode eventCode = !dynamicReceivingBinsEnabled || isFinalPutaway(order, putaway) ?
                EventCode.PUTAWAY :
                EventCode.PARTIALLY_PUTAWAY

        orderEventManager.createPutawayEvent(order, putaway, eventCode)
    }

    /**
     * Returns true if after the given putaway is completed, all receipts of the original inbound will have
     * been put away.
     */
    private boolean isFinalPutaway(Order order, Putaway putaway) {
        // TODO: We currently do not have a distinction between partial and final putaways. This will need to be
        //       implemented once we get a better sense of the requirements and can design a more long term solution.
        // 1) Fetch all completed receipts whose destination receiving bin is the same as the bin the putaway is originating from
        // 2) Fetch all completed putaways matching each of the receipts
        // 3) Do a diff between the quantity in the receipts and their matching putaways
        // 4) If there is no remaining quantity after factoring in this putaway, this is the final putaway
        return true
    }

    Order savePutaway(Putaway putaway) {

        Order order = Order.get(putaway.id)
        if (!order) {
            order = new Order()
        }

        OrderType orderType = OrderType.findByCode(Constants.PUTAWAY_ORDER)
        order.orderType = orderType
        order.status = OrderStatus.valueOf(putaway.putawayStatus.toString())
        if (!order.orderNumber) {
            order.orderNumber = "P-${putaway.putawayNumber}"
        }
        order.orderedBy = putaway.putawayAssignee
        order.dateOrdered = new Date()
        order.origin = putaway.origin
        order.destination = putaway.destination

        // Set auditing data on completion
        if (putaway.putawayStatus == PutawayStatus.COMPLETED) {
            order.completedBy = putaway.putawayAssignee
            order.dateCompleted = new Date()
        }

        // Generate name
        order.name = order.generateName()

        putaway.putawayItems.toArray().each { PutawayItem putawayItem ->

            OrderItem orderItem
            if (putawayItem.id) {
                orderItem = order.orderItems?.find { it.id == putawayItem.id }
            }

            if (!orderItem) {
                orderItem = new OrderItem()
                order.addToOrderItems(orderItem)
            }

            orderItem = updateOrderItem(putawayItem, orderItem)

            putawayItem.splitItems.each { PutawayItem splitItem ->
                OrderItem childOrderItem
                if (splitItem.id) {
                    childOrderItem = order.orderItems?.find { it.id == splitItem.id }
                }

                if (!childOrderItem && !splitItem.delete) {
                    childOrderItem = new OrderItem()
                    order.addToOrderItems(childOrderItem)
                }

                if (childOrderItem && splitItem.delete) {
                    orderItem.removeFromOrderItems(childOrderItem)
                    order.removeFromOrderItems(childOrderItem)

                    childOrderItem.delete()
                } else if (childOrderItem) {
                    childOrderItem = updateOrderItem(splitItem, childOrderItem)
                    childOrderItem.parentOrderItem = orderItem
                    orderItem.addToOrderItems(childOrderItem)
                }
            }
        }

        order.save(failOnError: true)
        return order
    }

    void deletePutawayItem(String id) {
        OrderItem orderItem = OrderItem.get(id)
        if (!orderItem) {
            throw new IllegalArgumentException("No putaway item found with ID ${id}")
        }

        def splitItems = orderItem.orderItems?.toArray()

        splitItems?.each { OrderItem item ->
            orderItem.removeFromOrderItems(item)
            item.order.removeFromOrderItems(item)
            item.delete()
        }

        orderItem.order.removeFromOrderItems(orderItem)
        orderItem.delete()
    }

    OrderItem updateOrderItem(PutawayItem putawayItem, OrderItem orderItem) {
        OrderItemStatusCode orderItemStatusCode =
                !putawayItem?.splitItems?.empty ? OrderItemStatusCode.CANCELED :
                        putawayItem.putawayStatus == PutawayStatus.COMPLETED ? OrderItemStatusCode.COMPLETED : OrderItemStatusCode.PENDING

        orderItem.orderItemStatusCode = orderItemStatusCode
        orderItem.product = putawayItem.product
        orderItem.inventoryItem = putawayItem.inventoryItem
        orderItem.quantity = putawayItem.quantity
        orderItem.recipient = putawayItem.recipient
        orderItem.originBinLocation = putawayItem.currentLocation
        orderItem.destinationBinLocation = putawayItem.putawayLocation
        return orderItem
    }

    void validatePutaway(Putaway putaway) {
        putaway.putawayItems.toArray().each { PutawayItem putawayItem ->
            validatePutawayItem(putawayItem)
        }
    }


    void validatePutawayItem(PutawayItem putawayItem) {
        def quantity = putawayItem.quantity

        if (putawayItem.splitItems) {
            quantity = putawayItem.splitItems.sum { it.quantity }
        }

        validateQuantityAvailable(putawayItem.currentFacility, putawayItem.currentLocation, putawayItem.inventoryItem, quantity)
    }


    void validateQuantityAvailable(Location facility, Location internalLocation, InventoryItem inventoryItem, BigDecimal quantity) {

        if (!facility) {
            throw new IllegalArgumentException("Facility is required")
        }

        Integer quantityAvailable = productAvailabilityService.getQuantityOnHandInBinLocation(inventoryItem, internalLocation)
        log.info "Quantity: ${quantity} vs ${quantityAvailable}"

        if (quantityAvailable < 0) {
            throw new IllegalStateException("The inventory item is no longer available at the specified facility ${facility} and bin ${internalLocation} ")
        }

        if (quantity > quantityAvailable) {
            throw new IllegalStateException("Quantity available ${quantityAvailable} is less than quantity to putaway ${quantity} for product ${inventoryItem.product.productCode} ${inventoryItem.product.name}")
        }

    }

}
