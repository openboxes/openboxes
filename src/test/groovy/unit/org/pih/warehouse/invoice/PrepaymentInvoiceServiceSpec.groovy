package unit.org.pih.warehouse.invoice

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.BudgetCode
import org.pih.warehouse.core.GlAccount
import org.pih.warehouse.core.PaymentTerm
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.invoice.Invoice
import org.pih.warehouse.invoice.InvoiceItem
import org.pih.warehouse.invoice.InvoiceService
import org.pih.warehouse.invoice.InvoiceType
import org.pih.warehouse.invoice.InvoiceTypeCode
import org.pih.warehouse.invoice.PrepaymentInvoiceService
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.order.OrderAdjustmentType
import org.pih.warehouse.order.OrderAdjustmentTypeCode
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.product.Product

@Unroll
class PrepaymentInvoiceServiceSpec extends Specification implements ServiceUnitTest<PrepaymentInvoiceService>, DataTest {

    void setupSpec() {
        mockDomains(Invoice, InvoiceItem, InvoiceType)
    }

    void setup() {
        new InvoiceType(code: InvoiceTypeCode.PREPAYMENT_INVOICE).save(validate: false)

        service.invoiceService = Stub(InvoiceService) {
            createFromOrder(_ as Order) >> new Invoice(invoiceNumber: UUID.randomUUID().toString())
        }
    }

    void 'generatePrepaymentInvoice should fail when order has no items or adjustments'() {
        given:
        Order order = new Order(
                orderItems: [],
                orderAdjustments: [],
        )

        when:
        service.generatePrepaymentInvoice(order)

        then:
        thrown(Exception)
    }

    void 'generatePrepaymentInvoice should fail when the order already has invoiced items'() {
        given:
        Order order = new Order(
                orderItems: [
                        new OrderItem(
                                invoiceItems: [new InvoiceItem()],
                        ),
                ],
        )

        when:
        service.generatePrepaymentInvoice(order)

        then:
        thrown(Exception)
    }

    void 'generatePrepaymentInvoice should fail when the order already has invoiced adjustments'() {
        given:
        Order order = new Order(
                orderAdjustments: [
                        new OrderAdjustment(
                                invoiceItems: [new InvoiceItem()],
                        ),
                ],
        )

        when:
        service.generatePrepaymentInvoice(order)

        then:
        thrown(Exception)
    }

    void 'generatePrepaymentInvoice should return a correct invoice for an order with an item'() {
        given:
        UnitOfMeasure quantityUom = new UnitOfMeasure()
        BigDecimal quantityPerUom = 1  // Not used in this flow so just set to some value
        BudgetCode budgetCode = new BudgetCode()
        GlAccount glAccount = new GlAccount()
        Product product = new Product(
                glAccount: glAccount,
        )
        Order order = new Order(
                paymentTerm: new PaymentTerm(prepaymentPercent: prepaymentPercent),
                orderItems: [
                        new OrderItem(
                                budgetCode: budgetCode,
                                quantity: quantity,
                                quantityUom: quantityUom,
                                quantityPerUom: quantityPerUom,
                                product: product,
                                unitPrice: unitPrice,
                                shipmentItems: [],
                                invoiceItems: [],
                        ),
                ],
        )

        when:
        Invoice invoice = service.generatePrepaymentInvoice(order)

        then:
        assert invoice.invoiceType.code == InvoiceTypeCode.PREPAYMENT_INVOICE

        Set<InvoiceItem> adjustmentInvoiceItems = getInvoiceItemsOnAdjustments(invoice)
        assert adjustmentInvoiceItems.size() == 0

        Set<InvoiceItem> orderInvoiceItems = getInvoiceItemsOnOrderItems(invoice)
        assert orderInvoiceItems.size() == 1

        InvoiceItem invoiceItem = orderInvoiceItems[0]
        assert invoiceItem.isPrepaymentInvoice
        assert invoiceItem.budgetCode == budgetCode
        assert invoiceItem.product == product
        assert invoiceItem.glAccount == glAccount
        assert invoiceItem.quantity == invoiceItemQuantity
        assert invoiceItem.quantityUom == quantityUom
        assert invoiceItem.quantityPerUom == quantityPerUom
        assert invoiceItem.amount == invoiceItemAmount
        assert invoiceItem.unitPrice == unitPrice

        where:
        quantity | unitPrice | prepaymentPercent || invoiceItemQuantity | invoiceItemAmount
        1        | 1.0       | 100               || 1                   | 1
        1        | 1.0       | 50                || 1                   | 0.5
        2        | 1.0       | 100               || 2                   | 2
        2        | 1.0       | 50                || 2                   | 1
        1        | 2.0       | 100               || 1                   | 2
        1        | 2.0       | 50                || 1                   | 1
        2        | 2.0       | 75                || 2                   | 3
    }

    void 'generatePrepaymentInvoice should not contain invoice items for cancelled order items'() {
        given:
        Order order = new Order(
                paymentTerm: new PaymentTerm(prepaymentPercent: 100),
                orderItems: [
                        new OrderItem(
                                orderItemStatusCode: OrderItemStatusCode.CANCELED,
                                quantity: 1,
                                unitPrice: 1.0,
                                product: new Product(),
                                shipmentItems: [],
                                invoiceItems: [],
                        ),
                ],
        )

        when:
        Invoice invoice = service.generatePrepaymentInvoice(order)

        then:
        assert invoice.invoiceType.code == InvoiceTypeCode.PREPAYMENT_INVOICE
        assert !invoice.invoiceItems
    }

    void 'generatePrepaymentInvoice should not contain invoice items for canceled order adjustments'() {
        given:
        Order order = new Order(
                paymentTerm: new PaymentTerm(prepaymentPercent: 100),
                orderAdjustments: [
                        new OrderAdjustment(
                                canceled: true,
                                percentage: 50,
                                orderAdjustmentType: new OrderAdjustmentType(code: OrderAdjustmentTypeCode.DISCOUNT_ADJUSTMENT),
                                invoiceItems: [],
                        ),
                ],
        )

        when:
        Invoice invoice = service.generatePrepaymentInvoice(order)

        then:
        assert invoice.invoiceType.code == InvoiceTypeCode.PREPAYMENT_INVOICE
        assert !invoice.invoiceItems
    }

    void 'generatePrepaymentInvoice should invoice correctly for an adjustment when no order items exist'() {
        given:
        BudgetCode adjustmentBudgetCode = new BudgetCode()
        GlAccount adjustmentGlAccount = new GlAccount()
        Order order = new Order(
                paymentTerm: new PaymentTerm(prepaymentPercent: prepaymentPercent),
                orderAdjustments: [
                        new OrderAdjustment(
                                budgetCode: adjustmentBudgetCode,
                                glAccount: adjustmentGlAccount,
                                amount: adjustmentAmount,
                                percentage: adjustmentPercentage,
                                orderAdjustmentType: new OrderAdjustmentType(code: OrderAdjustmentTypeCode.DISCOUNT_ADJUSTMENT),
                                orderItem: null,
                                invoiceItems: [],
                        ),
                ],
        )

        when:
        Invoice invoice = service.generatePrepaymentInvoice(order)

        then:
        assert invoice.invoiceType.code == InvoiceTypeCode.PREPAYMENT_INVOICE

        Set<InvoiceItem> orderInvoiceItems = getInvoiceItemsOnOrderItems(invoice)
        assert orderInvoiceItems.size() == 0

        Set<InvoiceItem> adjustmentInvoiceItems = getInvoiceItemsOnAdjustments(invoice)
        assert adjustmentInvoiceItems.size() == 1

        InvoiceItem invoiceItem = adjustmentInvoiceItems[0]
        assert invoiceItem.isPrepaymentInvoice
        assert invoiceItem.budgetCode == adjustmentBudgetCode
        assert invoiceItem.product == null
        assert invoiceItem.glAccount == adjustmentGlAccount
        assert invoiceItem.quantity == 1  // Quantity is always 1 for (non-canceled) adjustments
        assert invoiceItem.quantityUom == null
        assert invoiceItem.quantityPerUom == 1
        assert invoiceItem.amount == invoiceItemAmount
        assert invoiceItem.unitPrice == invoiceItemUnitPrice

        where:
        prepaymentPercent | adjustmentPercentage | adjustmentAmount || invoiceItemAmount | invoiceItemUnitPrice
        // Fixed amount adjustments should always have an invoiceItemAmount == adjustmentAmount * prepaymentPercent
        100               | 100                  | 1                || 1                 | 1
        100               | 50                   | 1                || 1                 | 1
        50                | 100                  | 1                || 0.5               | 1
        100               | null                 | 1                || 1                 | 1
        50                | null                 | 1                || 0.5               | 1
        // Non-fixed amount adjustments with no order items should always have an invoiceItemAmount == 0
        100               | 100                  | null             || 0                 | 0
        100               | 50                   | null             || 0                 | 0
        50                | 100                  | null             || 0                 | 0
        100               | null                 | null             || 0                 | 0
        50                | null                 | null             || 0                 | 0
    }

    void 'generatePrepaymentInvoice should invoice correctly for an adjustment with associated order items'() {
        given:
        UnitOfMeasure quantityUom = new UnitOfMeasure()
        BigDecimal quantityPerUom = 1  // Not used in this flow so just set to some value
        BudgetCode adjustmentBudgetCode = new BudgetCode()
        GlAccount glAccount = new GlAccount()
        Product product = new Product()
        OrderItem orderItem = new OrderItem(
                quantity: quantity,
                quantityUom: quantityUom,
                quantityPerUom: quantityPerUom,
                unitPrice: unitPrice,
                glAccount: glAccount,
                product: product,
                shipmentItems: [],
                invoiceItems: [],
        )
        Order order = new Order(
                paymentTerm: new PaymentTerm(prepaymentPercent: prepaymentPercent),
                orderItems: [orderItem],
                orderAdjustments: [
                        new OrderAdjustment(
                                budgetCode: adjustmentBudgetCode,
                                amount: adjustmentAmount,
                                percentage: adjustmentPercentage,
                                orderAdjustmentType: new OrderAdjustmentType(code: OrderAdjustmentTypeCode.DISCOUNT_ADJUSTMENT),
                                orderItem: orderItem,
                                invoiceItems: [],
                        ),
                ],
        )

        when:
        Invoice invoice = service.generatePrepaymentInvoice(order)

        then:
        assert invoice.invoiceType.code == InvoiceTypeCode.PREPAYMENT_INVOICE
        assert invoice.invoiceItems.size() == 2

        Set<InvoiceItem> orderItemInvoiceItems = getInvoiceItemsOnOrderItems(invoice)
        assert orderItemInvoiceItems.size() == 1
        // This test is for adjustment invoice items so don't bother with more order item asserts.

        Set<InvoiceItem> adjustmentInvoiceItems = getInvoiceItemsOnAdjustments(invoice)
        assert adjustmentInvoiceItems.size() == 1

        InvoiceItem invoiceItem = adjustmentInvoiceItems[0]
        assert invoiceItem.isPrepaymentInvoice
        assert invoiceItem.budgetCode == adjustmentBudgetCode
        assert invoiceItem.product == product
        assert invoiceItem.glAccount == glAccount
        assert invoiceItem.quantity == 1  // Quantity is always 1 for (non-canceled) adjustments
        assert invoiceItem.quantityUom == quantityUom
        assert invoiceItem.quantityPerUom == quantityPerUom
        assert invoiceItem.amount == invoiceItemAmount
        assert invoiceItem.unitPrice == invoiceItemUnitPrice

        where:
        quantity | unitPrice | prepaymentPercent | adjustmentPercentage | adjustmentAmount || invoiceItemAmount  | invoiceItemUnitPrice
        // Non-fixed adjustments should have an invoiceItemAmount == quantity * unitPrice * prepaymentPercent * adjustmentPercentage
        1        | 1.0       | 100               | 100                  | null             || 1                  | 1
        2        | 1.0       | 100               | 100                  | null             || 2                  | 2
        1        | 2.0       | 100               | 100                  | null             || 2                  | 2
        1        | 1.0       | 50                | 100                  | null             || 0.5                | 1
        1        | 1.0       | 100               | 50                   | null             || 0.5                | 0.5
        2        | 2.0       | 100               | 100                  | null             || 4                  | 4
        2        | 2.0       | 100               | 50                   | null             || 2                  | 2
        2        | 2.0       | 50                | 100                  | null             || 2                  | 4
        2        | 2.0       | 50                | 50                   | null             || 1                  | 2
        // Adjustments with a fixed amount should always have an invoiceItemAmount of adjustmentAmount * prepaymentPercent
        1        | 1.0       | 100               | null                 | 1                || 1                  | 1
        2        | 1.0       | 100               | null                 | 1                || 1                  | 1
        1        | 2.0       | 100               | null                 | 1                || 1                  | 1
        1        | 1.0       | 50                | null                 | 1                || 0.5                | 1
        1        | 1.0       | 100               | 50                   | 1                || 1                  | 1
        2        | 1.0       | 100               | 50                   | 1                || 1                  | 1
        1        | 2.0       | 100               | 50                   | 1                || 1                  | 1
        1        | 2.0       | 50                | 50                   | 1                || 0.5                | 1
        // Adjustments with no percent or amount should always have a zero invoiceItemAmount
        1        | 1.0       | 100               | null                 | null             || 0                  | 0
        2        | 1.0       | 100               | null                 | null             || 0                  | 0
        1        | 2.0       | 100               | null                 | null             || 0                  | 0
        1        | 1.0       | 50                | null                 | null             || 0                  | 0
    }

    void 'generateInvoice should fail when prepayment invoice is missing'() {
        given:
        Order order = new Order(
                invoices: [],
        )

        when:
        service.generateInvoice(order)

        then:
        thrown(Exception)
    }

    void 'getUnitPriceToInverse should calculate unitPrice to inverse #unitPriceToInverse when unitPrice invoiced is #unitPriceInvoiced and unitPrice inverseable is #unitPriceInverseable'() {
        when:
        BigDecimal unitPriceToInverseCalc = service.getUnitPriceToInverse(unitPriceInvoiced, unitPriceInversable)

        then:
        assert unitPriceToInverseCalc == unitPriceToInverse

        where:
        unitPriceInvoiced   | unitPriceInversable   | unitPriceToInverse
        1.0                 | 1.0                   | 1.0
        -1.0                | -1.0                  | -1.0
        1.0                 | 0.0                   | 0.0
        0.0                 | 1.0                   | 0.0
        -1.0                | 0.0                   | 0.0
        0.0                 | -1.0                  | 0.0
    }

    void 'getUnitPriceAvailableToInverse should calculate unit price available to inverse #unitPriceAvailableToInverse when unit price on prepayment item is #prepaymentItemUnitPrice and inversed unit price is #inversedUnitPrice'() {
        given:
        InvoiceItem prepaymentItem = new InvoiceItem()
        prepaymentItem.unitPrice = prepaymentItemUnitPrice
        OrderAdjustment orderAdjustment = Spy(OrderAdjustment) {
            getInversedUnitPrice() >> inversedUnitPrice
        }

        when:
        BigDecimal unitPriceAvailableToInverseCalc = service.getUnitPriceAvailableToInverse(
                prepaymentItem.unitPrice,
                orderAdjustment.inversedUnitPrice
        )

        then:
        assert unitPriceAvailableToInverseCalc == unitPriceAvailableToInverse

        where:
        prepaymentItemUnitPrice | inversedUnitPrice | unitPriceAvailableToInverse
        1.0                     | 1.0               | 0.0
        -1.0                    | -1.0              | 0.0
        1.0                     | 0.0               | 1.0
        -1.0                    | 0.0               | -1.0
        0.0                     | 0.0               | 0.0
        10                      | 3                 | 7
        -5                      | -2                | -3
        -5                      | -5                | 0
    }

    private Set<InvoiceItem> getInvoiceItemsOnOrderItems(Invoice invoice) {
        return invoice.invoiceItems.findAll{ it.orderItems }
    }

    private Set<InvoiceItem> getInvoiceItemsOnAdjustments(Invoice invoice) {
        return invoice.invoiceItems.findAll{ it.orderAdjustments }
    }
}
