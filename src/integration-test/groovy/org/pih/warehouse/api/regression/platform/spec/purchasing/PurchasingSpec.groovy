package org.pih.warehouse.api.regression.platform.spec.purchasing

import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.regression.platform.builder.RegressionLocationTestBuilder
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.EventType
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.PartyType
import org.pih.warehouse.core.PaymentTerm
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.invoice.Invoice
import org.pih.warehouse.invoice.PrepaymentInvoiceService
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentType
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.order.OrderSummaryService
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.order.OrderTypeCode

/**
 * Shared setup for the purchase order and invoice API specs: a vendor (an organization with a supplier location) and
 * purchase orders from it to the main facility (the logged-in location). Invoicing works on what is invoiceable: the
 * orders here carry order adjustments (a fee, a discount), which become invoice item candidates without a shipment.
 */
abstract class PurchasingSpec extends RegressionApiSpec {

    @Autowired
    PrepaymentInvoiceService prepaymentInvoiceService

    @Autowired
    OrderSummaryService orderSummaryService

    Organization vendor

    Location supplier

    @Override
    void setupData() {
        Map party = newVendor()
        vendor = party.vendor
        supplier = party.supplier
    }

    /** Another vendor: an organization with a supplier location; returns [vendor: ..., supplier: ...]. */
    Map newVendor() {
        return Organization.withNewTransaction {
            Organization organization = new Organization(
                    code: randomUtil.randomStringFieldValue('VND'),
                    name: randomUtil.randomStringFieldValue('Vendor'),
                    active: true,
                    partyType: PartyType.findByCode('ORG'))
                    .save(failOnError: true)
            Location location = new RegressionLocationTestBuilder()
                    .name(randomUtil.randomStringFieldValue('Supplier'))
                    .asFacility()
                    .organization(organization)
                    .build(true)
            [vendor: organization, supplier: location]
        }
    }

    /**
     * A purchase order from the vendor to the main facility with one order adjustment (amount 100) and, if asked,
     * one order line (10 of the test product at 2.50), in the given status; returns [order: id, adjustment: id,
     * orderNumber: number].
     */
    Map<String, String> purchaseOrder(OrderStatus status = OrderStatus.PLACED, Integer prepaymentPercent = null,
                                      boolean withLine = false, Map party = [vendor: vendor, supplier: supplier]) {
        Map<String, String> po = Order.withNewTransaction {
            PaymentTerm paymentTerm = prepaymentPercent == null ? null : new PaymentTerm(
                    code: randomUtil.randomStringFieldValue('PT'),
                    name: randomUtil.randomStringFieldValue('Payment term'),
                    prepaymentPercent: prepaymentPercent).save(failOnError: true)
            Location destination = Location.get(facility.id)
            Order order = new Order(
                    orderType: OrderType.findByCode(OrderTypeCode.PURCHASE_ORDER.name()),
                    name: randomUtil.randomStringFieldValue('Purchase order'),
                    orderNumber: randomUtil.randomStringFieldValue('PO'),
                    status: status,
                    origin: Location.get(party.supplier.id),
                    originParty: Organization.get(party.vendor.id),
                    destination: destination,
                    destinationParty: destination.organization,
                    orderedBy: Person.get(User.findByUsername(username).id),
                    dateOrdered: new Date(),
                    currencyCode: 'USD',
                    paymentTerm: paymentTerm)
            OrderAdjustment adjustment = new OrderAdjustment(description: 'Freight', amount: 100)
            order.addToOrderAdjustments(adjustment)
            if (withLine) {
                order.addToOrderItems(new OrderItem(product: Product.get(product.id), quantity: 10, unitPrice: 2.5,
                        quantityPerUom: 1, requestedBy: User.findByUsername(username), estimatedReadyDate: new Date()))
            }
            order.save(failOnError: true, flush: true)
            [order: order.id, adjustment: adjustment.id, orderNumber: order.orderNumber,
             paymentTerm: paymentTerm?.id, line: order.orderItems?.find()?.id]
        }
        refreshSummary(po.order)
        return po
    }

    /**
     * Ships the order's line: a shipment from the supplier with one item for it (the order now has a shipment);
     * returns the shipment item id. A shipped shipment (with a SHIPPED event) makes the item an invoice item
     * candidate.
     */
    String shipLine(Map<String, String> po, boolean shipped = false) {
        Shipment.withNewTransaction {
            OrderItem line = OrderItem.get(po.line)
            Shipment shipment = new Shipment(
                    name: randomUtil.randomStringFieldValue('Shipment'),
                    origin: Location.get(supplier.id),
                    destination: Location.get(facility.id),
                    expectedShippingDate: new Date(),
                    shipmentType: ShipmentType.list().first())
            if (shipped) {
                // The shipment's status comes from its events
                // (saved first: the shipment's current event refers to it)
                shipment.addToEvents(new Event(eventDate: new Date(), eventLocation: Location.get(supplier.id),
                        eventType: EventType.findByEventCode(EventCode.SHIPPED)).save(failOnError: true, flush: true))
            }
            ShipmentItem item = new ShipmentItem(product: line.product, quantity: line.quantity)
            shipment.addToShipmentItems(item)
            shipment.save(failOnError: true, flush: true)
            line.addToShipmentItems(item)
            line.save(failOnError: true, flush: true)
            item.id
        }
    }

    /**
     * The purchase order list reads a summary table that the application refreshes on order events; orders made
     * directly in the database here need the refresh done for them.
     */
    void refreshSummary(String orderId) {
        Order.withNewTransaction { orderSummaryService.refreshOrderSummary([orderId], false) }
    }

    /**
     * A prepaid purchase order: its prepayment invoice, and the final invoice that reverses the prepayment (one
     * item for the adjustment and its inverse item); returns [order: id, invoice: final invoice id,
     * item: its regular item id, inverseItem: id, prepaymentInvoice: id].
     */
    Map<String, String> prepaidOrderWithFinalInvoice() {
        Map<String, String> po = purchaseOrder(OrderStatus.PLACED, 50)
        return Invoice.withNewTransaction {
            Order order = Order.get(po.order)
            Invoice prepayment = prepaymentInvoiceService.generatePrepaymentInvoice(order)
            prepayment.save(failOnError: true, flush: true)
            order.refresh()
            Invoice invoice = prepaymentInvoiceService.generateInvoice(order)
            invoice.save(failOnError: true, flush: true)
            po + [
                    invoice          : invoice.id,
                    item             : invoice.invoiceItems.find { !it.inverse }.id,
                    inverseItem      : invoice.invoiceItems.find { it.inverse }?.id,
                    prepaymentInvoice: prepayment.id,
            ]
        }
    }
}
