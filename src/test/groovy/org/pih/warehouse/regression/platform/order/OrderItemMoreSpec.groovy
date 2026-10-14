// Companion spec: extra features for upstream's OrderItemSpec (the upstream file stays authoritative).
package org.pih.warehouse.regression.platform.order

import org.pih.warehouse.order.*
import grails.testing.gorm.DomainUnitTest
import org.pih.warehouse.core.BudgetCode
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.invoice.Invoice
import org.pih.warehouse.invoice.InvoiceItem
import org.pih.warehouse.invoice.InvoiceType
import org.pih.warehouse.invoice.InvoiceTypeCode
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.receiving.ReceiptStatusCode
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentStatusCode
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class OrderItemMoreSpec extends Specification implements DomainUnitTest<OrderItem> {

    private static ShipmentItem shipmentItem(Map args) {
        Shipment shipment = args.shipment ?: new Shipment(name: args.status.name(), currentStatus: args.status)
        ShipmentItem shipmentItem = new ShipmentItem(shipment: shipment, quantity: args.quantity)
        shipmentItem.invoiceItems = args.invoiceItems ?: [] as Set
        if (args.received || args.canceled) {
            shipmentItem.receiptItems = [
                    new ReceiptItem(
                            receipt: new Receipt(receiptStatusCode: ReceiptStatusCode.RECEIVED),
                            quantityReceived: args.received ?: 0,
                            quantityCanceled: args.canceled ?: 0,
                    ),
                    new ReceiptItem(
                            receipt: new Receipt(receiptStatusCode: ReceiptStatusCode.PENDING),
                            quantityReceived: 1000,
                            quantityCanceled: 1000,
                    ),
            ] as Set
        }
        return shipmentItem
    }

    private static Invoice invoice(InvoiceTypeCode code, Date datePosted = null) {
        return new Invoice(invoiceType: code ? new InvoiceType(code: code) : null, datePosted: datePosted)
    }

    void 'OrderItem.getQuantityInStandardUom() should multiply the quantity by the quantity per unit of measure'() {
        given:
        OrderItem orderItem = new OrderItem(quantity: 3, quantityPerUom: 10)

        expect:
        orderItem.quantityInStandardUom == 30
    }

    void 'OrderItem shipment quantities should only count shipments that have been shipped and convert to the order unit of measure'() {
        given: 'an order item for 5 packs of 10'
        OrderItem orderItem = new OrderItem(quantity: 5, quantityPerUom: 10)
        orderItem.shipmentItems = [
                shipmentItem(status: ShipmentStatusCode.RECEIVED, quantity: 20, received: 10, canceled: 10),
                shipmentItem(status: ShipmentStatusCode.SHIPPED, quantity: 10),
                shipmentItem(status: ShipmentStatusCode.PENDING, quantity: 20),
        ] as Set

        expect:
        orderItem.quantityShippedInStandardUom == 30
        orderItem.quantityShipped == 3
        orderItem.quantityReceivedInStandardUom == 10
        orderItem.quantityReceived == 1
        orderItem.quantityCanceledInStandardUom == 10
        orderItem.quantityCanceled == 1
        orderItem.quantityInShipmentsInStandardUom == 50
        orderItem.quantityInShipments == 5
        orderItem.shippedShipmentItems.size() == 2
        orderItem.hasShippedItems()
        orderItem.hasShipmentAssociated()
    }

    void 'OrderItem shipment quantities should be zero when there are no shipments'() {
        given:
        OrderItem orderItem = new OrderItem(quantity: 5, quantityPerUom: 10)

        expect:
        orderItem.quantityShipped == 0
        orderItem.quantityReceived == 0
        orderItem.quantityCanceled == 0
        orderItem.quantityInShipments == 0
        !orderItem.hasShippedItems()
        !orderItem.hasShipmentAssociated()
    }

    void 'OrderItem.getQuantityRemaining() should return #expected when quantity is #quantity, shipped is #shipped and status is #status'() {
        given:
        OrderItem orderItem = Spy(OrderItem) {
            getQuantityShipped() >> shipped
        }
        orderItem.quantity = quantity
        orderItem.orderItemStatusCode = status

        expect:
        orderItem.quantityRemaining == expected

        where:
        quantity | shipped | status                         || expected
        10       | 4       | OrderItemStatusCode.PENDING    || 6
        10       | 10      | OrderItemStatusCode.PENDING    || 0
        10       | 12      | OrderItemStatusCode.PENDING    || 0
        10       | 0       | OrderItemStatusCode.CANCELED   || 0
    }

    void 'OrderItem.getQuantityRemainingToShip() should return #expected when quantity is #quantity and #inShipments are in shipments'() {
        given:
        OrderItem orderItem = Spy(OrderItem) {
            getQuantityInShipments() >> inShipments
        }
        orderItem.quantity = quantity

        expect:
        orderItem.quantityRemainingToShip == expected

        where:
        quantity | inShipments || expected
        10       | 0           || 10
        10       | 7           || 3
        10       | 15          || 0
    }

    void 'OrderItem.getQuantityRemainingToShip(shipment) should exclude the given shipment and work in the standard unit of measure'() {
        given:
        Shipment current = new Shipment(name: 'current')
        Shipment other = new Shipment(name: 'other')
        OrderItem orderItem = new OrderItem(quantity: 5, quantityPerUom: 10)
        orderItem.shipmentItems = [
                shipmentItem(shipment: current, quantity: 30),
                shipmentItem(shipment: other, quantity: otherQuantity),
        ] as Set

        expect:
        orderItem.getQuantityRemainingToShip(current) == expected

        where:
        otherQuantity || expected
        20            || 30
        50            || 0
        60            || 0
    }

    void 'OrderItem fulfillment predicates should reflect quantity #shipped shipped out of #quantity'() {
        given:
        OrderItem orderItem = Spy(OrderItem) {
            getQuantityShipped() >> shipped
        }
        orderItem.quantity = quantity

        expect:
        orderItem.partiallyFulfilled == partiallyFulfilled
        orderItem.completelyFulfilled == completelyFulfilled
        orderItem.pending == !completelyFulfilled

        where:
        quantity | shipped || partiallyFulfilled | completelyFulfilled
        10       | 0       || false              | false
        10       | 4       || true               | false
        10       | 10      || false              | true
        10       | 11      || false              | true
    }

    void 'OrderItem receiving predicates should count canceled quantities as received when #received received and #canceled canceled out of #quantity'() {
        given:
        OrderItem orderItem = Spy(OrderItem) {
            getQuantityReceived() >> received
            getQuantityCanceled() >> canceled
        }
        orderItem.quantity = quantity

        expect:
        orderItem.completelyReceived == completelyReceived
        orderItem.partiallyReceived == partiallyReceived

        where:
        quantity | received | canceled || completelyReceived | partiallyReceived
        10       | 0        | 0        || false              | false
        10       | 4        | 0        || false              | true
        10       | 4        | 6        || true               | false
        10       | 10       | 0        || true               | false
        10       | 0        | 10       || true               | false
    }

    void 'OrderItem.getQuantityInvoiced() should ignore prepayment invoices and inverse items while posted quantity also ignores unposted invoices'() {
        given:
        Date posted = new Date(0L)
        OrderItem orderItem = new OrderItem(quantity: 10, quantityPerUom: 5)
        orderItem.invoiceItems = [
                new InvoiceItem(quantity: 2, invoice: invoice(InvoiceTypeCode.INVOICE, posted)),
                new InvoiceItem(quantity: 3, invoice: invoice(InvoiceTypeCode.INVOICE)),
                new InvoiceItem(quantity: 4, invoice: invoice(InvoiceTypeCode.PREPAYMENT_INVOICE, posted)),
                new InvoiceItem(quantity: 1, invoice: invoice(InvoiceTypeCode.INVOICE, posted), inverse: true),
        ] as Set
        orderItem.shipmentItems = [
                shipmentItem(status: ShipmentStatusCode.SHIPPED, quantity: 10,
                        invoiceItems: [new InvoiceItem(quantity: 6, invoice: invoice(InvoiceTypeCode.INVOICE, posted))] as Set),
        ] as Set

        expect:
        orderItem.allInvoiceItems.size() == 5
        orderItem.quantityInvoiced == 11
        orderItem.quantityInvoicedInStandardUom == 55
        orderItem.postedQuantityInvoiced == 8
        orderItem.postedQuantityInvoicedInStandardUom == 40
    }

    void 'OrderItem.getAllInvoiceItems() should not count an invoice item linked both directly and through a shipment twice'() {
        given:
        InvoiceItem invoiceItem = new InvoiceItem(quantity: 2, invoice: invoice(InvoiceTypeCode.INVOICE))
        OrderItem orderItem = new OrderItem(quantity: 10)
        orderItem.invoiceItems = [invoiceItem] as Set
        orderItem.shipmentItems = [shipmentItem(status: ShipmentStatusCode.SHIPPED, quantity: 10, invoiceItems: [invoiceItem] as Set)] as Set

        expect:
        orderItem.allInvoiceItems == [invoiceItem]
        orderItem.quantityInvoiced == 2
    }

    void 'OrderItem invoice type helpers should detect prepayment and regular invoices'() {
        given:
        Invoice prepayment = invoice(InvoiceTypeCode.PREPAYMENT_INVOICE)
        Invoice regular = invoice(InvoiceTypeCode.INVOICE)
        Invoice untyped = invoice(null)
        OrderItem orderItem = new OrderItem(quantity: 1)
        orderItem.invoiceItems = invoices.collect { new InvoiceItem(quantity: 1, invoice: [prepayment: prepayment, regular: regular, untyped: untyped][it]) } as Set

        expect:
        orderItem.hasInvoices == !invoices.isEmpty()
        orderItem.hasPrepaymentInvoice == hasPrepaymentInvoice
        orderItem.hasRegularInvoice == hasRegularInvoice
        orderItem.regularInvoices.size() == regularInvoiceCount

        where:
        invoices                  || hasPrepaymentInvoice | hasRegularInvoice | regularInvoiceCount
        []                        || false                | false             | 0
        ['prepayment']            || true                 | false             | 0
        ['prepayment', 'regular'] || true                 | true              | 1
        ['untyped']               || false                | true              | 0
    }

    void 'OrderItem.isFullyInvoiced() should depend on a regular invoice for a canceled item'() {
        given:
        OrderItem orderItem = Spy(OrderItem) {
            getHasRegularInvoice() >> hasRegularInvoice
        }
        orderItem.orderItemStatusCode = OrderItemStatusCode.CANCELED

        expect:
        orderItem.isFullyInvoiced() == hasRegularInvoice

        where:
        hasRegularInvoice << [true, false]
    }

    void 'OrderItem.canBeOnRegularInvoice() should return #expected for status #status with prepayment invoice: #hasPrepaymentInvoice'() {
        given:
        OrderItem orderItem = Spy(OrderItem) {
            getHasPrepaymentInvoice() >> hasPrepaymentInvoice
        }
        orderItem.orderItemStatusCode = status

        expect:
        orderItem.canBeOnRegularInvoice() == expected

        where:
        status                       | hasPrepaymentInvoice || expected
        OrderItemStatusCode.PENDING  | false                || true
        OrderItemStatusCode.CANCELED | true                 || true
        OrderItemStatusCode.CANCELED | false                || false
    }

    void 'OrderItem.isInvoiceable() for a canceled item should return #expected with prepayment invoice: #hasPrepaymentInvoice and fully invoiced: #fullyInvoiced'() {
        given:
        OrderItem orderItem = Spy(OrderItem) {
            getHasPrepaymentInvoice() >> hasPrepaymentInvoice
            isFullyInvoiced() >> fullyInvoiced
        }
        orderItem.orderItemStatusCode = OrderItemStatusCode.CANCELED

        expect:
        orderItem.invoiceable == expected

        where:
        hasPrepaymentInvoice | fullyInvoiced || expected
        true                 | false         || true
        true                 | true          || false
        false                | false         || false
    }

    void 'OrderItem.isInvoiceable() should return false for a fully invoiced item even if quantity is still available'() {
        given:
        OrderItem orderItem = Spy(OrderItem) {
            isFullyInvoiced() >> true
            getQuantityAvailableToInvoice() >> 5
        }

        expect:
        !orderItem.invoiceable
    }

    void 'OrderItem totals should apply amount and percentage adjustments and ignore canceled ones'() {
        given:
        OrderItem orderItem = new OrderItem(quantity: 4, unitPrice: 2.50)
        orderItem.orderAdjustments = [
                new OrderAdjustment(amount: 1.00),
                new OrderAdjustment(percentage: 10),
                new OrderAdjustment(amount: 50.00, canceled: true),
        ] as Set

        expect:
        orderItem.subtotal == 10.00
        orderItem.totalAdjustments == 2.00
        orderItem.total == 12.00
        orderItem.totalPrice() == 12.00
    }

    void 'OrderItem totals should treat a missing quantity or unit price as zero'() {
        given:
        OrderItem orderItem = new OrderItem(quantity: quantity, unitPrice: unitPrice)
        orderItem.orderAdjustments = [] as Set

        expect:
        orderItem.subtotal == 0
        orderItem.total == 0

        where:
        quantity | unitPrice
        null     | 2.50
        4        | null
    }

    void 'OrderItem.getUnitOfMeasure() should describe the unit of measure and quantity per unit of measure'() {
        given:
        OrderItem orderItem = new OrderItem(quantityUom: new UnitOfMeasure(code: 'BX'), quantityPerUom: 12)

        expect:
        orderItem.unitOfMeasure == 'BX/12'
    }

    void 'OrderItem.compareTo() should sort by date created, then product name, then quantity, then id'() {
        given:
        OrderItem older = item('4', new Date(1000L), 'Zinc', 1)
        OrderItem aspirinSmall = item('3', new Date(2000L), 'Aspirin', 1)
        OrderItem aspirinLargeB = item('2', new Date(2000L), 'Aspirin', 5)
        OrderItem aspirinLargeA = item('1', new Date(2000L), 'Aspirin', 5)
        OrderItem bandage = item('0', new Date(2000L), 'Bandage', 1)

        expect:
        [bandage, aspirinLargeB, older, aspirinLargeA, aspirinSmall].sort()*.id == ['4', '3', '1', '2', '0']
    }

    private static OrderItem item(String id, Date dateCreated, String productName, Integer quantity) {
        OrderItem orderItem = new OrderItem(product: new Product(name: productName), quantity: quantity)
        orderItem.id = id
        orderItem.dateCreated = dateCreated
        return orderItem
    }

    void 'OrderItem.toImport() should export the product source details with empty defaults'() {
        given:
        OrderItem orderItem = new OrderItem(
                product: new Product(productCode: 'P1'),
                quantity: 3,
                unitPrice: 2.5,
                quantityUom: new UnitOfMeasure(code: 'BX'),
                quantityPerUom: 10,
        )
        orderItem.id = 'item-1'

        expect:
        orderItem.toImport() == [
                id              : 'item-1',
                productCode     : 'P1',
                sourceName      : '',
                supplierCode    : '',
                manufacturer    : '',
                manufacturerCode: '',
                quantity        : '3',
                unitPrice       : '2.5',
                unitOfMeasure   : 'BX/10',
                budgetCode      : '',
        ]
    }

    void 'OrderItem.toImport() should export the product source and budget code when present'() {
        given:
        Organization manufacturer = new Organization(name: 'Maker')
        OrderItem orderItem = new OrderItem(
                product: new Product(productCode: 'P1'),
                productSupplier: new ProductSupplier(name: 'Source', supplierCode: 'S1', manufacturer: manufacturer, manufacturerCode: 'M1'),
                budgetCode: new BudgetCode(code: 'B1'),
                quantity: 3,
                unitPrice: 2.5,
                quantityUom: new UnitOfMeasure(code: 'EA'),
                quantityPerUom: 1,
        )

        when:
        Map json = orderItem.toImport()

        then:
        json.sourceName == 'Source'
        json.supplierCode == 'S1'
        json.manufacturer == 'Maker'
        json.manufacturerCode == 'M1'
        json.budgetCode == 'B1'
    }
}
