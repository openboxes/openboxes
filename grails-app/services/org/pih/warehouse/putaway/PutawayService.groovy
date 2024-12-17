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
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.api.Putaway
import org.pih.warehouse.api.PutawayItem
import org.pih.warehouse.api.PutawayStatus
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.TransferStockCommand
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAvailability
import org.pih.warehouse.inventory.InventoryLevel
import org.grails.web.json.JSONObject
import org.pih.warehouse.core.User

@Transactional
class PutawayService {

    def userService
    def locationService
    def inventoryService
    def productAvailabilityService
    def identifierService
    def grailsApplication

    void generatePutaways(Location location, User putawayAssignee) {
        def putawayCandidates = getPutawayCandidates(location)
        for (PutawayItem putawayCandidate in putawayCandidates) {


            // Ignore putaway candidates that already have a putaway order associated with it
            if (!putawayCandidate.id) {

                log.info "Attempting to generate putaway for putaway candidate ..." + new JSONObject(putawayCandidate?.toJson()).toString(4)

                // Get the next available putaway location based on criteria (available vs unavailable, volume, weight, etc)
                Location putawayLocation = getNextAvailablePutawayLocation(location, putawayCandidate.product)
                if (!putawayLocation) {
                    log.warn("No available putaway location for ${putawayCandidate?.product?.productCode}, lotNumber: ${putawayCandidate?.inventoryItem?.lotNumber?:'Default'}, currentLocation: ${putawayCandidate.currentFacility}.${putawayCandidate?.currentLocation}, quantity=${putawayCandidate.quantity}")
                    return
                }

                // Create a new putaway order for the putaway candidate
                Putaway putaway = new Putaway()
                putaway.origin = location
                putaway.destination = location
                putaway.putawayDate = new Date()
                putaway.putawayStatus = PutawayStatus.PENDING
                putaway.putawayAssignee = putawayAssignee
                putaway.putawayNumber = identifierService.generateOrderIdentifier()
                putawayCandidate.putawayLocation = putawayLocation
                putaway.putawayItems.add(putawayCandidate)
                savePutaway(putaway)
            }
        }
    }

    def getPutawayUsers(Location location) {
        List<User> users = userService.findUsersByRoleType(RoleType.ROLE_PUTAWAY)
        if (!users) {
            String usernameOrId = ConfigurationHolder.config.openboxes.jobs.automaticSlottingJob.defaultPutawayAssignee
            User user = User.findByIdOrUsername(usernameOrId, usernameOrId)
            users = [user]
        }
        return users
    }

    def getNextAvailablePutawayLocation(Location location, Product product) {
        def availableLocations

        InventoryLevel inventoryLevel = InventoryLevel.findByProductAndInventory(product, location.inventory)
        if (location.supports(ActivityCode.DYNAMIC_SLOTTING)) {
            // If inventory level specifies a putaway zone, assign a putaway location within that zone
            if (inventoryLevel?.preferredBinLocation && inventoryLevel?.preferredBinLocation?.isZoneLocation()) {
                availableLocations = getAvailablePutawayLocations(location, inventoryLevel?.preferredBinLocation)
            }
            // Assign a static putaway location has been specified
            else if (inventoryLevel?.preferredBinLocation && inventoryLevel?.preferredBinLocation?.isInternalLocation()) {
               availableLocations = [inventoryLevel?.preferredBinLocation]
            }
            // Otherwise assign any available putaway location
            else {
                availableLocations = getAvailablePutawayLocations(location, null)
            }
        }
        else if (location.supports(ActivityCode.STATIC_SLOTTING)) {
            if (inventoryLevel?.preferredBinLocation && inventoryLevel?.preferredBinLocation?.isInternalLocation()) {
               availableLocations = [inventoryLevel?.preferredBinLocation]
            }
        }
        else {
            availableLocations = getAvailablePutawayLocations(location, null)
        }
        return availableLocations ? availableLocations[0] : null
    }

    def getAvailablePutawayLocations(Location location, Location zone = null) {
        return getAvailableLocations(location, zone, [ActivityCode.PUTAWAY_STOCK])
    }

    def getAvailableLocations(Location location, Location zone = null, List activityCodes) {

        def unavailableLocations = ProductAvailability.createCriteria().list {
            projections {
                property("binLocation")
            }
            eq("location", location)
            gt("quantityOnHand", 0)
        }

        // Unfortunately, we cannot do filter by supported activity in the query because of a bug with Hibernate
        unavailableLocations = unavailableLocations.findAll {it && it.supports(ActivityCode.PUTAWAY_STRATEGY_SINGLE_LPN) }

        // Get all active internal locations within the given facility and zone (if provided)
        def internalLocations = Location.createCriteria().list {
            eq("active", Boolean.TRUE)
            eq("parentLocation", location)
            locationType {
                'in'("locationTypeCode", [LocationTypeCode.INTERNAL, LocationTypeCode.BIN_LOCATION])
            }
            if (zone) {
                eq("zone", zone)
            }
            locationType {
                order("sortOrder", "desc")
            }
            order("sortOrder","desc")
            order("name","asc")
        }

        // Get all available locations (with any of the given activity codes) that do not have a hold on them
        def supportedLocations = internalLocations.findAll { Location it ->
            it.supportsAny((ActivityCode[]) activityCodes.toArray())
        }

        // Get all putaway locations that have already been assigned in other putaway orders
        def pendingPutawayLocations = getPendingItems(location)?.collect { it.putawayLocation }?.findAll { it && it.supports(ActivityCode.PUTAWAY_STRATEGY_SINGLE_LPN) }

        // Return all putaway locations that are not already in use
        def availableLocations = supportedLocations - unavailableLocations - pendingPutawayLocations

        return availableLocations
    }

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

        grailsApplication.mainContext.publishEvent(new PutawayCompletedEvent(putaway))

        return order
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
