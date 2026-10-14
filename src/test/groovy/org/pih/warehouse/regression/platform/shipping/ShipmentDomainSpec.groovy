package org.pih.warehouse.regression.platform.shipping

import spock.lang.PendingFeature

import org.pih.warehouse.shipping.*
import grails.testing.gorm.DataTest
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant

import org.pih.warehouse.core.Address
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.EventType
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationGroup
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.ReferenceDocument
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.invoice.InvoiceItem
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.order.OrderTypeCode
import org.pih.warehouse.product.Product
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.receiving.ReceiptStatusCode
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionSourceType

/**
 * Behavior of the Shipment domain: the status and predicates derived from its events, the order and container
 * helpers, and the custom validators. (ShipmentTests holds the old, ignored JUnit version of a few validation
 * checks; this spec does not depend on it.)
 */
@Unroll
class ShipmentDomainSpec extends Specification implements DataTest {

    Location origin
    Location destination

    void setupSpec() {
        mockDomains(Shipment, ShipmentItem, Container, ContainerType, ShipmentType, ReferenceNumber, ReferenceNumberType, Event,
                EventType, Location, LocationGroup, Address, Inventory, InventoryItem, Product, Order, OrderItem,
                OrderType, Requisition, RequisitionItem, Person, User, Receipt, ReceiptItem, InvoiceItem)
    }

    void setup() {
        origin = new Location(name: 'Origin')
        destination = new Location(name: 'Destination')
    }

    void 'status should be #expectedCode for a shipment with events #eventCodes'() {
        given:
        Shipment shipment = buildShipment(eventCodes.withIndex().collect { EventCode code, int index ->
            buildEvent(code, "2024-01-1${index}T08:00:00Z")
        })

        when:
        ShipmentStatus status = shipment.status

        then:
        assert status.code == expectedCode
        assert status.location == [origin: origin, destination: destination][expectedLocation]

        where:
        eventCodes                                                             || expectedCode                           | expectedLocation
        []                                                                     || ShipmentStatusCode.PENDING             | null
        [EventCode.CREATED]                                                    || ShipmentStatusCode.PENDING             | null
        [EventCode.SHIPPED]                                                    || ShipmentStatusCode.SHIPPED             | 'origin'
        [EventCode.SHIPPED, EventCode.PARTIALLY_RECEIVED]                      || ShipmentStatusCode.PARTIALLY_RECEIVED  | 'destination'
        [EventCode.SHIPPED, EventCode.RECEIVED]                                || ShipmentStatusCode.RECEIVED            | 'destination'
        [EventCode.SHIPPED, EventCode.PARTIALLY_RECEIVED, EventCode.RECEIVED]  || ShipmentStatusCode.RECEIVED            | 'destination'
    }

    void 'status should carry the date of the event that set it'() {
        given:
        Event shipped = buildEvent(EventCode.SHIPPED, '2024-01-10T08:00:00Z')
        Event received = buildEvent(EventCode.RECEIVED, '2024-01-12T08:00:00Z')

        expect:
        assert buildShipment([]).status.date == null
        assert buildShipment([shipped]).status.date == date('2024-01-10T08:00:00Z')
        assert buildShipment([shipped, received]).status.date == date('2024-01-12T08:00:00Z')
    }

    void 'the event predicates should be #description'() {
        given:
        Shipment shipment = buildShipment(eventCodes.withIndex().collect { EventCode code, int index ->
            buildEvent(code, "2024-01-1${index}T08:00:00Z")
        })
        if (stockMovement) {
            shipment.requisition = new Requisition(name: 'Request')
        }

        expect:
        assert shipment.isPending() == pending
        assert shipment.hasShipped() == shipped
        assert shipment.wasReceived() == received
        assert shipment.wasPartiallyReceived() == partiallyReceived
        assert shipment.isSendAllowed() == sendAllowed
        assert shipment.isReceiveAllowed() == receiveAllowed
        assert shipment.isPartialReceiveAllowed() == partialReceiveAllowed
        assert shipment.isStockMovement() == stockMovement

        where:
        eventCodes                                        | stockMovement || pending | shipped | received | partiallyReceived | sendAllowed | receiveAllowed | partialReceiveAllowed | description
        []                                                | false         || true    | false   | false    | false             | true        | false          | false                 | 'sendable for a new shipment'
        [EventCode.SHIPPED]                               | false         || false   | true    | false    | false             | false       | true           | false                 | 'receivable but not partially for a shipped plain shipment'
        [EventCode.SHIPPED]                               | true          || false   | true    | false    | false             | false       | true           | true                  | 'partially receivable for a shipped stock movement'
        [EventCode.SHIPPED, EventCode.PARTIALLY_RECEIVED] | true          || false   | true    | false    | true              | false       | true           | true                  | 'still receivable for a partially received stock movement'
        [EventCode.SHIPPED, EventCode.RECEIVED]           | true          || false   | true    | true     | false             | false       | false          | false                 | 'closed for a received shipment'
        [EventCode.RECEIVED]                              | false         || false   | false   | true     | false             | false       | false          | false                 | 'closed for a shipment received without a shipped event'
    }

    void 'mostRecentEvent should be the newest event and mostRecentSystemEvent the newest system event'() {
        given:
        Event created = buildEvent(EventCode.CREATED, '2024-01-01T08:00:00Z')
        Event shipped = buildEvent(EventCode.SHIPPED, '2024-01-03T08:00:00Z')
        Event customsEntry = buildEvent(EventCode.CUSTOMS_ENTRY, '2024-01-05T08:00:00Z')
        Shipment shipment = buildShipment([customsEntry, created, shipped])

        expect:
        assert shipment.mostRecentEvent == customsEntry
        assert shipment.mostRecentSystemEvent == shipped
    }

    void 'mostRecentEvent and mostRecentSystemEvent should be null when there are no such events'() {
        expect:
        assert buildShipment([]).mostRecentEvent == null
        assert buildShipment([]).mostRecentSystemEvent == null
        assert buildShipment([buildEvent(EventCode.CUSTOMS_ENTRY, '2024-01-05T08:00:00Z')]).mostRecentSystemEvent == null
    }

    void 'getEventByCode should find the event with the given code'() {
        given:
        Event shipped = buildEvent(EventCode.SHIPPED, '2024-01-03T08:00:00Z')
        Shipment shipment = buildShipment([buildEvent(EventCode.CREATED, '2024-01-01T08:00:00Z'), shipped])

        expect:
        assert shipment.getEventByCode(EventCode.SHIPPED) == shipped
        assert shipment.getEventByCode(EventCode.RECEIVED) == null
    }

    void 'resynchronizeEventAndStatus should store the derived status and the newest system event'() {
        given:
        Event shipped = buildEvent(EventCode.SHIPPED, '2024-01-03T08:00:00Z')
        Shipment shipment = buildShipment([buildEvent(EventCode.CREATED, '2024-01-01T08:00:00Z'), shipped])

        when:
        shipment.resynchronizeEventAndStatus()

        then:
        assert shipment.currentStatus == ShipmentStatusCode.SHIPPED
        assert shipment.currentEvent == shipped
    }

    void 'shippedBy should be #description'() {
        given:
        User eventCreator = new User(username: 'event-creator', lastName: 'Event')
        User shipmentCreator = new User(username: 'shipment-creator', lastName: 'Shipment')
        User requisitionUpdater = new User(username: 'requisition-updater', lastName: 'Requisition')
        Event shipped = buildEvent(EventCode.SHIPPED, '2024-01-03T08:00:00Z')
        shipped.createdBy = eventHasCreator ? eventCreator : null
        Shipment shipment = buildShipment(hasShippedEvent ? [shipped] : [])
        shipment.createdBy = shipmentCreator
        if (sourceType) {
            shipment.requisition = new Requisition(sourceType: sourceType, updatedBy: requisitionUpdater)
        }
        Map users = [event: eventCreator, shipment: shipmentCreator, requisition: requisitionUpdater]

        expect:
        assert shipment.shippedBy.is(users[expected])

        where:
        hasShippedEvent | eventHasCreator | sourceType                         || expected      | description
        false           | false           | null                               || null          | 'nobody before the shipment is shipped'
        true            | true            | null                               || 'event'       | 'the creator of the shipped event'
        true            | false           | null                               || 'shipment'    | 'the creator of the shipment when the event has no creator'
        true            | true            | RequisitionSourceType.ELECTRONIC   || 'event'       | 'the creator of the shipped event of an electronic request'
        true            | false           | RequisitionSourceType.ELECTRONIC   || 'requisition' | 'the last updater of an electronic request when the event has no creator'
        true            | false           | RequisitionSourceType.PAPER        || 'shipment'    | 'the creator of the shipment for a paper request'
    }

    void 'the milestone dates and durations should be read off the events'() {
        given:
        Shipment shipment = buildShipment([
                buildEvent(EventCode.SCHEDULED, '2024-01-01T08:00:00Z'),
                buildEvent(EventCode.SHIPPED, '2024-01-03T08:00:00Z'),
                buildEvent(EventCode.CUSTOMS_ENTRY, '2024-01-04T08:00:00Z'),
                buildEvent(EventCode.CUSTOMS_RELEASE, '2024-01-04T20:00:00Z'),
                buildEvent(EventCode.RECEIVED, '2024-01-08T08:00:00Z'),
        ])

        expect:
        assert shipment.dateScheduled() == date('2024-01-01T08:00:00Z')
        assert shipment.dateShipped() == date('2024-01-03T08:00:00Z')
        assert shipment.actualShippingDate == date('2024-01-03T08:00:00Z')
        assert shipment.dateCustomsEntry() == date('2024-01-04T08:00:00Z')
        assert shipment.dateCustomsRelease() == date('2024-01-04T20:00:00Z')
        assert shipment.dateDelivered() == date('2024-01-08T08:00:00Z')
        assert shipment.actualDeliveryDate == date('2024-01-08T08:00:00Z')

        and:
        assert shipment.timeToProcess().toMilliseconds() == hours(48)
        assert shipment.timeInCustoms().toMilliseconds() == hours(12)
        assert shipment.timeInTransit().toMilliseconds() == hours(5 * 24)
    }

    void 'the milestone dates should fall back to the creation date and the delivered event'() {
        given:
        Shipment shipment = buildShipment([
                buildEvent(EventCode.SHIPPED, '2024-01-03T08:00:00Z'),
                buildEvent(EventCode.DELIVERED, '2024-01-06T08:00:00Z'),
        ])
        shipment.dateCreated = date('2023-12-31T08:00:00Z')

        expect: 'without a scheduled event the shipment was scheduled when it was created'
        assert shipment.dateScheduled() == date('2023-12-31T08:00:00Z')
        assert shipment.timeToProcess().toMilliseconds() == hours(3 * 24)

        and: 'without a received event the delivered event marks the delivery'
        assert shipment.dateDelivered() == date('2024-01-06T08:00:00Z')
        assert shipment.actualDeliveryDate == null
        assert shipment.timeInTransit().toMilliseconds() == hours(3 * 24)
    }

    void 'the durations should be null when one of their milestones is missing'() {
        given:
        Shipment shipment = buildShipment([buildEvent(EventCode.CUSTOMS_ENTRY, '2024-01-04T08:00:00Z')])

        expect:
        assert shipment.dateShipped() == null
        assert shipment.dateDelivered() == null
        assert shipment.timeToProcess() == null
        assert shipment.timeInTransit() == null
        assert shipment.timeInCustoms() == null
        assert shipment.timeDuration(date('2024-01-01T00:00:00Z'), null) == null
        assert shipment.timeDuration(null, date('2024-01-01T00:00:00Z')) == null
    }

    void 'isFullyReceived should be #expected when the lines have received #receivedQuantities out of 10'() {
        given:
        Shipment shipment = buildShipment([])
        receivedQuantities.each { Integer quantityReceived ->
            Product product = new Product(name: 'product')
            ShipmentItem shipmentItem = new ShipmentItem(product: product, quantity: 10)
            shipmentItem.receiptItems = [new ReceiptItem(product: product, quantityReceived: quantityReceived,
                    receipt: new Receipt(receiptStatusCode: ReceiptStatusCode.RECEIVED))] as Set
            shipment.addToShipmentItems(shipmentItem)
        }

        expect:
        assert shipment.isFullyReceived() == expected

        where:
        receivedQuantities || expected
        [10, 10]           || true
        [10, 9]            || false
        [0]                || false
    }

    void 'the order flags should be #expectedFlags for a shipment of #orderKinds orders'() {
        given:
        Shipment shipment = buildShipment([])
        // as loaded from the database, a shipment without lines has an empty collection of them
        shipment.shipmentItems = [] as Set
        orderKinds.each { String kind ->
            shipment.addToShipmentItems(new ShipmentItem(orderItems: [new OrderItem(order: buildOrder(kind))] as Set))
        }

        expect:
        assert [
                'purchase': shipment.isFromPurchaseOrder,
                'return'  : shipment.isFromReturnOrder,
                'putaway' : shipment.isFromPutawayOrder,
                'transfer': shipment.isFromTransferOrder,
        ].findAll { it.value }.keySet() as List == expectedFlags

        where:
        orderKinds                 || expectedFlags
        ['purchase']               || ['purchase']
        ['purchase', 'purchase']   || ['purchase']
        ['transfer']               || ['transfer']
        ['return']                 || ['return']
        ['putaway']                || ['putaway']
        ['purchase', 'transfer']   || []
        ['transfer', 'return']     || []
        ['transfer', 'putaway']    || []
        []                         || []
    }

    void 'orders should list each order of the lines once and pick out the purchase and return orders'() {
        given:
        Order purchaseOrder = buildOrder('purchase')
        Order otherPurchaseOrder = buildOrder('purchase')
        Order returnOrder = buildOrder('return')
        Shipment shipment = buildShipment([])
        shipment.addToShipmentItems(new ShipmentItem(orderItems: [
                new OrderItem(order: purchaseOrder), new OrderItem(order: purchaseOrder)] as Set))
        shipment.addToShipmentItems(new ShipmentItem(orderItems: [new OrderItem(order: otherPurchaseOrder)] as Set))
        shipment.addToShipmentItems(new ShipmentItem(orderItems: [new OrderItem(order: returnOrder)] as Set))

        expect:
        assert shipment.orders.size() == 3
        assert shipment.orders as Set == [purchaseOrder, otherPurchaseOrder, returnOrder] as Set
        assert shipment.purchaseOrders as Set == [purchaseOrder, otherPurchaseOrder] as Set
        assert shipment.purchaseOrder in [purchaseOrder, otherPurchaseOrder]
        assert shipment.returnOrder == returnOrder
    }

    void 'a shipment without orders should have no purchase or return order'() {
        given:
        Shipment shipment = buildShipment([])
        shipment.addToShipmentItems(new ShipmentItem(orderItems: [] as Set))

        expect:
        assert shipment.orders == []
        assert shipment.purchaseOrders == []
        assert shipment.purchaseOrder == null
        assert shipment.returnOrder == null
    }

    void 'sortShipmentItems should order by container, then product, lot and the larger quantity first'() {
        given:
        Product productA = new Product(name: 'A')
        Product productB = new Product(name: 'B')
        Container firstBox = new Container(name: 'first', sortOrder: 0)
        Container secondBox = new Container(name: 'second', sortOrder: 1)
        ShipmentItem looseALot2 = buildItem(productA, 'L2', 1, null)
        ShipmentItem looseALot1 = buildItem(productA, 'L1', 1, null)
        ShipmentItem looseALot1Larger = buildItem(productA, 'L1', 5, null)
        ShipmentItem looseB = buildItem(productB, 'L1', 1, null)
        ShipmentItem secondBoxA = buildItem(productA, 'L1', 1, secondBox)
        ShipmentItem firstBoxB = buildItem(productB, 'L1', 1, firstBox)
        Shipment shipment = buildShipment([])
        [secondBoxA, looseB, looseALot1, firstBoxB, looseALot2, looseALot1Larger].each {
            shipment.addToShipmentItems(it)
        }

        expect:
        assert shipment.sortShipmentItems() == [looseALot1Larger, looseALot1, looseALot2, looseB, firstBoxB, secondBoxA]
    }

    void 'sortShipmentItemsBySortOrder should follow the request line order before the product names'() {
        given:
        ShipmentItem secondLine = new ShipmentItem(product: new Product(name: 'A'), quantity: 1,
                requisitionItem: new RequisitionItem(orderIndex: 2))
        ShipmentItem firstLineSecondPick = new ShipmentItem(product: new Product(name: 'B'), quantity: 1, sortOrder: 1,
                requisitionItem: new RequisitionItem(orderIndex: 1))
        ShipmentItem firstLineFirstPick = new ShipmentItem(product: new Product(name: 'Z'), quantity: 1, sortOrder: 0,
                requisitionItem: new RequisitionItem(orderIndex: 1))
        Shipment shipment = buildShipment([])
        [secondLine, firstLineSecondPick, firstLineFirstPick].each { shipment.addToShipmentItems(it) }

        expect:
        assert shipment.sortShipmentItemsBySortOrder() == [firstLineFirstPick, firstLineSecondPick, secondLine]
        assert shipment.sortShipmentItems() == [secondLine, firstLineSecondPick, firstLineFirstPick]
    }

    void 'getNextShipmentItem should step through the sorted lines and wrap around at the end'() {
        given:
        Shipment shipment = buildShipment([])
        ShipmentItem first = buildItem(new Product(name: 'A'), 'L1', 1, null)
        first.id = 'item-a'
        ShipmentItem second = buildItem(new Product(name: 'B'), 'L1', 1, null)
        second.id = 'item-b'
        ShipmentItem third = buildItem(new Product(name: 'C'), 'L1', 1, null)
        third.id = 'item-c'
        [third, first, second].each { shipment.addToShipmentItems(it) }

        expect:
        assert shipment.getNextShipmentItem('item-a') == second
        assert shipment.getNextShipmentItem('item-b') == third
        assert shipment.getNextShipmentItem('item-c') == first
    }

    void 'findOrCreateShipmentItem should return the line with the given id'() {
        given:
        Shipment shipment = buildShipment([])
        ShipmentItem existing = new ShipmentItem(quantity: 1)
        existing.id = 'item-1'
        shipment.addToShipmentItems(existing)

        expect:
        assert shipment.findOrCreateShipmentItem('item-1').is(existing)
        assert shipment.shipmentItems.size() == 1
    }

    void 'findOrCreateShipmentItem should add a new line when no id is given'() {
        given:
        Shipment shipment = buildShipment([])
        shipment.addToShipmentItems(new ShipmentItem(quantity: 1))

        when:
        ShipmentItem created = shipment.findOrCreateShipmentItem(null)

        then:
        assert created.id == null
        assert created.shipment == shipment
        assert shipment.shipmentItems.size() == 2
        assert created in shipment.shipmentItems
    }

    void 'findOrCreateShipmentItem should refuse an id that is not a line of the shipment'() {
        given:
        Shipment shipment = buildShipment([])
        shipment.addToShipmentItems(new ShipmentItem(quantity: 1))

        when:
        shipment.findOrCreateShipmentItem('unknown')

        then:
        IllegalArgumentException exception = thrown()
        assert exception.message.contains('unknown')
        assert shipment.shipmentItems.size() == 1
    }

    void 'addNewContainer should append a container for the shipment recipient at the next sort order'() {
        given:
        Person recipient = new Person(firstName: 'Ann', lastName: 'Lee')
        ContainerType boxType = new ContainerType(name: 'Box')
        Shipment shipment = buildShipment([])
        shipment.recipient = recipient

        when:
        Container first = shipment.addNewContainer(boxType)
        Container second = shipment.addNewContainer('Box 2', boxType)

        then:
        assert first.sortOrder == 0
        assert second.sortOrder == 1
        assert second.name == 'Box 2'
        assert [first, second].every { it.shipment == shipment && it.recipient == recipient && it.containerType == boxType }
        assert shipment.containers == [first, second] as Set
    }

    void 'the container lookups should match names regardless of case and optionally the container type'() {
        given:
        ContainerType palletType = new ContainerType(name: 'Pallet')
        ContainerType boxType = new ContainerType(name: 'Box')
        Shipment shipment = buildShipment([])
        Container pallet = shipment.addNewContainer('Unit 1', palletType)
        Container box = shipment.addNewContainer('Unit 2', boxType)

        expect:
        assert shipment.findContainerByName('unit 1') == pallet
        assert shipment.findContainerByName('UNIT 2') == box
        assert shipment.findContainerByName('Unit 3') == null
        assert shipment.findContainerByNameAndContainerType('unit 2', boxType) == box
        assert shipment.findContainerByNameAndContainerType('unit 2', palletType) == null
    }

    void 'findOrCreateContainer should only create a container when no container of that name and type exists'() {
        given:
        ContainerType palletType = new ContainerType(name: 'Pallet')
        ContainerType boxType = new ContainerType(name: 'Box')
        Shipment shipment = buildShipment([])
        Container pallet = shipment.addNewContainer('Unit 1', palletType)

        when:
        Container found = shipment.findOrCreateContainer('UNIT 1', palletType)
        Container created = shipment.findOrCreateContainer('Unit 1', boxType)

        then:
        assert found == pallet
        assert created != pallet
        assert created.name == 'Unit 1'
        assert created.containerType == boxType
        assert shipment.containers.size() == 2
    }

    void 'addNewPallet, addNewBox and findOrCreatePallet should use the standard pallet and box container types'() {
        given:
        ContainerType palletType = saveContainerType(Constants.PALLET_CONTAINER_TYPE_ID, 'Pallet')
        ContainerType boxType = saveContainerType(Constants.BOX_CONTAINER_TYPE_ID, 'Box')
        Shipment shipment = buildShipment([])

        when:
        Container pallet = shipment.addNewPallet('Pallet 1')
        Container box = shipment.addNewBox('Box 1')

        then:
        assert pallet.containerType == palletType
        assert pallet.name == 'Pallet 1'
        assert box.containerType == boxType
        assert box.name == 'Box 1'

        and: 'an existing pallet is reused and a missing one is created'
        assert shipment.findOrCreatePallet('pallet 1') == pallet
        assert shipment.findOrCreatePallet('Pallet 2').containerType == palletType
        assert shipment.containers.size() == 3
    }

    void 'containersByType should group the containers by the name of their type'() {
        given:
        ContainerType palletType = new ContainerType(name: 'Pallet')
        ContainerType boxType = new ContainerType(name: 'Box')
        Shipment shipment = buildShipment([])
        Container pallet = shipment.addNewContainer('Pallet 1', palletType)
        Container firstBox = shipment.addNewContainer('Box 1', boxType)
        Container secondBox = shipment.addNewContainer('Box 2', boxType)

        when:
        Map<String, List<Container>> containersByType = shipment.containersByType

        then:
        assert containersByType.keySet() == ['Pallet', 'Box'] as Set
        assert containersByType['Pallet'] == [pallet]
        assert containersByType['Box'] as Set == [firstBox, secondBox] as Set
    }

    void 'the total weight should add up the top-level containers, using the children when a container has no weight'() {
        given:
        Shipment shipment = buildShipment([])
        Container weighed = new Container(name: 'weighed', weight: 10, weightUnits: units)
        Container nested = new Container(name: 'nested', weight: 99, weightUnits: units, parentContainer: weighed)
        Container unweighed = new Container(name: 'unweighed', weightUnits: units)
        Container firstChild = new Container(name: 'child 1', weight: 3, weightUnits: units, parentContainer: unweighed)
        Container secondChild = new Container(name: 'child 2', weight: 2, weightUnits: units, parentContainer: unweighed)
        unweighed.containers = [firstChild, secondChild] as Set
        weighed.containers = [nested] as Set
        shipment.containers = [weighed, nested, unweighed, firstChild, secondChild] as Set

        expect: 'the nested container is not counted again on top of its weighed parent'
        assert shipment.findAllParentContainers() as Set == [weighed, unweighed] as Set
        assert totalWeight(shipment) == 15F

        where:
        units | totalWeight
        'kg'  | { Shipment weighedShipment -> weighedShipment.totalWeightInKilograms() }
        'lbs' | { Shipment weighedShipment -> weighedShipment.totalWeightInPounds() }
    }

    void 'calculateTotalValue should price the lines whose product has a unit price'() {
        given:
        Shipment shipment = buildShipment([])
        shipment.addToShipmentItems(new ShipmentItem(quantity: 4, product: new Product(name: 'A', pricePerUnit: 2.5)))
        shipment.addToShipmentItems(new ShipmentItem(quantity: 2, product: new Product(name: 'B', pricePerUnit: 1.25)))
        shipment.addToShipmentItems(new ShipmentItem(quantity: 7, product: new Product(name: 'C')))

        expect:
        assert shipment.calculateTotalValue() == 12.5F
    }

    void 'calculateTotalValue should be zero when no line has a priced product'() {
        given:
        Shipment shipment = buildShipment([])
        shipment.addToShipmentItems(new ShipmentItem(quantity: 7, product: new Product(name: 'C')))

        expect:
        assert shipment.calculateTotalValue() == 0
    }

    void 'recipients should collect each recipient with an email from the containers, lines and shipment'() {
        given:
        Person containerRecipient = new Person(firstName: 'Ann', lastName: 'Lee', email: 'ann@example.com')
        Person itemRecipient = new Person(firstName: 'Bob', lastName: 'Ray', email: 'bob@example.com')
        Person shipmentRecipient = new Person(firstName: 'Cy', lastName: 'Fox', email: 'cy@example.com')
        Person withoutEmail = new Person(firstName: 'Dee', lastName: 'Ng')
        Shipment shipment = buildShipment([])
        shipment.recipient = shipmentRecipient
        shipment.containers = [
                new Container(name: 'box 1', recipient: containerRecipient),
                new Container(name: 'box 2', recipient: withoutEmail),
                new Container(name: 'box 3'),
        ] as Set
        shipment.addToShipmentItems(new ShipmentItem(quantity: 1, recipient: itemRecipient))
        shipment.addToShipmentItems(new ShipmentItem(quantity: 1, recipient: containerRecipient))
        shipment.addToShipmentItems(new ShipmentItem(quantity: 1))

        expect:
        assert shipment.recipients as Set == [containerRecipient, itemRecipient, shipmentRecipient] as Set
        assert shipment.recipients.size() == 3
    }

    void 'the addresses should come from the location, or from its location group when it has none'() {
        given:
        origin.address = new Address(description: 'Origin street 1')
        destination.locationGroup = new LocationGroup(name: 'Site', address: new Address(description: 'Site road 2'))
        Shipment shipment = buildShipment([])

        expect:
        assert shipment.consignorAddress == 'Origin street 1'
        assert shipment.consigneeAddress == 'Site road 2'
        assert new Shipment(origin: new Location(name: 'Bare')).consignorAddress == null
    }

    void 'getReferenceNumber should find the reference number of the named type'() {
        given:
        ReferenceNumberType billOfLading = new ReferenceNumberType(name: 'Bill of lading').save(validate: false)
        ReferenceNumberType customs = new ReferenceNumberType(name: 'Customs').save(validate: false)
        new ReferenceNumberType(name: 'Unused').save(validate: false, flush: true)
        ReferenceNumber billOfLadingNumber = new ReferenceNumber(identifier: 'BOL-1', referenceNumberType: billOfLading)
        Shipment shipment = buildShipment([])
        shipment.referenceNumbers = [
                new ReferenceNumber(identifier: 'C-1', referenceNumberType: customs),
                billOfLadingNumber,
        ]

        expect:
        assert shipment.getReferenceNumber('Bill of lading') == billOfLadingNumber
        assert shipment.getReferenceNumber('Unused') == null
        assert shipment.getReferenceNumber('Not a type') == null
    }

    void 'receipt should be the earliest receipt of the shipment'() {
        given:
        // dateCreated is not bindable through the map constructor
        Receipt later = new Receipt()
        later.dateCreated = date('2024-01-09T08:00:00Z')
        Receipt earlier = new Receipt()
        earlier.dateCreated = date('2024-01-08T08:00:00Z')
        Shipment shipment = buildShipment([])

        expect:
        assert shipment.receipt == null

        when:
        shipment.receipts = new TreeSet([later, earlier])

        then:
        assert shipment.receipt == earlier
    }

    void 'hasInvoicedItem should be #expected when the invoiced quantities are #invoicedQuantities'() {
        given:
        Shipment shipment = buildShipment([])
        invoicedQuantities.each { Integer quantity ->
            ShipmentItem shipmentItem = new ShipmentItem(quantity: 10)
            shipmentItem.invoiceItems = [new InvoiceItem(quantity: quantity)] as Set
            shipment.addToShipmentItems(shipmentItem)
        }

        expect:
        assert shipment.hasInvoicedItem() == expected

        where:
        invoicedQuantities || expected
        [0, 3]             || true
        [0, 0]             || false
    }

    void 'hasParentContainer should be #parent and hasChildContainer #child when the lines are #description'() {
        given:
        Container pallet = new Container(name: 'pallet')
        Container box = new Container(name: 'box', parentContainer: pallet)
        Shipment shipment = buildShipment([])
        shipment.addToShipmentItems(new ShipmentItem(quantity: 1))
        if (container) {
            shipment.addToShipmentItems(new ShipmentItem(quantity: 1, container: [pallet: pallet, box: box][container]))
        }

        expect:
        assert shipment.hasParentContainer() == parent
        assert shipment.hasChildContainer() == child

        where:
        container || parent | child | description
        null      || false  | false | 'loose'
        'pallet'  || false  | true  | 'packed in a top-level container'
        'box'     || true   | true  | 'packed in a container inside another container'
    }

    void 'referenceDocument should link to the stock movement of #description'() {
        given:
        Shipment shipment = buildShipment([])
        shipment.id = 'shipment-1'
        shipment.shipmentNumber = 'S-100'
        shipment.description = 'Monthly resupply'
        if (requisitionId) {
            shipment.requisition = new Requisition(name: 'request')
            shipment.requisition.id = requisitionId
        }

        when:
        ReferenceDocument document = shipment.referenceDocument

        then:
        assert document.url == expectedUrl
        assert document.id == 'shipment-1'
        assert document.label == 'S-100'
        assert document.identifier == 'S-100'
        assert document.name == 'Test shipment'
        assert document.description == 'Monthly resupply'

        where:
        requisitionId   || expectedUrl                                    | description
        'requisition-1' || '/openboxes/stockMovement/show/requisition-1'  | 'its request when it has one'
        null            || '/openboxes/stockMovement/show/shipment-1'     | 'the shipment itself otherwise'
    }

    void 'origin should be rejected when it is also the destination'() {
        given:
        Shipment sameLocation = buildShipment([])
        sameLocation.destination = origin

        expect:
        assert !sameLocation.validate(['origin'])
        assert sameLocation.errors.getFieldError('origin').code == 'validator.invalid'
        assert buildShipment([]).validate(['origin'])
    }

    void 'expectedShippingDate should be #description'() {
        given:
        Shipment shipment = buildShipment([])
        shipment.expectedShippingDate = date('2024-01-10T12:00:00Z')
        shipment.expectedDeliveryDate = deliveryDate ? date(deliveryDate) : null

        expect:
        assert shipment.validate(['expectedShippingDate']) == expected

        where:
        deliveryDate           || expected | description
        null                   || true     | 'accepted without an expected delivery date'
        '2024-01-12T12:00:00Z' || true     | 'accepted before the expected delivery date'
        '2024-01-10T06:00:00Z' || true     | 'accepted later on the expected delivery day'
        '2024-01-08T12:00:00Z' || false    | 'rejected days after the expected delivery date'
    }

    void 'referenceNumbers should be rejected when two of them share a type'() {
        given:
        ReferenceNumberType billOfLading = new ReferenceNumberType(name: 'Bill of lading')
        billOfLading.id = 'type-1'
        ReferenceNumberType customs = new ReferenceNumberType(name: 'Customs')
        customs.id = 'type-2'
        Shipment shipment = buildShipment([])
        shipment.referenceNumbers = [
                new ReferenceNumber(identifier: 'BOL-1', referenceNumberType: billOfLading),
                new ReferenceNumber(identifier: 'C-1', referenceNumberType: customs),
        ]

        expect:
        assert shipment.validate(['referenceNumbers'])

        when:
        shipment.referenceNumbers << new ReferenceNumber(identifier: 'BOL-2', referenceNumberType: billOfLading)

        then:
        assert !shipment.validate(['referenceNumbers'])
    }

    void 'the line and container queries should find the lines of the saved shipment'() {
        given:
        ContainerType boxType = new ContainerType(name: 'Box').save(validate: false)
        Person recipient = new Person(firstName: 'Ann', lastName: 'Lee').save(validate: false)
        Shipment shipment = buildSavedShipment()
        Container pallet = shipment.addNewContainer('Pallet 1', boxType)
        Container box = pallet.addNewContainer(boxType)
        box.name = 'Box 1'
        Product product = new Product(name: 'product').save(validate: false)
        InventoryItem lot = new InventoryItem(product: product, lotNumber: 'LOT-1').save(validate: false)
        ShipmentItem loose = new ShipmentItem(product: product, inventoryItem: lot, quantity: 1)
        ShipmentItem looseForRecipient = new ShipmentItem(
                product: product, inventoryItem: lot, quantity: 2, recipient: recipient)
        ShipmentItem inPallet = new ShipmentItem(product: product, inventoryItem: lot, quantity: 3, container: pallet)
        [loose, looseForRecipient, inPallet].each { shipment.addToShipmentItems(it) }
        shipment.save(failOnError: true, flush: true)

        expect:
        assert shipment.countShipmentItems() == 3
        assert shipment.countShipmentItemsByContainer(pallet) == 1
        assert shipment.countShipmentItemsByContainer(box) == 0
        assert shipment.findShipmentItemsByContainer(pallet)*.id == [inPallet.id]
        assert shipment.findAllChildContainers(pallet)*.id == [box.id]
        assert shipment.findShipmentItem(lot, pallet, null).id == inPallet.id
        assert shipment.findShipmentItem(lot, null, recipient).id == looseForRecipient.id
        assert shipment.unpackedShipmentItems*.id as Set == [loose.id, looseForRecipient.id] as Set
    }

    void 'allShipmentItems should list the loose lines and the lines packed in top-level containers'() {
        given:
        ContainerType boxType = new ContainerType(name: 'Box').save(validate: false)
        Shipment shipment = buildSavedShipment()
        Container firstBox = shipment.addNewContainer('Box 1', boxType)
        Container secondBox = shipment.addNewContainer('Box 2', boxType)
        Product product = new Product(name: 'product').save(validate: false)
        ShipmentItem loose = new ShipmentItem(product: product, quantity: 1)
        ShipmentItem inFirstBox = new ShipmentItem(product: product, quantity: 2, container: firstBox)
        ShipmentItem inSecondBox = new ShipmentItem(product: product, quantity: 3, container: secondBox)
        [loose, inFirstBox, inSecondBox].each { shipment.addToShipmentItems(it) }
        shipment.save(failOnError: true, flush: true)

        when:
        Collection<ShipmentItem> allShipmentItems = shipment.allShipmentItems

        then:
        assert allShipmentItems.size() == 3
        assert allShipmentItems*.id as Set == [loose.id, inFirstBox.id, inSecondBox.id] as Set
    }

    @PendingFeature(reason = 'OBAF lists a child container made by addNewContainer twice')
    void 'allShipmentItems should list a line in a nested container once'() {
        given:
        ContainerType palletType = new ContainerType(name: 'Pallet').save(validate: false)
        ContainerType boxType = new ContainerType(name: 'Box').save(validate: false)
        Shipment shipment = buildSavedShipment()
        Container pallet = shipment.addNewContainer('Pallet 1', palletType)
        Container box = pallet.addNewContainer(boxType)
        box.name = 'Box 1'
        Product product = new Product(name: 'product').save(validate: false)
        ShipmentItem inBox = new ShipmentItem(product: product, quantity: 2, container: box)
        shipment.addToShipmentItems(inBox)
        shipment.save(failOnError: true, flush: true)

        expect:
        assert shipment.allShipmentItems*.id == [inBox.id]
    }

    @PendingFeature(reason = 'OBAF has POUNDS_PER_KILOGRAM and KILOGRAMS_PER_POUND swapped')
    void 'the total weight should convert between pounds and kilograms'() {
        given:
        Shipment shipment = buildShipment([])
        shipment.containers = [new Container(name: 'box', weight: 10, weightUnits: units)] as Set

        expect:
        Math.abs(shipment.totalWeightInKilograms() - kilograms) < 0.01
        Math.abs(shipment.totalWeightInPounds() - pounds) < 0.01

        where:
        units | kilograms | pounds
        'lbs' | 4.536     | 10
        'kg'  | 10        | 22.046
    }

    private Shipment buildShipment(List<Event> events) {
        Shipment shipment = new Shipment(
                name: 'Test shipment',
                origin: origin,
                destination: destination,
                expectedShippingDate: date('2024-01-01T00:00:00Z'),
                shipmentType: new ShipmentType(name: 'Default'),
        )
        // The domain declares the events as a SortedSet, newest first
        shipment.events = new TreeSet(events)
        return shipment
    }

    private Shipment buildSavedShipment() {
        Shipment shipment = buildShipment([])
        return shipment.save(failOnError: true, flush: true)
    }

    private static Event buildEvent(EventCode eventCode, String isoInstant) {
        return new Event(eventType: new EventType(name: eventCode.name(), eventCode: eventCode),
                eventDate: date(isoInstant))
    }

    private static Order buildOrder(String kind) {
        OrderType orderType
        switch (kind) {
            case 'purchase':
                orderType = new OrderType(orderTypeCode: OrderTypeCode.PURCHASE_ORDER)
                break
            case 'transfer':
                orderType = new OrderType(orderTypeCode: OrderTypeCode.TRANSFER_ORDER)
                break
            case 'return':
                orderType = new OrderType(code: Constants.RETURN_ORDER, orderTypeCode: OrderTypeCode.TRANSFER_ORDER)
                break
            case 'putaway':
                orderType = new OrderType(code: Constants.PUTAWAY_ORDER, orderTypeCode: OrderTypeCode.TRANSFER_ORDER)
                break
        }
        return new Order(name: kind, orderType: orderType)
    }

    private static ShipmentItem buildItem(Product product, String lotNumber, Integer quantity, Container container) {
        return new ShipmentItem(
                inventoryItem: new InventoryItem(product: product, lotNumber: lotNumber),
                product: product,
                quantity: quantity,
                container: container,
        )
    }

    private static ContainerType saveContainerType(String id, String name) {
        ContainerType containerType = new ContainerType(name: name)
        containerType.id = id
        return containerType.save(validate: false, flush: true)
    }

    private static Date date(String isoInstant) {
        return Date.from(Instant.parse(isoInstant))
    }

    private static long hours(int count) {
        return count * 60L * 60L * 1000L
    }
}
