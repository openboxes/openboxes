package org.pih.warehouse.api.regression.platform.spec.purchasing

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.http.ContentType
import io.restassured.http.Method
import io.restassured.response.Response
import org.apache.http.HttpStatus
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.api.regression.platform.base.AccessCheckApi
import org.pih.warehouse.api.regression.platform.client.purchasing.InvoiceApi
import org.pih.warehouse.invoice.Invoice
import org.pih.warehouse.invoice.InvoiceItem

/**
 * Invoicing a vendor: creating and editing an invoice, finding what can be invoiced (the candidates of the vendor's
 * purchase orders), adding and removing invoice items, submitting and posting, and the lists the invoice pages use.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation(tolerate = [
        'known error-code POST /purchaseOrders/[^/]+/rollback',
        'known response POST /purchaseOrders/[^/]+/rollback',
])
class InvoiceApiSpec extends PurchasingSpec {

    @Autowired
    InvoiceApi invoiceApi

    /** A new invoice for the vendor, in US dollars; returns its id. */
    String newInvoice(String vendorInvoiceNumber = randomUtil.randomStringFieldValue('VIN')) {
        return invoiceApi.create(new JSONObject()
                .put('vendor', vendor.id)
                .put('vendorInvoiceNumber', vendorInvoiceNumber)
                .put('dateInvoiced', new Date().format('MM/dd/yyyy'))
                .put('currencyUom', new JSONObject().put('id', 'USD'))
                .toString(), responseSpecUtil.OK_RESPONSE_SPEC).jsonPath().getString('data.id')
    }

    /** Adds the purchase order's adjustment to the invoice (the candidate's id is the adjustment's); returns the item id. */
    String invoiceAdjustment(String invoiceId, Map<String, String> po) {
        invoiceApi.updateItems(invoiceId, new JSONObject().put('invoiceItems', new JSONArray()
                .put(new JSONObject().put('id', po.adjustment).put('quantityToInvoice', 1))).toString(),
                responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)
        return InvoiceItem.withNewSession { Invoice.get(invoiceId).invoiceItems*.id.first() }
    }

    void 'the invoice statuses and type codes are lists of options'() {
        expect:
        invoiceApi.statuses(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItems('PENDING', 'SUBMITTED', 'POSTED'))
                .build())
        invoiceApi.typeCodes(new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItems('INVOICE', 'PREPAYMENT_INVOICE'))
                .build())
    }

    void 'an invoice is created for a vendor, with a generated number, and read back'() {
        when:
        String vendorInvoiceNumber = randomUtil.randomStringFieldValue('VIN')
        String invoiceId = newInvoice(vendorInvoiceNumber)

        then:
        invoiceApi.read(invoiceId, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.equalTo(invoiceId))
                .expectBody('data.invoiceNumber', Matchers.not(Matchers.isEmptyOrNullString()))
                .expectBody('data.vendorInvoiceNumber', Matchers.equalTo(vendorInvoiceNumber))
                .expectBody('data.vendor', Matchers.equalTo(vendor.id))
                .expectBody('data.currencyUom.code', Matchers.equalTo('USD'))
                .build())
    }

    @PendingFeature(reason = 'creating an invoice without dateInvoiced saves it and answers 500')
    void 'an invoice can be created before its invoice date is known'() {
        expect:
        invoiceApi.create(new JSONObject().put('vendor', vendor.id).toString(), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.dateInvoiced', Matchers.nullValue())
                .build())
    }

    void 'an invoice is updated with PUT or POST'() {
        given:
        String invoiceId = newInvoice()

        expect:
        invoiceApi.update(invoiceId, new JSONObject()
                .put('vendor', vendor.id)
                .put('vendorInvoiceNumber', "Changed with ${method}".toString())
                .toString(), method, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.vendorInvoiceNumber', Matchers.equalTo("Changed with ${method}".toString()))
                .build())

        where:
        method << [Method.PUT, Method.POST]
    }

    void 'an invalid invoice is refused with 400, on create and on update'() {
        given:
        String invoiceId = newInvoice()
        String tooLong = 'x' * 256

        expect:
        invoiceApi.create(new JSONObject().put('vendor', vendor.id).put('name', tooLong).toString(),
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
        invoiceApi.update(invoiceId, new JSONObject().put('vendor', vendor.id).put('name', tooLong).toString(),
                Method.PUT, responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
    }

    @PendingFeature(reason = 'invoice operations without a current location answer 500')
    void 'invoices belong to the current location: without one the list asks for it'() {
        given: 'a session logged in without a location'
        Response login = authApiWrapper.login(new JSONObject().put('username', username).put('password', password),
                HttpStatus.SC_OK)

        expect:
        new AccessCheckApi(new RequestSpecBuilder()
                .setAccept(ContentType.JSON)
                .addCookie(login.getDetailedCookie('JSESSIONID'))
                .build())
                .call('GET', '/api/invoices', responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
    }

    @PendingFeature(reason = 'unknown invoices answer 500, not 404')
    void 'unknown invoices and invoice items are a 404'() {
        expect:
        invoiceApi.read('no-such-invoice', responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
        invoiceApi.update('no-such-invoice', '{}', Method.PUT, responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
        invoiceApi.items('no-such-invoice', [:], responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
        invoiceApi.updateItems('no-such-invoice', '{"invoiceItems": []}', responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
        invoiceApi.itemCandidates('no-such-invoice', '{}', responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
        invoiceApi.orderNumbers('no-such-invoice', responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
        invoiceApi.shipmentNumbers('no-such-invoice', responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
        invoiceApi.submit('no-such-invoice', responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
        invoiceApi.post('no-such-invoice', responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
        invoiceApi.prepaymentItems('no-such-invoice', responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
        invoiceApi.removeItem('no-such-item', responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }

    void 'the vendor purchase orders with something to invoice are offered, and their adjustments are candidates'() {
        given:
        Map<String, String> po = purchaseOrder()
        String invoiceId = newInvoice()

        expect: 'the order is among the orders to choose from; adjustments have no shipment'
        invoiceApi.orderNumbers(invoiceId, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data', Matchers.hasItem(po.orderNumber))
                .build())
        invoiceApi.shipmentNumbers(invoiceId, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data', Matchers.notNullValue())
                .build())

        and: 'the candidates of that order are its adjustment'
        invoiceApi.itemCandidates(invoiceId, new JSONObject()
                .put('orderNumbers', new JSONArray().put(po.orderNumber))
                .put('shipmentNumbers', new JSONArray())
                .toString(), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.contains(po.adjustment))
                .build())
    }

    @PendingFeature(reason = 'GET /api/invoices/{id}/items without paging answers 500 once the invoice has items')
    void 'an adjustment is invoiced, listed with paging, and removed again'() {
        given:
        Map<String, String> po = purchaseOrder()
        String invoiceId = newInvoice()

        when:
        String itemId = invoiceAdjustment(invoiceId, po)

        then:
        invoiceApi.items(invoiceId, [:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.contains(itemId))
                .expectBody('totalCount', Matchers.equalTo(1))
                .build())
        invoiceApi.items(invoiceId, [max: 10, offset: 0], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.contains(itemId))
                .build())

        and: 'the invoiced adjustment is no longer a candidate'
        invoiceApi.itemCandidates(invoiceId, new JSONObject()
                .put('orderNumbers', new JSONArray().put(po.orderNumber)).toString(), new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data', Matchers.empty())
                .build())

        when: 'the item is removed (the path names the invoice item)'
        invoiceApi.removeItem(itemId, responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)

        then:
        invoiceApi.items(invoiceId, [:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data', Matchers.empty())
                .build())
    }

    @PendingFeature(reason = 'an invalid invoice item change answers 204 and is silently not saved')
    void 'an adjustment is invoiced once: a quantity above 1 is refused, 0 removes the item'() {
        given:
        Map<String, String> po = purchaseOrder()
        String invoiceId = newInvoice()
        String itemId = invoiceAdjustment(invoiceId, po)

        expect: 'adjustments are not quantity-based'
        invoiceApi.updateItems(invoiceId, new JSONObject().put('invoiceItems', new JSONArray()
                .put(new JSONObject().put('id', itemId).put('quantity', 2))).toString(),
                responseSpecUtil.buildStatusCodeResponseSpec(HttpStatus.SC_BAD_REQUEST))
        invoiceApi.items(invoiceId, [:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.quantity', Matchers.contains(1))
                .build())

        when:
        invoiceApi.updateItems(invoiceId, new JSONObject().put('invoiceItems', new JSONArray()
                .put(new JSONObject().put('id', itemId).put('quantity', 0))).toString(),
                responseSpecUtil.NO_CONTENT_RESPONSE_SPEC)

        then:
        invoiceApi.items(invoiceId, [:], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data', Matchers.empty())
                .build())
    }

    void 'an invoice is submitted, then posted'() {
        given:
        Map<String, String> po = purchaseOrder()
        String invoiceId = newInvoice()
        invoiceAdjustment(invoiceId, po)

        expect:
        invoiceApi.submit(invoiceId, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.status', Matchers.equalTo('SUBMITTED'))
                .build())
        invoiceApi.post(invoiceId, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.status', Matchers.equalTo('POSTED'))
                .build())
    }

    void 'the invoice list finds an invoice by number, and downloads as CSV with or without its items'() {
        given:
        Map<String, String> po = purchaseOrder()
        String invoiceId = newInvoice()
        invoiceAdjustment(invoiceId, po)
        String invoiceNumber = Invoice.withNewSession { Invoice.get(invoiceId).invoiceNumber }

        expect:
        invoiceApi.list([invoiceNumber: invoiceNumber, max: 10, offset: 0], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(invoiceId))
                .expectBody('totalCount', Matchers.equalTo(1))
                .build())
        invoiceApi.list([invoiceNumber: invoiceNumber, vendor: vendor.id, status: 'PENDING'], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data.id', Matchers.hasItem(invoiceId))
                .build())

        when:
        Response invoices = invoiceApi.list([invoiceNumber: invoiceNumber, format: 'csv'], new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectContentType('text/csv')
                .build())
        Response items = invoiceApi.list([invoiceNumber: invoiceNumber, format: 'csv', invoiceItems: true],
                new ResponseSpecBuilder()
                        .expectStatusCode(HttpStatus.SC_OK)
                        .expectContentType('text/csv')
                        .build())

        then:
        invoices.asString().contains(invoiceNumber)
        items.asString().readLines().size() >= 2
    }

    void 'an invoice without prepaid orders has no prepayment items'() {
        given:
        String invoiceId = newInvoice()

        expect:
        invoiceApi.prepaymentItems(invoiceId, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('data', Matchers.empty())
                .expectBody('totalCount', Matchers.equalTo(0))
                .build())
    }

}
