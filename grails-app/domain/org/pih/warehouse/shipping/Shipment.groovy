/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.shipping

import grails.util.Holders
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import org.pih.warehouse.core.*
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.donation.Donor
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.RefreshOrderSummaryEvent
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.requisition.Requisition
import util.StringUtil

class Shipment implements Comparable, Serializable, Historizable {

    def publishRefreshEvent() {
        Holders.grailsApplication.mainContext.publishEvent(new RefreshOrderSummaryEvent(this))
    }

    def beforeInsert() {
        createdBy = AuthService.currentUser
        updatedBy = AuthService.currentUser
        currentEvent = mostRecentSystemEvent
        currentStatus = status.code
    }

    def beforeUpdate() {
        updatedBy = AuthService.currentUser
        currentEvent = mostRecentSystemEvent
        currentStatus = status.code
    }

    def afterInsert() {
        publishRefreshEvent()
    }

    def afterUpdate() {
        publishRefreshEvent()
    }

    String id
    String name                    // user-defined name of the shipment
    String description
    String shipmentNumber            // an auto-generated shipment number
    Date expectedShippingDate        // the date the origin expects to ship the goods (required)
    Date expectedDeliveryDate
    // the date the destination should expect to receive the goods (optional)
    Float statedValue
    Float totalValue                // the total value of all items in the shipment
    String additionalInformation// any additional information about the shipment (e.g., comments)
    Float weight                                            // weight of container
    String weightUnits = Constants.DEFAULT_WEIGHT_UNITS    // standard weight unit: kg, lb

    Long shipmentItemCount

    // Audit fields
    Date dateCreated
    Date lastUpdated

    // One-to-one associations
    Location origin                    // the location from which the shipment will depart
    Location destination            // the location to which the shipment will arrive
    ShipmentType shipmentType        // the shipment type: Air, Sea Freight, Suitcase
    ShipmentMethod shipmentMethod    // the shipping carrier and shipping service used
    Person carrier// the person or organization that actually carries the goods from A to B
    Person recipient                // the person or organization that is receiving the goods
    Donor donor                        // the information about the donor (OPTIONAL)
    String driverName                // added for stock movements (should use carrier)

    // One-to-many associations
    SortedSet events

    Requisition requisition

    Event currentEvent
    ShipmentStatusCode currentStatus
    User createdBy
    User updatedBy

    List documents
    List comments
    List referenceNumbers

    SortedSet receipts

    Boolean disableRefresh = Boolean.TRUE

    static transients = [
            "allShipmentItems",
            "unpackedShipmentItems",
            "containersByType",
            "mostRecentEvent",
            "status",
            "actualShippingDate",
            "actualDeliveryDate",
            "recipients",
            "consignorAddress",
            "consigneeAddress",
            "receipt",
            "isFromPurchaseOrder",
            "purchaseOrder",
            "purchaseOrders",
            "orders",
            "isFromReturnOrder",
            "isFromPutawayOrder",
            "isFromTransferOrder",
            "returnOrder",
            "disableRefresh"
    ]

    static mappedBy = [
            outgoingTransactions: 'outgoingShipment',
            incomingTransactions: 'incomingShipment'
    ]

    // Core association mappings
    static hasMany = [
            events              : Event,
            comments            : Comment,
            containers          : Container,
            documents           : Document,
            receipts            : Receipt,
            shipmentItems       : ShipmentItem,
            referenceNumbers    : ReferenceNumber,
            outgoingTransactions: Transaction,
            incomingTransactions: Transaction
    ]


    // Ran into Hibernate bug HHH-4394 and GRAILS-4089 when trying to order the associations.  This is due to the
    // fact that the many side of the association (e.g. 'events') does not have a belongsTo 'shipment'.  So instead
    // of adding a foreign key reference to the 'event' table, GORM creates a new join table 'shipment_event' to
    // map 'events' to 'shipments' (which is exactly what I want).  However, the events are not 'eagerly' fetched
    // so the query to pull the data (and sort) only uses the 'shipment_event' table.  So for now, I'm going to
    // use a SortedSet for events and have the Event class implement Comparable.

    static mapping = {
        id generator: 'uuid'
        cache true
        additionalInformation type: "text"
        events cascade: "all-delete-orphan"
        comments cascade: "all-delete-orphan"
        documents cascade: "all-delete-orphan"
        shipmentItemCount(formula: '(select count(shipment_item.id) from shipment_item where (shipment_item.shipment_id = id))')
        shipmentMethod cascade: "all-delete-orphan"
        referenceNumbers cascade: "all-delete-orphan"
        receipts cascade: "all-delete-orphan"
        containers sort: 'sortOrder', order: 'asc'
    }

    // Constraints
    static constraints = {
        name(nullable: false, blank: false, maxSize: 255)
        description(nullable: true, blank: true)
        shipmentNumber(nullable: true, blank: false, maxSize: 255)
        origin(nullable: false,
                validator: { value, obj -> !value.equals(obj.destination) })
        destination(nullable: false)
        expectedShippingDate(nullable: false,
                validator: { value, obj -> !obj.expectedDeliveryDate || value.before(obj.expectedDeliveryDate + 1) })
        expectedDeliveryDate(nullable: true)    // optional
        shipmentType(nullable: false)
        shipmentMethod(nullable: true)
        additionalInformation(nullable: true, maxSize: 2147483646)
        carrier(nullable: true)
        recipient(nullable: true)
        donor(nullable: true)
        driverName(nullable: true)
        statedValue(nullable: true, max: 99999999F)
        totalValue(nullable: true, max: 99999999F)
        dateCreated(nullable: true)
        lastUpdated(nullable: true)
        weight(nullable: true, max: 99999999F)
        weightUnits(nullable: true)
        // a shipment can't have two reference numbers of the same type (we may want to change this, but UI makes this assumption at this point)
        referenceNumbers(validator: { referenceNumbers ->
            referenceNumbers?.collect({
                it.referenceNumberType?.id
            })?.unique({ a, b -> a <=> b })?.size() == referenceNumbers?.size()
        })

        // a shipment can't have two system events with the same event code
        events(validator: { events ->
            Set<Event> systemEvents = events.findAll {
                it?.eventType?.eventCode in EventCode.listSystemEventTypeCodes()
            }
            return systemEvents.unique().size() == systemEvents.size()
        })
        requisition(nullable: true)
        shipmentItemCount(nullable: true)
        currentStatus(nullable: true)
        currentEvent(nullable: true)
        createdBy(nullable: true)
        updatedBy(nullable: true)
    }


    String toString() { return "$name" }

    /**
     * Sort by name
     */

    // TODO: is this in descending order for a good reason?
    int compareTo(obj) {
        obj.name <=> name
    }

    /**
     * Transient method that gets all shipment items two-levels deep.
     *
     * TODO Need to make this recursive.
     *
     * @return
     */

    Collection<ShipmentItem> getAllShipmentItems() {
        List<ShipmentItem> shipmentItems = new ArrayList<ShipmentItem>()

        for (shipmentItem in unpackedShipmentItems) {
            shipmentItems.add(shipmentItem)
        }

        for (container in containers) {
            for (shipmentItem in container?.shipmentItems) {
                shipmentItems.add(shipmentItem)
            }
            for (childContainer in container?.containers) {
                for (shipmentItem in childContainer?.shipmentItems) {
                    shipmentItems.add(shipmentItem)
                }
            }
        }
        return shipmentItems
    }

    Collection<ShipmentItem> getUnpackedShipmentItems() {
        return shipmentItems.findAll { !it.container }
    }

    /**
     * Invoking the default sort() method from a GSP on the shipment items association does not seem to work reliably,
     * so we're going to try to perform the sort operation within the shipment.
     *
     * @return
     */
    List sortShipmentItems() {
        def shipmentItemComparator = { a, b ->
            def sortOrder =
                    a?.container?.parentContainer?.sortOrder <=> b?.container?.parentContainer?.sortOrder ?:
                            a?.container?.sortOrder <=> b?.container?.sortOrder ?:
                                    a?.inventoryItem?.product?.name <=> b?.inventoryItem?.product?.name ?:
                                            a?.inventoryItem?.lotNumber <=> b?.inventoryItem?.lotNumber ?:
                                                    a?.product?.name <=> b?.product?.name ?:
                                                            a?.lotNumber <=> b?.lotNumber ?:
                                                                    b?.quantity <=> a?.quantity
            return sortOrder
        }

        return shipmentItems?.sort(shipmentItemComparator)
    }

    List sortShipmentItemsBySortOrder() {
        def shipmentItemComparator = { a, b ->
            def sortOrder =
                    a?.container?.parentContainer?.sortOrder <=> b?.container?.parentContainer?.sortOrder ?:
                            a?.container?.sortOrder <=> b?.container?.sortOrder ?:
                                    a?.requisitionItem?.orderIndex <=> b?.requisitionItem?.orderIndex ?:
                                            a?.sortOrder <=> b?.sortOrder ?:
                                                    a?.inventoryItem?.product?.name <=> b?.inventoryItem?.product?.name ?:
                                                            a?.inventoryItem?.lotNumber <=> b?.inventoryItem?.lotNumber ?:
                                                                    a?.product?.name <=> b?.product?.name ?:
                                                                            a?.lotNumber <=> b?.lotNumber ?:
                                                                                    b?.quantity <=> a?.quantity
            return sortOrder
        }

        return shipmentItems?.sort(shipmentItemComparator)
    }


    Map<String, List<Container>> getContainersByType() {
        Map<String, List<Container>> containerMap = new HashMap<String, List<Container>>()
        containers.each {

            def containersByType = containerMap.get(it.containerType.name)
            if (!containersByType) {
                containersByType = new ArrayList<Container>()
            }
            containersByType.add(it)
            containerMap.put(it.containerType.name, containersByType)
        }
        return containerMap

    }

    // for inbounds and outbounds only
    Order getReturnOrder() {
        return orders?.find { it.isReturnOrder }
    }

    List<Order> getPurchaseOrders() {
        return orders?.findAll { it.isPurchaseOrder }
    }

    Order getPurchaseOrder() {
        return orders?.find { it.isPurchaseOrder }
    }

    List<Order> getOrders() {
        return this.shipmentItems*.orderItems?.order?.flatten()?.unique()
    }

    Boolean isPending() {
        return !this.hasShipped() && !this.wasReceived()
    }

    Boolean hasShipped() {
        return events.any { it.eventType?.eventCode == EventCode.SHIPPED }
    }

    Boolean wasReceived() {
        return events.any { it.eventType?.eventCode == EventCode.RECEIVED }
    }

    Boolean wasPartiallyReceived() {
        return events.any { it.eventType?.eventCode == EventCode.PARTIALLY_RECEIVED }
    }

    Boolean getIsFromPurchaseOrder() {
        return !orders?.isEmpty() && orders.every { it.isPurchaseOrder }
    }

    Boolean getIsFromReturnOrder() {
        return !orders?.isEmpty() && orders?.every { it.isReturnOrder }
    }

    Boolean getIsFromPutawayOrder() {
        return !orders?.isEmpty() && orders?.every { it.isPutawayOrder }
    }

    Boolean getIsFromTransferOrder() {
        // Check if is transfer order, but exclude Putaways and Returns
        return !orders?.isEmpty() && orders?.every { it.isTransferOrder } && !orders?.any { it.isPutawayOrder } && !orders?.any { it.isReturnOrder }
    }

    Boolean isStockMovement() {
        return requisition != null
    }

    Boolean isReceiveAllowed() {
        return hasShipped() && !wasReceived()
    }

    Boolean isPartialReceiveAllowed() {
        return isReceiveAllowed() && isStockMovement()
    }

    Boolean isSendAllowed() {
        return !hasShipped() && !wasReceived()
    }

    Boolean isFullyReceived() {
        return shipmentItems?.every { ShipmentItem shipmentItem -> shipmentItem.isFullyReceived() }
    }

    ReferenceNumber getReferenceNumber(String typeName) {
        def referenceNumberType = ReferenceNumberType.findByName(typeName)
        if (referenceNumberType) {
            for (referenceNumber in referenceNumbers) {
                if (referenceNumber.referenceNumberType == referenceNumberType) {
                    return referenceNumber
                }
            }
        }
        return null
    }


    Date getActualShippingDate() {
        for (event in events) {
            if (event?.eventType?.eventCode == EventCode.SHIPPED) {
                return event?.eventDate
            }
        }
        return null
    }

    Date getActualDeliveryDate() {
        for (event in events) {
            if (event?.eventType?.eventCode == EventCode.RECEIVED) {
                return event?.eventDate
            }
        }
        return null
    }

    User getShippedBy() {
        Event shippedEvent = events?.find { it.eventType?.eventCode == EventCode.SHIPPED }
        if (shippedEvent) {
            // For the e-requests we want to have the fallback to the user, who last updated the shipment
            if (requisition?.electronicType) {
                return shippedEvent.createdBy ?: requisition.updatedBy
            }
            // The fallback for createdBy is done, because there was a bug at Event level,
            // which didn't persist the event.createdBy, so for SMs from the past, we might not have the shippedEvent.createdBy
            return shippedEvent?.createdBy ?: createdBy
        }
        return null
    }


    Event getMostRecentEvent() {
        if (events && events.size() > 0) {
            return events.iterator().next()
        }
        return null
    }

    Event getMostRecentSystemEvent() {
        Set<Event> systemEvents = events.findAll { EventCode.listSystemEventTypeCodes().contains(it.eventType?.eventCode) }.sort()
        if (systemEvents?.size()) {
            return systemEvents.first()
        }
        return null
    }

    ShipmentStatus getStatus() {
        if (this.wasReceived()) {
            return new ShipmentStatus([code    : ShipmentStatusCode.RECEIVED,
                                       date    : this.getActualDeliveryDate(),
                                       location: this.destination])
        } else if (wasPartiallyReceived()) {
            return new ShipmentStatus([code    : ShipmentStatusCode.PARTIALLY_RECEIVED,
                                       date    : this.getActualDeliveryDate(),
                                       location: this.destination])
        } else if (this.hasShipped()) {
            return new ShipmentStatus([code    : ShipmentStatusCode.SHIPPED,
                                       date    : this.getActualShippingDate(),
                                       location: this.origin])
        } else {
            return new ShipmentStatus([code    : ShipmentStatusCode.PENDING,
                                       date    : null,
                                       location: null])
        }
    }

    /**
     * Adds a new container to the shipment of the specified type
     */
    Container addNewContainer(ContainerType containerType) {
        def sortOrder = (this.containers) ? this.containers.size() : 0

        def container = new Container(
                containerType: containerType,
                shipment: this,
                recipient: this.recipient,
                sortOrder: sortOrder
        )

        addToContainers(container)

        return container
    }

    Receipt getReceipt() {
        return receipts ? receipts.first() : null
    }

    /**
     * Get all recipients for this shipment
     *
     * @return
     */
    def getRecipients() {
        def recipients = []
        containers.each { container ->
            if (container?.recipient?.email) {
                recipients.add(container.recipient)
            }
        }
        shipmentItems.each { shipmentItem ->
            if (shipmentItem?.recipient?.email) {
                recipients.add(shipmentItem.recipient)
            }
        }
        if (recipient?.email) {
            recipients.add(recipient)
        }
        return recipients?.unique()
    }

    String getConsigneeAddress() {
        return destination?.address?.description ?: destination?.locationGroup?.address?.description
    }

    String getConsignorAddress() {
        return origin?.address?.description ?: origin?.locationGroup?.address?.description
    }

    /**
     * Clones the specified container
     */
    void cloneContainer(Container container, Integer quantity) {}

    Float totalWeightInKilograms() {
        return containers.findAll { it.parentContainer == null }.collect {
            it.totalWeightInKilograms()
        }.sum()
    }

    Float totalWeightInPounds() {
        return containers.findAll { it.parentContainer == null }.collect {
            it.totalWeightInPounds()
        }.sum()
    }

    Float calculateTotalValue() {
        return shipmentItems?.findAll { it.product.pricePerUnit }?.collect {
            it?.quantity * it?.product?.pricePerUnit
        }?.sum() ?: 0
    }

    Collection findAllParentContainers() {
        return containers.findAll { !it.parentContainer }
    }

    Collection findAllChildContainers(Container container) {
        return Container.findAllByShipmentAndParentContainer(this, container)
    }

    Container findContainerByName(String name) {
        return containers.find { it.name.equalsIgnoreCase(name) }
    }

    Container findContainerByNameAndContainerType(String name, ContainerType containerType) {
        return containers.find {
            it.name.equalsIgnoreCase(name) && it.containerType.equals(containerType)
        }
    }


    Container addNewPallet(String name) {
        return addNewContainer(name, ContainerType.findById(Constants.PALLET_CONTAINER_TYPE_ID))
    }

    Container addNewBox(String name) {
        return addNewContainer(name, ContainerType.findById(Constants.BOX_CONTAINER_TYPE_ID))
    }

    Container addNewContainer(String name, ContainerType containerType) {
        Container container = addNewContainer(containerType)
        container.name = name
        return container
    }

    Container findOrCreatePallet(String palletName) {
        Container pallet = findContainerByName(palletName)
        if (!pallet) {
            pallet = addNewPallet(palletName)
        }
        return pallet
    }

    Container findOrCreateContainer(String containerName, ContainerType containerType) {
        Container container = findContainerByNameAndContainerType(containerName, containerType)
        if (!container) {
            container = addNewContainer(containerName, containerType)
        }
        return container
    }


    ShipmentItem getNextShipmentItem(String currentShipmentItemId) {
        def nextIndex
        def shipmentItems = sortShipmentItems()
        def shipmentItemIndex = shipmentItems.findIndexOf { it.id == currentShipmentItemId }
        def shipmentItemCount = shipmentItems.size()

        // Wrap if we hit the end of the list
        if (shipmentItemIndex >= shipmentItemCount - 1) {
            nextIndex = 0
        } else {
            nextIndex = shipmentItemIndex + 1
        }

        return shipmentItems.get(nextIndex)
    }

    ShipmentItem findOrCreateShipmentItem(String id) {
        ShipmentItem shipmentItem
        if (id) {
            shipmentItem = shipmentItems.find { ShipmentItem si -> si.id == id }
            if (!shipmentItem) {
                throw new IllegalArgumentException("Could not find shipmente item with id ${id}")
            }
        }
        if (!shipmentItem) {
            shipmentItem = new ShipmentItem()
            addToShipmentItems(shipmentItem)
        }
        return shipmentItem
    }


    ShipmentItem findShipmentItem(InventoryItem inventoryItem, Container container, Person recipient) {
        ShipmentItem shipmentItem = ShipmentItem.withCriteria(uniqueResult: true) {
            eq('shipment', this)
            if (container) {
                eq('container', container)
            } else {
                isNull('container')
            }
            eq('inventoryItem', inventoryItem)

            if (recipient) {
                eq("recipient", recipient)
            }
        }
        return shipmentItem
    }


    Collection findShipmentItemsByContainer(container) {
        return ShipmentItem?.findAllByShipmentAndContainer(this, container)
    }

    Integer countShipmentItemsByContainer(container) {
        return ShipmentItem?.countByShipmentAndContainer(this, container)
    }

    Integer countShipmentItems() {
        return ShipmentItem.countByShipment(this)
    }

    TimeDuration timeToProcess() {
        return timeDuration(dateScheduled(), dateShipped())
    }

    TimeDuration timeInTransit() {
        return timeDuration(dateShipped(), dateDelivered())
    }

    TimeDuration timeInCustoms() {
        return timeDuration(dateCustomsEntry(), dateCustomsRelease())
    }

    TimeDuration timeDuration(Date startDate, Date endDate) {
        if (startDate && endDate) {
            return TimeCategory.minus(endDate, startDate)
        }
        return null
    }

    Date dateScheduled() {
        Event event = events.find { Event event -> event?.eventType?.eventCode == EventCode.SCHEDULED }
        return event?.eventDate ?: dateCreated
    }

    Date dateShipped() {
        Event event = events.find { Event event -> event?.eventType?.eventCode == EventCode.SHIPPED }
        return event?.eventDate ?: actualShippingDate
    }

    Date dateDelivered() {
        Event event = events.find { Event event -> event?.eventType?.eventCode == EventCode.RECEIVED || event?.eventType?.eventCode == EventCode.DELIVERED }
        return event?.eventDate ?: actualDeliveryDate
    }

    Date dateCustomsEntry() {
        Event event = events.find { Event event -> event?.eventType?.eventCode == EventCode.CUSTOMS_ENTRY }
        return event?.eventDate
    }

    Date dateCustomsRelease() {
        Event event = events.find { Event event -> event?.eventType?.eventCode == EventCode.CUSTOMS_RELEASE }
        return event?.eventDate
    }

    boolean hasInvoicedItem() {
        shipmentItems.any { it.quantityInvoiced > 0 }
    }

    Boolean hasParentContainer() {
        shipmentItems?.any { it?.container?.parentContainer }
    }

    Boolean hasChildContainer() {
        shipmentItems?.any { it?.container }
    }

    Set<Event> getCustomEvents() {
        return events.findAll { EventCode.listCustomEventTypeCodes().contains(it.eventType?.eventCode) }
    }

    @Override
    ReferenceDocument getReferenceDocument() {
        return new ReferenceDocument(
                label: shipmentNumber,
                url: "/stockMovement/show/${requisition?.id ?: id}",
                id: id,
                identifier: shipmentNumber,
                description: description,
                name: name,
        )
    }

    @Override
    List<HistoryItem> getHistory() {
        List<HistoryItem> histories = []
        // First collect history of CREATED event
        histories.add(new HistoryItem<Shipment>(
                date: dateCreated,
                location: origin,
                eventType: new EventTypeDto(
                        name: StringUtil.format(EventCode.CREATED.name()),
                        eventCode: EventCode.CREATED,
                ),
                referenceDocument: referenceDocument,
                createdBy: createdBy,
        ))
        // Then collect history of a shipped event if any
        if (hasShipped()) {
            histories.add(new HistoryItem<Shipment>(
                    date: dateShipped(),
                    location: origin,
                    eventType: new EventTypeDto(
                            name: StringUtil.format(EventCode.SHIPPED.name()),
                            eventCode: EventCode.SHIPPED,
                    ),
                    referenceDocument: referenceDocument,
                    createdBy: shippedBy,
            ))
        }
        // Then collect history of a partially_received events if any
        if (wasPartiallyReceived()) {
            // If there is a received event, exclude it from the set
            Set<Receipt> partialReceipts = wasReceived() ? (receipts - receipts.last()) : receipts
            List<HistoryItem<Receipt>> partiallyReceivedHistoryItem = partialReceipts.collect { it.getHistory() }.flatten()
            partiallyReceivedHistoryItem.each {
                it.eventType = new EventTypeDto(
                        name: StringUtil.format(EventCode.PARTIALLY_RECEIVED.name()),
                        eventCode: EventCode.PARTIALLY_RECEIVED,
                )
            }
            histories.addAll(partiallyReceivedHistoryItem)
        }
        // Then collect history of a received event if any
        if (wasReceived()) {
            List<HistoryItem<Receipt>> receivedHistoryItem = receipts.last().getHistory()
            receivedHistoryItem.each {
                it.eventType = new EventTypeDto(
                        name: StringUtil.format(EventCode.RECEIVED.name()),
                        eventCode: EventCode.RECEIVED,
                )
            }
            histories.addAll(receivedHistoryItem)
        }
        // At the end collect history of custom events
        List<HistoryItem<Event>> customEventsHistory = getCustomEvents().collect { it.getHistory() }.flatten()
        histories.addAll(customEventsHistory)
        return histories
    }

    Map toJson() {
        def containerList = []
        def shipmentItemsByContainer = shipmentItems?.groupBy { it.container }
        shipmentItemsByContainer.each { container, shipmentItems ->
            containerList << [id: container?.id, name: container?.name, status: container?.containerStatus?.name(), type: container?.containerType?.name, shipmentItems: shipmentItems]
        }

        return [
                id                  : id,
                name                : name,
                status              : status?.code?.name(),
                origin              : [
                        id  : origin?.id,
                        name: origin?.name,
                        type: origin?.locationType?.locationTypeCode?.name()
                ],
                destination         : [
                        id  : destination?.id,
                        name: destination?.name,
                        type: destination?.locationType?.locationTypeCode?.name()

                ],
                expectedShippingDate: expectedShippingDate?.format("MM/dd/yyyy HH:mm XXX"),
                actualShippingDate  : actualShippingDate?.format("MM/dd/yyyy HH:mm XXX"),
                expectedDeliveryDate: expectedDeliveryDate?.format("MM/dd/yyyy HH:mm XXX"),
                actualDeliveryDate  : actualDeliveryDate?.format("MM/dd/yyyy HH:mm XXX"),
                shipmentItems       : shipmentItems,
                containers          : containerList,

                // Mobile
                shipmentNumber      : shipmentNumber,
                receivedCount       : shipmentItems?.findAll { it.isFullyReceived() }.size(),
        ]
    }
}

