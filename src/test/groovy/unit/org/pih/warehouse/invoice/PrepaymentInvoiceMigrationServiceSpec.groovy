package unit.org.pih.warehouse.invoice

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.PaymentTerm
import org.pih.warehouse.invoice.Invoice
import org.pih.warehouse.invoice.InvoiceItem
import org.pih.warehouse.invoice.InvoiceType
import org.pih.warehouse.invoice.InvoiceTypeCode
import org.pih.warehouse.invoice.PrepaymentInvoiceMigrationService
import org.pih.warehouse.invoice.PrepaymentInvoiceService
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.ShipmentItem

@Unroll
class PrepaymentInvoiceMigrationServiceSpec extends Specification implements ServiceUnitTest<PrepaymentInvoiceMigrationService>, DataTest {

    @Shared
    InvoiceType prepaymentInvoiceType

    @Shared
    InvoiceType regularInvoiceType

    void setupSpec() {
        mockDomains(Invoice, InvoiceItem, InvoiceType, Order, OrderItem, ShipmentItem)
    }

    void setup() {
        prepaymentInvoiceType = new InvoiceType(code: InvoiceTypeCode.PREPAYMENT_INVOICE).save(validate: false)
        regularInvoiceType = new InvoiceType(code: InvoiceTypeCode.INVOICE).save(validate: false)

        // Call spy here because we want to test the real service
        service.prepaymentInvoiceService = Spy(PrepaymentInvoiceService)
    }

    void 'updateAmountForPrepaymentInvoiceItems does nothing for regular invoices'() {
        given:
        String invoiceItemId = "1"
        new Invoice(
                invoiceType: regularInvoiceType,
                invoiceItems: [
                        new InvoiceItem(
                                id: invoiceItemId,
                                amount: null,
                        ),
                ],
        ).save(validate: false)

        when:
        service.updateAmountForPrepaymentInvoiceItems()

        then:
        assert InvoiceItem.findById(invoiceItemId).amount == null
    }

    void 'updateAmountForPrepaymentInvoiceItems does nothing if amount already set'() {
        given:
        String invoiceItemId = "1"
        new Invoice(
                invoiceType: prepaymentInvoiceType,
                invoiceItems: [
                        new InvoiceItem(
                                id: invoiceItemId,
                                amount: originalAmount,
                        ),
                ],
        ).save(validate: false)

        when:
        service.updateAmountForPrepaymentInvoiceItems()

        then:
        assert InvoiceItem.findById(invoiceItemId).amount == expectedAmount

        where:
        originalAmount || expectedAmount
        0              || 0
        1              || 1
    }

    void 'updateAmountForPrepaymentInvoiceItems correctly sets amount for invoice items'() {
        given:
        Order order = new Order(
                paymentTerm: new PaymentTerm(prepaymentPercent: prepaymentPercent),
        )
        OrderItem orderItem = new OrderItem(
                order: order,
                quantity: quantity,
                unitPrice: unitPrice,
        )

        String invoiceItemId = "1"
        new Invoice(
                invoiceType: prepaymentInvoiceType,
                invoiceItems: [
                        new InvoiceItem(
                                id: invoiceItemId,
                                quantity: 0,  // Not used in amount calculation. Just needs to be non-null.
                                orderItems: [orderItem],
                        ),
                ],
        ).save(validate: false)

        when:
        service.updateAmountForPrepaymentInvoiceItems()

        then:
        assert InvoiceItem.findById(invoiceItemId).amount == expectedAmount

        where:
        quantity | unitPrice | prepaymentPercent || expectedAmount
        1        | 1.0       | 100               || 1
        2        | 1.0       | 100               || 2
        1        | 2.0       | 100               || 2
        1        | 1.0       | 50                || 0.5
    }

    void 'updateAmountForPrepaymentInvoiceItems correctly sets amount for a flat adjustment to the order'() {
        given:
        Order order = new Order(
                paymentTerm: new PaymentTerm(prepaymentPercent: 100),
        )
        OrderAdjustment adjustment = new OrderAdjustment(
                order: order,
                amount: adjustmentAmount,
        )

        String invoiceItemId = "1"
        new Invoice(
                invoiceType: prepaymentInvoiceType,
                invoiceItems: [
                        new InvoiceItem(
                                id: invoiceItemId,
                                quantity: 0,  // Not used in amount calculation. Just needs to be non-null.
                                orderAdjustments: [adjustment],
                        ),
                ],
        ).save(validate: false)

        when:
        service.updateAmountForPrepaymentInvoiceItems()

        then:
        assert InvoiceItem.findById(invoiceItemId).amount == expectedAmount

        where:
        adjustmentAmount || expectedAmount
        0                || 0
        100              || 100
        10.24            || 10.24
    }

    void 'updateAmountForPrepaymentInvoiceItems correctly sets amount for a flat adjustment to an order item'() {
        given:
        Order order = new Order(
                paymentTerm: new PaymentTerm(prepaymentPercent: prepaymentPercent),
        )
        OrderItem orderItem = new OrderItem(
                order: order,
                quantity: quantity,
                unitPrice: unitPrice,
        )
        OrderAdjustment adjustment = new OrderAdjustment(
                order: order,
                orderItem: orderItem,
                amount: adjustmentAmount,
                percentage: adjustmentPercent,
        )

        String invoiceItemId = "1"
        new Invoice(
                invoiceType: prepaymentInvoiceType,
                invoiceItems: [
                        new InvoiceItem(
                                id: invoiceItemId,
                                quantity: 0,  // Not used in amount calculation. Just needs to be non-null.
                                orderItems: [orderItem],
                                orderAdjustments: [adjustment],
                        ),
                ],
        ).save(validate: false)

        when:
        service.updateAmountForPrepaymentInvoiceItems()

        then:
        assert InvoiceItem.findById(invoiceItemId).amount == expectedAmount

        where:
        quantity | unitPrice | prepaymentPercent | adjustmentPercent | adjustmentAmount || expectedAmount
        1        | 1.0       | null              | null              | 1                || 1
        1        | 1.0       | null              | null              | 1                || 1
        1        | 1.0       | null              | null              | 30               || 30
        1        | 1.0       | null              | null              | 0.25             || 0.25
        // Quantity and unit price changes don't affect the invoice amount if there's a fixed adjustment amount
        1        | 1.0       | null              | null              | 1                || 1
        2        | 1.0       | null              | null              | 1                || 1
        1        | 2.0       | null              | null              | 1                || 1
        // prepayment percent DOES apply to fixed adjustment amounts
        1        | 1.0       | 100               | null              | 1               || 1
        1        | 1.0       | 50                | null              | 1                || 0.5
        // Adjustment percent is ignored if there's a fixed adjustment amount
        1        | 1.0       | null              | 100               | 1                || 1
        1        | 1.0       | null              | 50                | 1                || 1
    }

    void 'updateAmountForPrepaymentInvoiceItems correctly sets amount for a percentage adjustment to an order item'() {
        given:
        Order order = new Order(
                paymentTerm: new PaymentTerm(prepaymentPercent: prepaymentPercent),
        )
        OrderItem orderItem = new OrderItem(
                order: order,
                quantity: quantity,
                unitPrice: unitPrice,
        )
        OrderAdjustment adjustment = new OrderAdjustment(
                order: order,
                orderItem: orderItem,
                percentage: adjustmentPercent,
        )

        String invoiceItemId = "1"
        new Invoice(
                invoiceType: prepaymentInvoiceType,
                invoiceItems: [
                        new InvoiceItem(
                                id: invoiceItemId,
                                quantity: 0,  // Not used in amount calculation. Just needs to be non-null.
                                orderItems: [orderItem],
                                orderAdjustments: [adjustment],
                        ),
                ],
        ).save(validate: false)

        when:
        service.updateAmountForPrepaymentInvoiceItems()

        then:
        assert InvoiceItem.findById(invoiceItemId).amount == expectedAmount

        where:
        quantity | unitPrice | prepaymentPercent | adjustmentPercent || expectedAmount
        1        | 1.0       | 100               | 100               || 1
        2        | 1.0       | 100               | 100               || 2
        1        | 2.0       | 100               | 100               || 2
        1        | 1.0       | 50                | 100               || 0.5
        1        | 1.0       | 100               | 50                || 0.5
        1        | 1.0       | 50                | 50                || 0.25
    }

    void 'updateAmountForPrepaymentInvoiceItems correctly sets amount for order items that are canceled after prepayment'() {
        given:
        Order order = new Order(
                paymentTerm: new PaymentTerm(prepaymentPercent: 100),
        )
        OrderItem orderItem = new OrderItem(
                order: order,
                orderItemStatusCode: OrderItemStatusCode.CANCELED,
                quantity: 1,
                unitPrice: 1.0,
        )

        // The fact that there is a prepayment invoice at all for the item means it was canceled after prepayment.
        String invoiceItemId = "1"
        new Invoice(
                invoiceType: prepaymentInvoiceType,
                invoiceItems: [
                        new InvoiceItem(
                                id: invoiceItemId,
                                quantity: 0,  // Not used in amount calculation. Just needs to be non-null.
                                orderItems: [orderItem],
                        ),
                ],
        ).save(validate: false)
        new Invoice(
                invoiceType: regularInvoiceType,
                invoiceItems: [
                        new InvoiceItem(
                                amount: 0,
                                quantity: 0,
                                orderItems: [orderItem],
                        ),
                ],
        ).save(validate: false)

        when:
        service.updateAmountForPrepaymentInvoiceItems()

        then: 'the prepayment amount should be unaffected by item cancellations that happen after prepayment'
        assert InvoiceItem.findById(invoiceItemId).amount == 1
    }

    void 'migrateFinalInvoice successfully creates inverse items for prepayment items'() {
        given: 'an existing prepayment invoice with an associated final invoice'
        Invoice prepaymentInvoice = new Invoice(
                invoiceType: prepaymentInvoiceType,
                invoiceItems: [
                        new InvoiceItem(
                                quantity: 0,  // Not used in amount calculation. Just needs to be non-null.
                                amount: prepaymentAmount,
                        ),
                ],
        ).save(validate: false)

        // We leave out the final invoice items from the final invoice here because they're not used
        // when generating the inverse items so there's no point defining them.
        Invoice finalInvoice = new Invoice(
                invoiceNumber: "1",
        ).save(validate: false)

        when:
        Invoice finalInvoiceAfterSave = service.migrateFinalInvoice(prepaymentInvoice, finalInvoice)

        then: 'the inverse item should be created'
        assert finalInvoiceAfterSave.invoiceItems.size() == 1

        InvoiceItem inverseItem = finalInvoiceAfterSave.invoiceItems.find { it.inverse }
        assert inverseItem != null

        and: 'the amount field should be set for the inverse item'
        assert inverseItem.amount == expectedInverseAmount

        where:
        prepaymentAmount || expectedInverseAmount
        0                || 0
        1                || -1
        10.46            || -10.46
    }

    void 'migrateFinalInvoice successfully sets amount for final invoice items'() {
        given: 'an existing prepayment invoice with an associated final invoice'
        Invoice prepaymentInvoice = new Invoice(
                invoiceType: prepaymentInvoiceType,
                invoiceItems: [
                        new InvoiceItem(
                                quantity: 0,  // Not used in amount calculation. Just needs to be non-null.
                                amount: 100,
                        ),
                ],
        ).save(validate: false)

        // We don't bother including the order item in the regular invoice here because it's not used when
        // computing the amount field.
        Invoice finalInvoice = new Invoice(
                invoiceNumber: "1",
                invoiceType: regularInvoiceType,
                invoiceItems: [
                        new InvoiceItem(
                                quantity: invoiceQuantity,
                                unitPrice: invoiceUnitPrice,
                        ),
                ],
        ).save(validate: false)

        when:
        Invoice finalInvoiceAfterSave = service.migrateFinalInvoice(prepaymentInvoice, finalInvoice)

        then: 'the inverse item should be created alongside the regular invoice item'
        assert finalInvoiceAfterSave.invoiceItems.size() == 2

        and: 'the amount field should be set for the regular invoice item'
        InvoiceItem invoiceItem = finalInvoiceAfterSave.invoiceItems.find { !it.inverse }
        assert invoiceItem != null
        assert invoiceItem.amount == expectedInvoiceAmount

        where:
        invoiceQuantity | invoiceUnitPrice || expectedInvoiceAmount
        null            | null             || 0
        null            | 1.0              || 0
        0               | 1.0              || 0
        1               | 0                || 0
        1               | null             || 0
        1               | 1.0              || 1
        2               | 1.0              || 2
        1               | 2.2              || 2.2
        2               | 2.2              || 4.4
        5               | 0.5              || 2.5
    }

    void 'OBPIH-6726: migrateFinalInvoice successfully populates the relationship to order items when generating inverses for canceled orders'() {
        given: 'an order with an order item but no shipment item (because the order is canceled)'
        Order order = new Order(
                paymentTerm: new PaymentTerm(prepaymentPercent: 100),
                canceled: true,
        )
        OrderItem orderItem = new OrderItem(
                // The order item isn't used at all whe migrating canceled orders but including it for clarity.
        )
        order.addToOrderItems(orderItem)
        order.save(validate: false)  // Will cascade save the order item

        and: 'we make a prepayment on that order'
        Invoice prepaymentInvoice = new Invoice(
                invoiceType: prepaymentInvoiceType,
        )
        InvoiceItem prepaymentInvoiceItem = new InvoiceItem(
                quantity: 0,  // Not used in amount calculation. Just needs to be non-null.
                amount: prepaymentAmount,
        )
        prepaymentInvoice.addToInvoiceItems(prepaymentInvoiceItem)
        prepaymentInvoice.save(validate: false)

        and: 'the prepayment item is mapped to the order item'
        orderItem.addToInvoiceItems(prepaymentInvoiceItem)
        orderItem.save(validate: false)

        and: 'we have already generated a final invoice for that order'
        InvoiceItem finalInvoiceItem = new InvoiceItem(
                // The final invoice item isn't used at all when migrating canceled orders but including it for clarity.
        )
        Invoice finalInvoice = new Invoice(
                invoiceNumber: "1",
        )
        finalInvoice.addToInvoiceItems(finalInvoiceItem)
        finalInvoice.save(validate: false)

        when:
        Invoice finalInvoiceAfterSave = service.migrateFinalInvoice(prepaymentInvoice, finalInvoice)

        then: 'the inverse item should be created'
        assert finalInvoiceAfterSave.invoiceItems.size() == 2

        InvoiceItem inverseItem = finalInvoiceAfterSave.invoiceItems.find { it.inverse }
        assert inverseItem != null
        assert inverseItem.amount == expectedInverseAmount

        and: 'the relationship between invoice item and order item should be fully populated in the returned inverse'
        assert inverseItem.orderItem != null
        assert inverseItem.orderItem.invoiceItems.size() == 2
        assert inverseItem.orderItem.invoiceItems.find { it.inverse } == inverseItem

        and: 'the relationship to order item should be fully populated in the order item'
        OrderItem orderItemFromDb = OrderItem.find(orderItem)
        assert orderItemFromDb != null
        assert orderItemFromDb.invoiceItems.size() == 2
        assert orderItemFromDb.invoiceItems.find { it.inverse } == inverseItem

        where:
        prepaymentAmount || expectedInverseAmount
        0                || 0
        1                || -1
        10.46            || -10.46
    }

    void 'OBPIH-6726: migrateFinalInvoice successfully populates the relationship to shipment items when generating inverses'() {
        given: 'an order with an order item and a shipment item'
        Order order = new Order(
                paymentTerm: new PaymentTerm(prepaymentPercent: 100),
        )
        OrderItem orderItem = new OrderItem(
                unitPrice: 1,
        )
        ShipmentItem shipmentItem = new ShipmentItem(
                quantity: 1,
                product: new Product(),
        )
        order.addToOrderItems(orderItem)
        orderItem.addToShipmentItems(shipmentItem)
        order.save(validate: false)  // Will cascade save the order item and shipment item

        and: 'we make a prepayment on that order'
        Invoice prepaymentInvoice = new Invoice(
                invoiceType: prepaymentInvoiceType,
        )
        InvoiceItem prepaymentInvoiceItem = new InvoiceItem(
                quantity: 1,  // Not used in amount calculation. Just needs to be > 0 to not get filtered out.
                unitPrice: 1,
        )
        prepaymentInvoice.addToInvoiceItems(prepaymentInvoiceItem)
        prepaymentInvoice.save(validate: false)

        and: 'the prepayment item is mapped to the order item'
        orderItem.addToInvoiceItems(prepaymentInvoiceItem)
        orderItem.save(validate: false)

        and: 'we have already generated a final invoice for that order'
        InvoiceItem finalInvoiceItem = new InvoiceItem(
                quantity: 1,
                amount: 1,
        )
        Invoice finalInvoice = new Invoice(
                invoiceNumber: "1",
        )
        finalInvoice.addToInvoiceItems(finalInvoiceItem)
        finalInvoice.save(validate: false)

        and: 'the final invoice item is mapped to the shipment item'
        shipmentItem.addToInvoiceItems(finalInvoiceItem)
        shipmentItem.save(validate: false)

        when:
        Invoice finalInvoiceAfterSave = service.migrateFinalInvoice(prepaymentInvoice, finalInvoice)

        then: 'the inverse item should be created'
        assert finalInvoiceAfterSave.invoiceItems.size() == 2

        InvoiceItem inverseItem = finalInvoiceAfterSave.invoiceItems.find { it.inverse }
        assert inverseItem != null
        assert inverseItem.amount == -1

        and: 'the relationship between invoice item and shipment item should be fully populated in the returned inverse'
        assert inverseItem.shipmentItem != null
        assert inverseItem.shipmentItem.invoiceItems.size() == 2
        assert inverseItem.shipmentItem.invoiceItems.find { it.inverse } == inverseItem

        and: 'the relationship to shipment item should be fully populated in the shipment item'
        ShipmentItem shipmentItemFromDb = ShipmentItem.find(shipmentItem)
        assert shipmentItemFromDb != null
        assert shipmentItemFromDb.invoiceItems.size() == 2
        assert shipmentItemFromDb.invoiceItems.find { it.inverse } == inverseItem

        where:
        quantity | unitPrice || expectedInverseAmount
        1        | 1         || -1
        0        | 1         || 0
        1        | 0         || 0
        2        | 3         || -6
        3        | 1.5       || -4.5
    }
}
