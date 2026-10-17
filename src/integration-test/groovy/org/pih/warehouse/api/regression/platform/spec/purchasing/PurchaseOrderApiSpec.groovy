package org.pih.warehouse.api.regression.platform.spec.purchasing

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.response.Response
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.regression.platform.client.purchasing.PurchaseOrderApi
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.order.OrderType

/**
 * Purchase orders: the list (JSON and the two CSV exports), reading one, deleting a pending one, and rolling a placed
 * one back to pending.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation(tolerate = [
        'known error-code POST /purchaseOrders/[^/]+/rollback',
        'known response POST /purchaseOrders/[^/]+/rollback',
])
class PurchaseOrderApiSpec extends PurchasingSpec {

    @Autowired
    PurchaseOrderApi purchaseOrderApi

    void 'the list finds a purchase order by its number, with the order total and count'() {
        given:
        Map<String, String> po = purchaseOrder()

        expect:
        purchaseOrderApi.list([searchTerm: po.orderNumber, max: 10, offset: 0], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(po.order))
                .expectBody('totalCount', Matchers.equalTo(1))
                .build())
    }

    void 'the list filters by status'() {
        given:
        Map<String, String> pending = purchaseOrder(OrderStatus.PENDING)

        expect:
        purchaseOrderApi.list([searchTerm: pending.orderNumber, status: 'PENDING'], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(pending.order))
                .build())
        purchaseOrderApi.list([searchTerm: pending.orderNumber, status: 'PLACED'], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.not(Matchers.hasItem(pending.order)))
                .build())
    }

    void 'the purchase order statuses are a list of options'() {
        expect:
        purchaseOrderApi.statusOptions(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItems('PENDING', 'PLACED'))
                .build())
    }

    void 'the list downloads as CSV, by order or by order line'() {
        given:
        Map<String, String> po = purchaseOrder(OrderStatus.PLACED, null, true)

        when:
        Response orders = purchaseOrderApi.list([searchTerm: po.orderNumber, format: 'csv'], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectContentType('text/csv')
                .expectHeader('Content-Disposition', Matchers.containsString('Orders-'))
                .build())
        Response lines = purchaseOrderApi.list([searchTerm: po.orderNumber, format: 'csv', orderItems: true],
                new ResponseSpecBuilder()
                        .expectStatusCode(HttpStatus.SC_OK)
                        .expectContentType('text/csv')
                        .expectHeader('Content-Disposition', Matchers.containsString('OrderLineItems-'))
                        .build())

        then:
        orders.asString().readLines()[0].startsWith('Status,PO Number')
        orders.asString().contains(po.orderNumber)
        lines.asString().readLines()[0].startsWith('Supplier organization,Supplier location')
        lines.asString().readLines().size() == 2
        lines.asString().contains(product.productCode)
    }

    void 'a pending purchase order can be deleted'() {
        given:
        Map<String, String> pending = purchaseOrder(OrderStatus.PENDING)

        when:
        purchaseOrderApi.delete(pending.order, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)

        then:
        Order.withNewSession { Order.get(pending.order) } == null
    }

    @PendingFeature(reason = 'deleting a placed purchase order answers 400, not 409')
    void 'a placed purchase order cannot be deleted (409); an unknown one is a 404'() {
        given:
        Map<String, String> placed = purchaseOrder(OrderStatus.PLACED)

        expect:
        purchaseOrderApi.delete(placed.order, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_CONFLICT)
                .expectBody('errorCode', Matchers.equalTo(409))
                .build())
        Order.withNewSession { Order.get(placed.order) } != null
        purchaseOrderApi.delete('no-such-order', responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    @PendingFeature(reason = 'deleting a prepaid purchase order answers 400, not 409')
    void 'a prepaid purchase order cannot be deleted'() {
        given:
        Map<String, String> prepaid = prepaidOrderWithFinalInvoice()
        Order.withNewTransaction {
            Order order = Order.get(prepaid.order)
            order.status = OrderStatus.PENDING
            order.save(failOnError: true, flush: true)
        }

        expect:
        purchaseOrderApi.delete(prepaid.order, responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_CONFLICT))
    }

    void 'rolling back a placed purchase order makes it pending again'() {
        given:
        Map<String, String> placed = purchaseOrder(OrderStatus.PLACED)

        when:
        purchaseOrderApi.rollback(placed.order, responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        Order.withNewSession { Order.get(placed.order).status } == OrderStatus.PENDING
    }

    @PendingFeature(reason = 'rolling back an order with shipments answers 500, not 409')
    void 'an order with shipments is not rolled back (409)'() {
        given:
        Map<String, String> po = purchaseOrder(OrderStatus.PLACED, null, true)
        shipLine(po)

        expect:
        purchaseOrderApi.rollback(po.order, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_CONFLICT)
                .expectBody('errorMessage', Matchers.equalTo('Cannot rollback order with associated shipments'))
                .build())
        Order.withNewSession { Order.get(po.order).status } == OrderStatus.PLACED
    }

    void 'the list filters by several payment terms, "null" meaning none'() {
        given:
        Map<String, String> termed = purchaseOrder(OrderStatus.PLACED, 30)
        Map<String, String> untermed = purchaseOrder()

        expect:
        purchaseOrderApi.list([searchTerm: 'PO-', paymentTerm: [termed.paymentTerm, 'null'], max: 1000],
                new ResponseSpecBuilder()
                        .expectStatusCode(HttpStatus.SC_OK)
                        .expectBody('data.id', Matchers.hasItems(termed.order, untermed.order))
                        .build())
        purchaseOrderApi.list([searchTerm: termed.orderNumber, paymentTerm: [untermed.order]], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.not(Matchers.hasItem(termed.order)))
                .build())
    }

    void 'rolling back an unknown purchase order is a 404'() {
        expect:
        purchaseOrderApi.rollback('no-such-order', responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }
}
