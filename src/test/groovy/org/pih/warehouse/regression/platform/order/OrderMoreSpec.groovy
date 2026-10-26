// Companion spec: extra features for upstream's OrderSpec (the upstream file stays authoritative).
package org.pih.warehouse.regression.platform.order

import org.pih.warehouse.order.*
import grails.testing.gorm.DomainUnitTest
import grails.util.Holders
import org.pih.warehouse.api.StockMovementDirection
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.PaymentMethodType
import org.pih.warehouse.core.PaymentTerm
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.ReferenceDocument
import org.pih.warehouse.invoice.Invoice
import org.pih.warehouse.invoice.InvoiceItem
import org.pih.warehouse.invoice.InvoiceType
import org.pih.warehouse.invoice.InvoiceTypeCode
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentStatusCode
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class OrderMoreSpec extends Specification implements DomainUnitTest<Order> {

    private static OrderItem orderItem(Map args) {
        OrderItem orderItem = new OrderItem(args)
        orderItem.id = args.id
        orderItem.orderAdjustments = [] as Set
        return orderItem
    }

    private static Invoice invoice(String id, InvoiceTypeCode code) {
        Invoice invoice = new Invoice(invoiceNumber: id, invoiceType: code ? new InvoiceType(code: code) : null)
        invoice.id = id
        return invoice
    }

    private static Location location(String name, LocationTypeCode locationTypeCode = LocationTypeCode.DEPOT) {
        return new Location(name: name, locationType: new LocationType(name: locationTypeCode.name(), locationTypeCode: locationTypeCode))
    }

    void 'Order status predicates should return #expected for status #status'() {
        given:
        Order order = new Order(status: status)

        expect:
        assert [
                pending           : order.isPending(),
                placed            : order.isPlaced(),
                partiallyReceived : order.isPartiallyReceived(),
                received          : order.isReceived(),
                completed         : order.isCompleted(),
                canceled          : order.isCanceled(),
        ].findAll { it.value }.keySet() as List == expected

        where:
        status                         || expected
        null                           || ['pending']
        OrderStatus.PENDING            || ['pending']
        OrderStatus.APPROVED           || []
        OrderStatus.PLACED             || ['placed']
        OrderStatus.PARTIALLY_RECEIVED || ['partiallyReceived']
        OrderStatus.RECEIVED           || ['received']
        OrderStatus.COMPLETED          || ['completed']
        OrderStatus.CANCELED           || ['canceled']
    }

    void 'Order.updateStatus() should return #expectedStatus when items are completely received: #completelyReceived and partially received: #partiallyReceived'() {
        given:
        Order order = new Order(status: initialStatus)
        order.orderItems = [completelyReceived, partiallyReceived].transpose().collect { List flags ->
            Stub(OrderItem) {
                isCompletelyReceived() >> flags[0]
                isPartiallyReceived() >> flags[1]
            }
        } as Set

        when:
        OrderStatus status = order.updateStatus()

        then:
        assert status == expectedStatus
        assert order.status == expectedStatus

        where:
        initialStatus       | completelyReceived | partiallyReceived || expectedStatus
        OrderStatus.PLACED  | [true, true]       | [false, false]    || OrderStatus.RECEIVED
        OrderStatus.PLACED  | [true, false]      | [false, false]    || OrderStatus.PLACED
        OrderStatus.PLACED  | [true, false]      | [false, true]     || OrderStatus.PARTIALLY_RECEIVED
        null                | [false]            | [false]           || OrderStatus.PENDING
        null                | []                 | []                || OrderStatus.PENDING
        OrderStatus.PLACED  | []                 | []                || OrderStatus.PLACED
    }

    void 'Order.getStockMovementDirection() should return #expected for the #current location'() {
        given:
        Map<String, Location> locations = [
                depot   : location('depot'),
                ward    : location('ward', LocationTypeCode.WARD),
                supplier: location('supplier', LocationTypeCode.SUPPLIER),
                other   : location('other'),
        ]
        Order order = new Order(origin: locations[origin], destination: locations[destination])

        expect:
        assert order.getStockMovementDirection(locations[current]) == expected

        where:
        origin     | destination | current  || expected
        'depot'    | 'depot'     | 'depot'  || StockMovementDirection.INTERNAL
        'depot'    | 'ward'      | 'depot'  || StockMovementDirection.OUTBOUND
        'depot'    | 'ward'      | 'ward'   || StockMovementDirection.INBOUND
        'supplier' | 'ward'      | 'other'  || StockMovementDirection.INBOUND
        'depot'    | 'ward'      | 'other'  || null
    }

    void 'Order.isInbound() and Order.isOutbound() should only apply to return orders'() {
        given:
        Location depot = location('depot')
        Location ward = location('ward', LocationTypeCode.WARD)
        Order order = new Order(origin: depot, destination: ward, orderType: new OrderType(code: orderTypeCode))

        expect:
        assert order.isOutbound(depot) == outboundFromDepot
        assert order.isInbound(ward) == inboundToWard
        assert !order.isInbound(depot)
        assert !order.isOutbound(ward)

        where:
        orderTypeCode          || outboundFromDepot | inboundToWard
        Constants.RETURN_ORDER || true              | true
        'OTHER'                || false             | false
    }

    void 'Order type predicates should be derived from the order type'() {
        given:
        Order order = new Order(orderType: orderType)

        expect:
        assert order.isPurchaseOrder == purchase
        assert order.isTransferOrder == transfer
        assert order.isReturnOrder == returnOrder
        assert order.isPutawayOrder == putaway

        where:
        orderType                                                    || purchase | transfer | returnOrder | putaway
        new OrderType(orderTypeCode: OrderTypeCode.PURCHASE_ORDER)   || true     | false    | false       | false
        new OrderType(orderTypeCode: OrderTypeCode.TRANSFER_ORDER)   || false    | true     | false       | false
        new OrderType(code: Constants.RETURN_ORDER)                  || false    | false    | true        | false
        new OrderType(code: Constants.PUTAWAY_ORDER)                 || false    | false    | false       | true
        null                                                         || null     | null     | null        | null
    }

    void 'Order.listOrderItems() should skip canceled items and sort by category, product name and id'() {
        given:
        Category drugs = new Category(name: 'Drugs')
        Category supplies = new Category(name: 'Supplies')
        OrderItem syringe = orderItem(id: '1', product: new Product(name: 'Syringe', category: supplies))
        OrderItem aspirinB = orderItem(id: 'b', product: new Product(name: 'Aspirin', category: drugs))
        OrderItem aspirinA = orderItem(id: 'a', product: new Product(name: 'Aspirin', category: drugs))
        OrderItem bandage = orderItem(id: '2', product: new Product(name: 'Bandage', category: drugs))
        OrderItem canceled = orderItem(id: '0', product: new Product(name: 'Aspirin', category: drugs),
                orderItemStatusCode: OrderItemStatusCode.CANCELED)
        Order order = new Order()
        order.orderItems = [syringe, aspirinB, canceled, bandage, aspirinA] as Set

        expect:
        assert order.listOrderItems()*.id == ['a', 'b', '2', '1']
        assert order.activeOrderItems*.id as Set == ['a', 'b', '2', '1'] as Set
    }

    void 'Order.listOrderItems() should return an empty list when there are no order items'() {
        expect:
        assert new Order().listOrderItems() == []
    }

    void 'Order totals should combine item subtotals, item adjustments and order adjustments'() {
        given:
        OrderItem first = orderItem(id: '1', quantity: 2, unitPrice: 10.00)
        first.orderAdjustments = [
                new OrderAdjustment(amount: 1.00),
                new OrderAdjustment(amount: 100.00, canceled: true),
        ] as Set
        OrderItem second = orderItem(id: '2', quantity: 3, unitPrice: 5.00)
        OrderItem canceledItem = orderItem(id: '3', quantity: 1, unitPrice: 1000.00, orderItemStatusCode: OrderItemStatusCode.CANCELED)
        Order order = new Order()
        order.orderItems = [first, second, canceledItem] as Set
        order.orderAdjustments = [
                new OrderAdjustment(amount: 5.00),
                new OrderAdjustment(percentage: 10),
                new OrderAdjustment(amount: 50.00, canceled: true),
                new OrderAdjustment(amount: 1.00, orderItem: first),
        ] as Set

        expect:
        assert order.subtotal == 35.00
        assert order.totalOrderItemAdjustments == 1.00
        assert order.totalOrderAdjustments == 8.50
        assert order.totalAdjustments == 9.50
        assert order.total == 44.50
        assert order.totalPrice() == 44.50
    }

    void 'Order totals should be zero for an order without items and adjustments'() {
        given:
        Order order = new Order()

        expect:
        assert order.subtotal == 0
        assert order.totalAdjustments == 0
        assert order.total == 0
    }

    void 'Order.getTotalNormalized() should apply the fixed exchange rate of the order'() {
        given:
        Order order = new Order(exchangeRate: 2.5)
        order.orderItems = [orderItem(id: '1', quantity: 4, unitPrice: 10.00)] as Set

        expect:
        assert order.lookupCurrentExchangeRate() == 2.5
        assert order.totalNormalized == 100.00
    }

    void 'Order.lookupCurrentExchangeRate() should return 1 for an order in the default currency'() {
        given:
        Order order = new Order(currencyCode: Holders.grailsApplication.config.openboxes.locale.defaultCurrencyCode)

        expect:
        assert order.lookupCurrentExchangeRate() == 1.0
    }

    void 'Order.generateName() should append the completion date and the person who completed the order'() {
        given:
        Locale defaultLocale = Locale.default
        Locale.setDefault(Locale.US)
        Order order = new Order(orderNumber: 'PO-1')
        Order completedOrder = new Order(
                orderNumber: 'PO-2',
                dateCompleted: new GregorianCalendar(2026, Calendar.MARCH, 5).time,
                completedBy: new Person(firstName: 'Ann', lastName: 'Smith'),
        )

        expect:
        assert order.generateName() == 'PO-1'
        assert completedOrder.generateName() == 'PO-2 - March 5, 2026 - Ann Smith'

        cleanup:
        Locale.setDefault(defaultLocale)
    }

    void 'Order.getPrepaymentPercent() should return #expected for prepayment percent #prepaymentPercent'() {
        given:
        Order order = new Order(paymentTerm: hasPaymentTerm ? new PaymentTerm(prepaymentPercent: prepaymentPercent) : null)

        expect:
        assert order.prepaymentPercent == expected
        assert order.isPrepaymentRequired == prepaymentRequired

        where:
        hasPaymentTerm | prepaymentPercent || expected | prepaymentRequired
        true           | 30                || 0.3      | true
        true           | 0                 || 1        | false
        true           | null              || 1        | false
        false          | null              || 1        | false
    }

    void 'Order invoice helpers should collect the invoices of order items and order adjustments'() {
        given:
        Invoice prepayment = invoice('prepayment', InvoiceTypeCode.PREPAYMENT_INVOICE)
        Invoice regular = invoice('regular', InvoiceTypeCode.INVOICE)
        OrderItem item = orderItem(id: '1', quantity: 1)
        item.invoiceItems = [new InvoiceItem(invoice: prepayment)] as Set
        OrderAdjustment adjustment = new OrderAdjustment(amount: 1)
        adjustment.invoiceItems = [new InvoiceItem(invoice: prepayment), new InvoiceItem(invoice: regular)] as Set
        Order order = new Order()
        order.orderItems = [item] as Set
        order.orderAdjustments = [adjustment] as Set

        expect:
        assert order.invoiceItems.size() == 3
        assert order.invoices as Set == [prepayment, regular] as Set
        assert order.invoices.size() == 2
        assert order.hasInvoice
        assert order.hasPrepaymentInvoice
        assert order.hasRegularInvoice
    }

    void 'Order invoice helpers should report no invoices for an order without invoice items'() {
        given:
        Order order = new Order()
        order.orderItems = [orderItem(id: '1', quantity: 1)] as Set

        expect:
        assert order.invoiceItems == []
        assert !order.hasInvoice
        assert !order.hasPrepaymentInvoice
        assert !order.hasRegularInvoice
    }

    void 'Order.getHasRegularInvoice() should treat an invoice without type as a regular invoice'() {
        given:
        OrderItem item = orderItem(id: '1', quantity: 1)
        item.invoiceItems = [new InvoiceItem(invoice: invoice('untyped', null))] as Set
        Order order = new Order()
        order.orderItems = [item] as Set

        expect:
        assert order.hasRegularInvoice
        assert !order.hasPrepaymentInvoice
    }

    void 'Order.getSortedInvoiceItems() should sort by newest invoice first, then invoice number, inverse flag and id'() {
        given:
        Invoice older = invoice('A-older', InvoiceTypeCode.INVOICE)
        older.dateCreated = new Date(1000L)
        Invoice newerB = invoice('B-newer', InvoiceTypeCode.INVOICE)
        newerB.dateCreated = new Date(2000L)
        Invoice newerA = invoice('A-newer', InvoiceTypeCode.INVOICE)
        newerA.dateCreated = new Date(2000L)
        InvoiceItem olderItem = new InvoiceItem(invoice: older)
        olderItem.id = '1'
        InvoiceItem newerBItem = new InvoiceItem(invoice: newerB)
        newerBItem.id = '2'
        InvoiceItem newerAInverse = new InvoiceItem(invoice: newerA, inverse: true)
        newerAInverse.id = '3'
        InvoiceItem newerAItem2 = new InvoiceItem(invoice: newerA, inverse: false)
        newerAItem2.id = '5'
        InvoiceItem newerAItem1 = new InvoiceItem(invoice: newerA, inverse: false)
        newerAItem1.id = '4'
        OrderItem item = orderItem(id: '1', quantity: 1)
        item.invoiceItems = [olderItem, newerBItem, newerAInverse, newerAItem2, newerAItem1] as Set
        Order order = new Order()
        order.orderItems = [item] as Set

        expect:
        assert order.sortedInvoiceItems*.id == ['4', '5', '3', '2', '1']
    }

    void 'Order.getIsPrepaymentInvoiceAllowed() should return #expected when hasInvoice: #hasInvoice, prepayment required: #prepaymentRequired and active items: #hasActiveItems'() {
        given:
        Order order = Spy(Order) {
            getHasInvoice() >> hasInvoice
            getIsPrepaymentRequired() >> prepaymentRequired
            getHasActiveItemsOrAdjustments() >> hasActiveItems
        }

        expect:
        assert order.isPrepaymentInvoiceAllowed == expected

        where:
        hasInvoice | prepaymentRequired | hasActiveItems || expected
        false      | true               | true           || true
        true       | true               | true           || false
        false      | false              | true           || false
        false      | true               | false          || false
    }

    void 'Order.getHasActiveItemsOrAdjustments() should ignore canceled items and adjustments'() {
        given:
        Order order = new Order()
        order.orderItems = items as Set
        order.orderAdjustments = adjustments as Set

        expect:
        assert order.hasActiveItemsOrAdjustments == expected

        where:
        items                                                                    | adjustments                                         || expected
        []                                                                       | []                                                  || false
        [new OrderItem(orderItemStatusCode: OrderItemStatusCode.CANCELED)]       | [new OrderAdjustment(amount: 1, canceled: true)]    || false
        [new OrderItem(orderItemStatusCode: OrderItemStatusCode.PENDING)]        | []                                                  || true
        []                                                                       | [new OrderAdjustment(amount: 1)]                    || true
    }

    void 'Order.isShipped() should return true only when every active item is completely fulfilled'() {
        given:
        Order order = new Order()
        order.orderItems = fulfilled.collect { Boolean isFulfilled ->
            Stub(OrderItem) {
                getOrderItemStatusCode() >> OrderItemStatusCode.PENDING
                isCompletelyFulfilled() >> isFulfilled
            }
        } as Set
        order.orderItems.add(Stub(OrderItem) {
            getOrderItemStatusCode() >> OrderItemStatusCode.CANCELED
            isCompletelyFulfilled() >> false
        })

        expect:
        assert order.isShipped() == expected

        where:
        fulfilled     || expected
        [true, true]  || true
        [true, false] || false
    }

    void 'Order item status filters should select ordered, shipped, received and invoiced items'() {
        given:
        Order order = new Order(status: OrderStatus.PLACED)
        OrderItem shipped = Stub(OrderItem) {
            getOrder() >> order
            getOrderItemStatusCode() >> OrderItemStatusCode.PENDING
            isCompletelyFulfilled() >> true
            isCompletelyReceived() >> true
            isCompletelyInvoiced() >> false
        }
        OrderItem invoiced = Stub(OrderItem) {
            getOrder() >> order
            getOrderItemStatusCode() >> OrderItemStatusCode.PENDING
            isCompletelyFulfilled() >> false
            isCompletelyReceived() >> false
            isCompletelyInvoiced() >> true
        }
        OrderItem canceled = Stub(OrderItem) {
            getOrder() >> order
            getOrderItemStatusCode() >> OrderItemStatusCode.CANCELED
        }
        order.orderItems = [shipped, invoiced, canceled] as Set

        expect:
        assert order.orderedOrderItems as Set == [shipped, invoiced] as Set
        assert order.shippedOrderItems as Set == [shipped] as Set
        assert order.receivedOrderItems as Set == [shipped] as Set
        assert order.invoicedOrderItems as Set == [invoiced] as Set
    }

    void 'Order.getOrderedOrderItems() should be empty while the order is not placed yet'() {
        given:
        Order order = new Order(status: OrderStatus.PENDING)
        order.orderItems = [new OrderItem(order: order, orderItemStatusCode: OrderItemStatusCode.PENDING)] as Set

        expect:
        assert order.orderedOrderItems.isEmpty()
    }

    void 'Order.getShipments() should return the distinct saved shipments of all order items'() {
        given:
        Shipment first = new Shipment(name: 'first')
        first.id = 's1'
        Shipment second = new Shipment(name: 'second')
        second.id = 's2'
        Shipment unsaved = new Shipment(name: 'unsaved')
        Order order = new Order()
        order.orderItems = [
                Stub(OrderItem) { listShipments() >> [first, unsaved] },
                Stub(OrderItem) { listShipments() >> [first, second] },
        ] as Set

        expect:
        assert order.shipments.size() == 2
        assert order.shipments as Set == [first, second] as Set
    }

    void 'Order.getPendingShipments() should return #expectedPending for shipments in status #statuses'() {
        given:
        Order order = Spy(Order) {
            getShipments() >> statuses.collect { new Shipment(name: it.name(), currentStatus: it) }
        }

        expect:
        assert order.pendingShipments*.currentStatus == expectedPending
        assert order.getShipmentsByStatus(ShipmentStatusCode.SHIPPED)*.currentStatus == expectedShipped

        where:
        statuses                                                 || expectedPending                | expectedShipped
        [ShipmentStatusCode.PENDING, ShipmentStatusCode.SHIPPED] || [ShipmentStatusCode.PENDING]   | [ShipmentStatusCode.SHIPPED]
        [ShipmentStatusCode.SHIPPED]                             || null                           | [ShipmentStatusCode.SHIPPED]
        []                                                       || null                           | []
    }

    void 'Order.getDisplayStatus() should return #expected for shipment statuses #shipmentStatuses and regular invoice: #hasRegularInvoice'() {
        given:
        Order order = Spy(Order) {
            getShipments() >> shipmentStatuses.collect { new Shipment(currentStatus: it) }
            getHasRegularInvoice() >> hasRegularInvoice
        }
        order.status = OrderStatus.PLACED

        expect:
        assert order.displayStatus == expected

        where:
        shipmentStatuses                                               | hasRegularInvoice || expected
        []                                                             | false             || OrderStatus.PLACED
        [ShipmentStatusCode.PENDING]                                   | false             || OrderStatus.PLACED
        [ShipmentStatusCode.PENDING, ShipmentStatusCode.SHIPPED]       | false             || ShipmentStatusCode.SHIPPED
        [ShipmentStatusCode.RECEIVED, ShipmentStatusCode.SHIPPED]      | false             || ShipmentStatusCode.SHIPPED
        [ShipmentStatusCode.RECEIVED, ShipmentStatusCode.PARTIALLY_RECEIVED] | false       || ShipmentStatusCode.PARTIALLY_RECEIVED
        [ShipmentStatusCode.RECEIVED]                                  | false             || ShipmentStatusCode.RECEIVED
        [ShipmentStatusCode.RECEIVED]                                  | true              || OrderStatus.COMPLETED
        [ShipmentStatusCode.SHIPPED]                                   | true              || ShipmentStatusCode.SHIPPED
    }

    void 'Order.toJson() should summarize transfer orders by name and count only active items'() {
        given:
        Order order = new Order(
                orderNumber: 'TO-1',
                name: 'transfer',
                status: OrderStatus.PENDING,
                origin: location('origin'),
                destination: location('destination'),
                createdBy: new Person(firstName: 'Ann', lastName: 'Smith'),
        )
        order.id = 'order-1'
        order.orderItems = [
                new OrderItem(orderItemStatusCode: OrderItemStatusCode.PENDING),
                new OrderItem(orderItemStatusCode: OrderItemStatusCode.PENDING),
                new OrderItem(orderItemStatusCode: OrderItemStatusCode.CANCELED),
        ] as Set

        when:
        Map json = order.toJson(OrderTypeCode.TRANSFER_ORDER)

        then:
        assert json.id == 'order-1'
        assert json.status == 'PENDING'
        assert json.orderNumber == 'TO-1'
        assert json.createdBy == 'Ann Smith'
        assert json.origin == 'origin'
        assert json.destination == 'destination'
        assert json.orderItemsCount == 2
    }

    void 'Order.toJson() should use the default representation for other order types'() {
        given:
        Location origin = location('origin')
        Order order = new Order(orderNumber: 'PO-1', name: 'purchase', status: OrderStatus.PLACED, origin: origin,
                paymentMethodType: new PaymentMethodType(name: 'Cash'))

        when:
        Map json = order.toJson(OrderTypeCode.PURCHASE_ORDER)

        then:
        assert json == order.toJson()
        assert json.status == OrderStatus.PLACED
        assert json.origin.is(origin)
        assert json.paymentMethodType == [name: 'Cash']
    }

    void 'Order.getReferenceDocument() should reference the order by its order number'() {
        given:
        Order order = new Order(orderNumber: 'PO-1', name: 'purchase', description: 'description')
        order.id = 'order-1'

        when:
        ReferenceDocument document = order.referenceDocument

        then:
        assert document.label == 'PO-1'
        assert document.identifier == 'PO-1'
        assert document.url == '/openboxes/order/show/order-1'
        assert document.id == 'order-1'
        assert document.name == 'purchase'
        assert document.description == 'description'
    }

    void 'Order.isFullyInvoiceable() should ignore canceled items that cannot be on a regular invoice'() {
        given:
        OrderItem invoiceableItem = Stub(OrderItem) {
            canBeOnRegularInvoice() >> true
            isFullyInvoiced() >> false
            isInvoiceable() >> true
            getQuantityRemaining() >> 0
        }
        OrderItem canceledItem = Stub(OrderItem) {
            canBeOnRegularInvoice() >> false
            isInvoiceable() >> false
        }
        Order order = new Order()
        order.orderItems = [invoiceableItem, canceledItem] as Set

        expect:
        assert order.isFullyInvoiceable()
    }
}
