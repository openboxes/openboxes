package org.pih.warehouse.api.regression.platform.spec.purchasing

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.specification.ResponseSpecification
import org.apache.http.HttpStatus
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.regression.platform.client.purchasing.InvoiceApi
import org.pih.warehouse.api.regression.platform.client.purchasing.PrepaymentInvoiceApi
import org.pih.warehouse.invoice.Invoice
import org.pih.warehouse.invoice.InvoiceItem

/**
 * The final invoice of a prepaid purchase order: its items reverse the prepayment (inverse items), and editing an
 * item's unit price keeps the inverse item in step. Inverse and prepayment items cannot be edited directly.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation(tolerate = [
        'known error-code POST /purchaseOrders/[^/]+/rollback',
        'known response POST /purchaseOrders/[^/]+/rollback',
])
class PrepaymentInvoiceApiSpec extends PurchasingSpec {

    @Autowired
    PrepaymentInvoiceApi prepaymentInvoiceApi

    @Autowired
    InvoiceApi invoiceApi

    void 'the final invoice of a prepaid order lists the prepayment items it reverses'() {
        given:
        Map<String, String> prepaid = prepaidOrderWithFinalInvoice()

        expect:
        invoiceApi.prepaymentItems(prepaid.invoice, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('totalCount', Matchers.greaterThanOrEqualTo(1))
                .build())
    }

    void 'the unit price of an adjustment item is changed in bulk, and on its own'() {
        given:
        Map<String, String> prepaid = prepaidOrderWithFinalInvoice()

        BigDecimal inverseBefore = InvoiceItem.withNewSession { InvoiceItem.get(prepaid.inverseItem).amount }

        when:
        prepaymentInvoiceApi.updateItems(prepaid.invoice, new JSONArray()
                .put(new JSONObject().put('id', prepaid.item).put('unitPrice', 80)).toString(),
                responseSpecUtil.OK_RESPONSE_SPEC)

        then: 'the item, and the inverse item reversing the prepayment with it'
        InvoiceItem.withNewSession { InvoiceItem.get(prepaid.item).unitPrice } == 80
        InvoiceItem.withNewSession { InvoiceItem.get(prepaid.inverseItem).amount } != inverseBefore

        when:
        prepaymentInvoiceApi.updateItem(prepaid.item, new JSONObject().put('unitPrice', 60).toString(),
                responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        InvoiceItem.withNewSession { InvoiceItem.get(prepaid.item).unitPrice } == 60
    }

    @PendingFeature(reason = 'a prepayment invoice item change the rules refuse answers 500, not 400 with the reason')
    void 'edits the invoice rules do not allow answer 400 with the reason'() {
        given:
        Map<String, String> prepaid = prepaidOrderWithFinalInvoice()

        expect: 'a unit price of 0, a missing value, a price above what is left to invoice'
        prepaymentInvoiceApi.updateItem(prepaid.item, new JSONObject().put('unitPrice', 0).toString(),
                badRequest('Cannot update unit price to 0'))
        prepaymentInvoiceApi.updateItem(prepaid.item, '{}', badRequest('Missing required attributes'))
        prepaymentInvoiceApi.updateItem(prepaid.item, new JSONObject().put('unitPrice', 1000).toString(),
                badRequest('Cannot update unit price to higher value than available to invoice'))

        and: 'the inverse item cannot be edited or deleted directly'
        prepaymentInvoiceApi.updateItem(prepaid.inverseItem, new JSONObject().put('unitPrice', 10).toString(),
                badRequest('Cannot edit inverse invoice items directly'))
        prepaymentInvoiceApi.deleteItem(prepaid.inverseItem,
                badRequest('Cannot delete prepayment or inverse items'))

        and: 'the bulk edit needs a list, and only applies to final invoices of prepaid orders'
        prepaymentInvoiceApi.updateItems(prepaid.invoice, '{}', responseSpecUtil.buildStatusCodeResponseSpec(
                HttpStatus.SC_BAD_REQUEST))
        prepaymentInvoiceApi.updateItems(prepaid.prepaymentInvoice, '[]',
                badRequest('Cannot update quantities on prepayment or non prepaid invoices'))
    }

    void 'an adjustment item is deleted together with its inverse item'() {
        given:
        Map<String, String> prepaid = prepaidOrderWithFinalInvoice()

        when:
        prepaymentInvoiceApi.deleteItem(prepaid.item, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)

        then:
        InvoiceItem.withNewSession { InvoiceItem.get(prepaid.item) } == null
        InvoiceItem.withNewSession { InvoiceItem.get(prepaid.inverseItem) } == null
    }

    @PendingFeature(reason = 'creating the final invoice without dateInvoiced answers 500')
    void 'only items of this invoice, of a prepaid order\'s final invoice, can be edited here'() {
        given:
        Map<String, String> prepaid = prepaidOrderWithFinalInvoice()
        Map<String, String> other = prepaidOrderWithFinalInvoice()
        Map<String, String> plain = purchaseOrder()
        String plainInvoice = invoiceApi.create(new JSONObject().put('vendor', vendor.id)
                .put('currencyUom', new JSONObject().put('id', 'USD')).toString(), responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath().getString('data.id')
        invoiceApi.updateItems(plainInvoice, new JSONObject().put('invoiceItems', new JSONArray()
                .put(new JSONObject().put('id', plain.adjustment).put('quantityToInvoice', 1))).toString(),
                responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)
        String plainItem = InvoiceItem.withNewSession { InvoiceItem.findByInvoice(Invoice.get(plainInvoice)).id }

        expect: 'an item of another invoice in the bulk edit'
        prepaymentInvoiceApi.updateItems(prepaid.invoice, new JSONArray()
                .put(new JSONObject().put('id', other.item).put('unitPrice', 10)).toString(),
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
        InvoiceItem.withNewSession { InvoiceItem.get(other.item).unitPrice } != 10

        and: 'an item of an invoice without prepayment'
        prepaymentInvoiceApi.updateItem(plainItem, '{"unitPrice": 10}',
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
        prepaymentInvoiceApi.deleteItem(plainItem,
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
        InvoiceItem.withNewSession { InvoiceItem.get(plainItem) } != null

        and: 'an item of an order without prepayment on a final invoice that also has prepaid items'
        Map<String, String> notPrepaid = purchaseOrder()
        invoiceApi.updateItems(prepaid.invoice, new JSONObject().put('invoiceItems', new JSONArray()
                .put(new JSONObject().put('id', notPrepaid.adjustment).put('quantityToInvoice', 1))).toString(),
                responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)
        String mixedItem = InvoiceItem.withNewSession {
            Invoice.get(prepaid.invoice).invoiceItems.find { it.orderAdjustment?.id == notPrepaid.adjustment }.id
        }
        prepaymentInvoiceApi.updateItem(mixedItem, '{"unitPrice": 10}',
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
        prepaymentInvoiceApi.deleteItem(mixedItem,
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
        prepaymentInvoiceApi.updateItems(prepaid.invoice, new JSONArray()
                .put(new JSONObject().put('id', mixedItem).put('unitPrice', 10)).toString(),
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))

        and: 'a body that lists an item twice'
        prepaymentInvoiceApi.updateItems(prepaid.invoice, new JSONArray()
                .put(new JSONObject().put('id', prepaid.item).put('unitPrice', 10))
                .put(new JSONObject().put('id', prepaid.item).put('unitPrice', 20)).toString(),
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))

        and: 'a body that is not a list of items with ids'
        prepaymentInvoiceApi.updateItems(prepaid.invoice, '[{"unitPrice": 10}]',
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
    }

    @PendingFeature(reason = 'unknown invoices and invoice items answer 500, not 404')
    void 'unknown invoices and invoice items are a 404'() {
        expect:
        prepaymentInvoiceApi.updateItems('no-such-invoice', '[]', responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
        prepaymentInvoiceApi.updateItem('no-such-item', '{"unitPrice": 1}', responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
        prepaymentInvoiceApi.deleteItem('no-such-item', responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    private static ResponseSpecification badRequest(String message) {
        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody('errorMessage', Matchers.equalTo(message))
                .build()
    }
}
