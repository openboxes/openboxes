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
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode

@Unroll
class PrepaymentInvoiceMigrationServiceSpec extends Specification implements ServiceUnitTest<PrepaymentInvoiceMigrationService>, DataTest {

    @Shared
    InvoiceType prepaymentInvoiceType

    @Shared
    InvoiceType regularInvoiceType

    void setupSpec() {
        mockDomains(Invoice, InvoiceItem, InvoiceType)
    }

    void setup() {
        prepaymentInvoiceType = new InvoiceType(code: InvoiceTypeCode.PREPAYMENT_INVOICE).save(validate: false)
        regularInvoiceType = new InvoiceType(code: InvoiceTypeCode.INVOICE).save(validate: false)
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
                                quantity: 0,  // Value not used, just needs to be non-null
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
                                quantity: 0,  // Value not used, just needs to be non-null
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
                                quantity: 0,  // Value not used, just needs to be non-null
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
                                quantity: 0,  // Value not used, just needs to be non-null
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
                                quantity: 0,  // Value not used, just needs to be non-null
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

    void 'generateInverseInvoiceItems successfully creates inverse items for prepayment items'() {
        given:
        Invoice prepaymentInvoice = new Invoice(
                invoiceType: prepaymentInvoiceType,
                invoiceItems: [
                        new InvoiceItem(
                                quantity: 0,  // Value not used, just needs to be non-null
                                amount: prepaymentAmount,
                        ),
                ],
        ).save(validate: false)

        // We leave out the final invoice items from the final invoice here because they're not used
        // when generating the inverse items so there's no point defining them.
        Invoice finalInvoice = new Invoice(
                invoiceNumber: "1",
        )

        when:
        Invoice finalInvoiceAfterSave = service.generateInverseInvoiceItems(prepaymentInvoice, finalInvoice)

        then:
        assert finalInvoiceAfterSave.invoiceItems.size() == 1

        InvoiceItem inverseItem = finalInvoiceAfterSave.invoiceItems.find { it.inverse }
        assert inverseItem != null
        assert inverseItem.amount == expectedInverseAmount

        where:
        prepaymentAmount || expectedInverseAmount
        0                || 0
        1                || -1
        10.46            || -10.46
    }
}
