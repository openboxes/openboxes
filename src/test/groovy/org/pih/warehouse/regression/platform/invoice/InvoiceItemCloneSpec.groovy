package org.pih.warehouse.regression.platform.invoice

import org.pih.warehouse.invoice.*
import grails.testing.gorm.DataTest
import spock.lang.Specification

import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.ShipmentItem

class InvoiceItemCloneSpec extends Specification implements DataTest {

    void setupSpec() {
        mockDomains(InvoiceItem, Invoice, OrderItem, OrderAdjustment, ShipmentItem, Order, Product, Organization)
    }

    void 'a cloned invoice item keeps its values and is linked to the same order and shipment lines'() {
        given:
        Product product = new Product(name: 'Gloves').save(validate: false)
        OrderItem orderItem = new OrderItem(product: product).save(validate: false)
        OrderAdjustment adjustment = new OrderAdjustment(description: 'Freight').save(validate: false)
        ShipmentItem shipmentItem = new ShipmentItem(quantity: 4).save(validate: false)
        InvoiceItem original = new InvoiceItem(product: product, quantity: 4, quantityPerUom: 12, amount: 48,
                unitPrice: 12, inverse: true, quantityUom: new UnitOfMeasure(code: 'BX'))
        original.addToOrderItems(orderItem)
        original.addToOrderAdjustments(adjustment)
        original.addToShipmentItems(shipmentItem)

        when:
        InvoiceItem clone = original.clone()

        then:
        !clone.is(original)
        [clone.product, clone.quantity, clone.quantityPerUom, clone.amount, clone.unitPrice, clone.inverse] ==
                [product, 4, 12, 48, 12, true]
        clone.orderItems == [orderItem] as Set
        clone.orderAdjustments == [adjustment] as Set
        clone.shipmentItems == [shipmentItem] as Set
        orderItem.invoiceItems.contains(clone)
        adjustment.invoiceItems.contains(clone)
        shipmentItem.invoiceItems.contains(clone)
        clone.unitOfMeasure == 'BX/12'
    }

    void 'organizations compare by name, then creation date'() {
        given:
        Organization acme = new Organization(name: 'Acme')
        Organization zeta = new Organization(name: 'Zeta')
        Organization acmeLater = new Organization(name: 'Acme')
        acme.dateCreated = new Date(1000)
        acmeLater.dateCreated = new Date(2000)

        expect:
        acme.compareTo(zeta) < 0
        zeta.compareTo(acme) > 0
        acme.compareTo(acmeLater) < 0
        acmeLater.compareTo(acme) > 0
    }
}
