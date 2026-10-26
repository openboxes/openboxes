package org.pih.warehouse.api.regression.obaf.mobile

import grails.util.GrailsWebMockUtil
import groovy.json.JsonSlurper
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.context.request.RequestContextHolder

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.api.MobileProductApiController
import org.pih.warehouse.api.PutawayApiController
import org.pih.warehouse.api.SearchService
import org.pih.warehouse.api.regression.obaf.events.support.ObafEventsIntegrationSpec
import org.pih.warehouse.api.regression.obaf.events.support.RecordingMailService
import org.pih.warehouse.api.regression.support.RegressionRoleFixtures
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductService
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.ContainerType
import org.pih.warehouse.shipping.Shipment

/**
 * The services behind OBAF's mobile API, called in-process (the HTTP contract, the app's
 * expectations and the status codes are in api.regression.mobile):
 * SearchService.globalSearch (the Scan screen), ProductService.getProduct / searchProducts (product
 * lookups and the scan search), MobileProductApiController.details (images, stock summary) and
 * updateIdentifier (UPC rules, notification), and the mobile putaway listing.
 */
class MobileServicesIntegrationSpec extends ObafEventsIntegrationSpec {

    static final Date SOON = new Date() + 120

    @Autowired
    SearchService searchService

    @Autowired
    ProductService productService

    /** globalSearch as the Scan screen calls it: the session facility bound as the current location. */
    Object scan(String depotId, String identifier) {
        return tx {
            authService.setCurrentLocation(depotId ? Location.get(depotId) : null)
            try {
                Object o = searchService.globalSearch(identifier)
                o instanceof AvailableItem ? [type: 'AvailableItem', id: o.inventoryItem?.id, bin: o.binLocation?.id] :
                        o ? [type: o.getClass().simpleName.replaceAll(/_\$\$.*/, ''), id: o.id] : null
            } finally {
                authService.setCurrentLocation(null)
            }
        }
    }

    // ------------------------------------------------------------------ SearchService.globalSearch

    void 'globalSearch resolves a #kind by its number'() {
        given:
        String facility = depot('MS-GS')
        Map fixture = make.call(this, facility) as Map

        expect:
        scan(facility, fixture.identifier as String) == [type: kind, id: fixture.id]

        where:
        kind          | make
        'Product'     | { s, d -> String p = s.product('GSP'); [id: p, identifier: s.tx { Product.get(p).productCode }] }
        'Location'    | { s, d -> String b = s.bin(d, 'GSB'); [id: b, identifier: s.tx { Location.get(b).locationNumber }] }
        'Requisition' | { s, d -> String r = s.requisition(d, [[productId: s.product('GSR'), quantity: 1]]); [id: r, identifier: s.tx { Requisition.get(r).requestNumber }] }
        'Shipment'    | { s, d -> String sh = s.inboundShipment(d, [[productId: s.product('GSS'), quantity: 1, lotNumber: "GS-${s.uid()}"]]); [id: sh, identifier: s.tx { Shipment.get(sh).shipmentNumber }] }
        'Container'   | { s, d -> s.container(d) }
        'Order'       | { s, d -> s.putawayOrderFixture(d) }
        'Transaction' | { s, d -> s.numberedTransaction(d) }
        'InventoryItem' | { s, d -> String p = s.product('GSL'); String lot = "GSL-${s.uid()}"; s.tx { new InventoryItem(product: Product.get(p), lotNumber: lot).save(flush: true, failOnError: true) }; [id: s.lotId(p, lot), identifier: lot] }
    }

    Map container(String dc) {
        String sh = inboundShipment(dc, [[productId: product('GSC'), quantity: 1, lotNumber: "GSC-${uid()}"]])
        String n = "LPN-GS-${uid()}"
        String id = tx {
            Shipment s = Shipment.get(sh)
            Container c = new Container(name: n, containerNumber: n, containerType: ContainerType.get(Constants.PALLET_CONTAINER_TYPE_ID))
            s.addToContainers(c)
            s.save(flush: true, failOnError: true)
            c.id
        } as String
        return [id: id, identifier: n]
    }

    Map putawayOrderFixture(String dc) {
        String from = bin(dc, 'GSF')
        String to = bin(dc, 'GST')
        String p = product('GSO')
        stock(dc, p, [[from, 2, 'GSO', SOON]])
        String orderId = putawayOrder(dc, p, 'GSO', 1, from, to).orderId
        return [id: orderId, identifier: tx { Order.get(orderId).orderNumber }]
    }

    Map numberedTransaction(String dc) {
        String b = bin(dc, 'GSX')
        stock(dc, product('GSX'), [[b, 1, 'GSX', SOON]])
        String n = "LT${uid()}"
        String id = tx {
            Transaction t = Transaction.findAllByInventory(Location.get(dc).inventory)[0]
            t.transactionNumber = n
            t.save(flush: true, failOnError: true)
            t.id
        } as String
        return [id: id, identifier: n]
    }

    void 'globalSearch: a product code wins over a bin with the same number; nothing matching, or no identifier, is null'() {
        given:
        String dc = depot('MS-PREC')
        String p = product('PREC')
        String code = tx { Product.get(p).productCode } as String
        String b = bin(dc, 'PREC')
        tx { Location l = Location.get(b); l.locationNumber = code; l.save(flush: true, failOnError: true) }

        expect:
        scan(dc, code) == [type: 'Product', id: p]
        scan(dc, "NOTHING-${uid()}") == null
        scan(dc, null) == null
        scan(dc, '') == null
    }

    void 'characterization: today a lot resolves to its InventoryItem whether it sits in one bin or several'() {
        given:
        String dc = depot('MS-LOTN')
        String b1 = bin(dc, 'LOTA')
        String b2 = bin(dc, 'LOTB')
        String p = product('LOTN')
        String one = "LOTO-${uid()}"
        String two = "LOTT-${uid()}"
        stock(dc, p, [[b1, 5, one, SOON], [b1, 2, two, SOON], [b2, 3, two, SOON]])

        expect:
        scan(dc, one) == [type: 'InventoryItem', id: lotId(p, one)]
        scan(dc, two) == [type: 'InventoryItem', id: lotId(p, two)]
    }

    void 'characterization: with no current location, anything that is not a product code fails (null.get())'() {
        given:
        String dc = depot('MS-NOLOC')
        String b = bin(dc, 'NOLOC')
        String number = tx { Location.get(b).locationNumber } as String

        when:
        scan(null, number)

        then:
        thrown(NullPointerException)
    }

    // ------------------------------------------------------------------ ProductService

    void 'getProduct finds a product by id or by code; an unknown one is ObjectNotFoundException (the API\'s 404)'() {
        given:
        String p = product('GETP')
        String code = tx { Product.get(p).productCode } as String

        expect:
        tx { productService.getProduct(p).id } == p
        tx { productService.getProduct(code).id } == p

        when:
        tx { productService.getProduct("NONE-${uid()}") }

        then:
        thrown(org.hibernate.ObjectNotFoundException)
    }

    void 'characterization: the scan search (searchProducts with one term) matches codes and UPCs by PREFIX, so a scanned code also returns longer codes'() {
        given:
        String base = "SCN${uid()}"
        String exact = product('X')
        String longer = product('Y')
        String byUpc = product('Z')
        tx {
            Product.get(exact).productCode = base
            Product.get(longer).productCode = "${base}0"
            Product.get(byUpc).upc = "${base}9"
            [exact, longer, byUpc].each { Product.get(it).save(flush: true, failOnError: true) }
        }

        when:
        List<String> found = tx { productService.searchProducts([base] as String[], [])*.id.unique() } as List<String>

        then:
        found as Set == [exact, longer, byUpc] as Set
    }

    // ------------------------------------------------------------------ MobileProductApiController (in-process)

    Map call(String action, Map params, String json = null, String username = 'admin', String facility = null) {
        GrailsWebRequest web = GrailsWebMockUtil.bindMockWebRequest(grails.util.Holders.applicationContext)
        try {
            web.currentRequest.method = json != null ? 'PUT' : 'GET'
            if (json != null) {
                web.currentRequest.contentType = 'application/json'
                web.currentRequest.characterEncoding = 'UTF-8'
                web.currentRequest.content = json.getBytes('UTF-8')
            }
            params.each { k, v -> web.params.put(k, v) }
            def controller = action == 'mobilePutaways' ? grails.util.Holders.applicationContext.getBean(PutawayApiController) :
                    grails.util.Holders.applicationContext.getBean(MobileProductApiController)
            tx {
                if (facility) {
                    web.session.setAttribute('warehouse', Location.get(facility))
                }
                if (username) {
                    web.session.setAttribute('user', User.findByUsername(username))
                }
                controller."${action}"()
            }
            String body = web.currentResponse.contentAsString
            Object parsed = null
            try { parsed = body ? new JsonSlurper().parseText(body) : null } catch (Exception ignored) { }
            return [status: web.currentResponse.status, body: body, json: parsed]
        } finally {
            RequestContextHolder.resetRequestAttributes()
        }
    }

    void 'details: stock summary for the session facility, images as {id, name, contentType, uri} with uri = the file URI'() {
        given:
        String dc = depot('MS-DET')
        String b = bin(dc, 'DET')
        String p = product('DET')
        stock(dc, p, [[b, 6, 'DET', SOON]])
        tx {
            Product product = Product.get(p)
            product.addToDocuments(new Document(name: 'front', filename: 'front.png', contentType: 'image/png',
                    fileUri: 'https://images.invalid/front.png'))
            product.addToDocuments(new Document(name: 'sheet', filename: 'sheet.pdf', contentType: 'application/pdf',
                    fileUri: 'https://images.invalid/sheet.pdf'))
            product.save(flush: true, failOnError: true)
        }

        when:
        Map r = call('details', [id: p], null, 'admin', dc)
        Map data = r.json.data as Map

        then:
        r.status == 200
        data.quantityOnHand == 6
        data.quantityAvailable == 6
        data.status == 'In Stock'
        data.images.collect { it.subMap(['name', 'contentType', 'uri']) } == [[name: 'front.png', contentType: 'image/png', uri: 'https://images.invalid/front.png']]
        !data.images[0].containsKey('url')
        data.defaultImageUrl == 'https://images.invalid/front.png'
        data.documents*.name as Set == ['front.png', 'sheet.pdf'] as Set

        and: 'quantityAllocated and quantityOnOrder are constants'
        data.quantityAllocated == 0
        data.quantityOnOrder == 0
    }

    void 'details: a product with no stock at the session facility is "Out of Stock"'() {
        given:
        String dc = depot('MS-OOS')
        String p = product('OOS')

        when:
        Map r = call('details', [id: p], null, 'admin', dc)

        then:
        r.json.data.status == 'Out of Stock'
        r.json.data.images == []
        r.json.data.defaultImageUrl == null
    }

    String productManager() {
        String username = "test-pm-${uid().toLowerCase()}".toString()
        RegressionRoleFixtures.ensureUser(username, [RoleType.ROLE_PRODUCT_MANAGER], [:])
        return username
    }

    void 'updateIdentifier: the UPC rules (#label)'() {
        given:
        String p = product('UPC')
        String other = product('UPCO')
        String otherCode = tx { Product.get(other).productCode } as String
        String taken = "TAKEN-${uid()}"
        tx { Product o = Product.get(other); o.upc = taken; o.save(flush: true, failOnError: true) }
        String user = asUser == 'pm' ? productManager() : asUser
        String json = body.replace('<taken>', taken).replace('<code>', otherCode)

        when:
        Map r = call('updateIdentifier', [id: productId == 'unknown' ? "NONE-${uid()}" : p], json, user)

        then:
        r.status == status

        where:
        label                                   | asUser  | productId | body                                                      || status
        'set by a product manager'              | 'pm'    | 'p'       | '{"identifier":{"type":"upc","value":"NEW-1"}}'           || 200
        'type is case-insensitive and trimmed'  | 'pm'    | 'p'       | '{"identifier":{"type":" UPC ","value":" NEW-2 "}}'       || 200
        'no session user'                       | null    | 'p'       | '{"identifier":{"type":"upc","value":"NEW-3"}}'           || 401
        'admin without ROLE_PRODUCT_MANAGER'    | 'admin' | 'p'       | '{"identifier":{"type":"upc","value":"NEW-4"}}'           || 403
        'unknown product'                       | 'pm'    | 'unknown' | '{"identifier":{"type":"upc","value":"NEW-5"}}'           || 404
        'missing type'                          | 'pm'    | 'p'       | '{"identifier":{"value":"NEW-6"}}'                         || 400
        'unsupported type'                      | 'pm'    | 'p'       | '{"identifier":{"type":"gtin","value":"NEW-7"}}'          || 400
        'UPC of another product'                | 'pm'    | 'p'       | '{"identifier":{"type":"upc","value":"<taken>"}}'          || 409
        'product code of another product'       | 'pm'    | 'p'       | '{"identifier":{"type":"upc","value":"<code>"}}'           || 409
    }

    void 'updateIdentifier: a set UPC is saved trimmed and every product notification recipient is told; clearing it notifies nobody'() {
        given:
        String p = product('UPCS')
        String pm = productManager()
        RecordingMailService mail = recordMail()
        String upc = "UPC-${uid()}"

        when:
        Map set = call('updateIdentifier', [id: p], """{"identifier":{"type":"upc","value":"  ${upc} "}}""", pm)

        then:
        set.status == 200
        set.json.data.identifier == [type: 'upc', value: upc]
        tx { Product.get(p).upc } == upc
        !mail.sent.isEmpty()
        mail.sent.every { it.body.contains(upc) }

        when:
        mail.sent.clear()
        Map cleared = call('updateIdentifier', [id: p], '{"identifier":{"type":"upc","value":""}}', pm)

        then:
        cleared.status == 200
        cleared.json.data.identifier == [type: 'upc', value: null]
        tx { Product.get(p).upc } == null
        mail.sent.isEmpty()
    }

    // ------------------------------------------------------------------ mobile putaway listing

    void 'mobilePutaways lists the PENDING putaway orders of the facility only; ?status= selects another status'() {
        given:
        String dc = depot('MS-PUT')
        String elsewhere = depot('MS-PUTX')
        Map mine = putawayOrderFixture(dc)
        Map completed = putawayOrderFixture(dc)
        tx { Order o = Order.get(completed.id); o.status = OrderStatus.COMPLETED; o.save(flush: true, failOnError: true) }
        Map theirs = putawayOrderFixture(elsewhere)

        when:
        Map pending = call('mobilePutaways', ['location.id': dc], null, 'admin', dc)
        Map done = call('mobilePutaways', ['location.id': dc, status: 'COMPLETED'], null, 'admin', dc)
        Map bySession = call('mobilePutaways', [:], null, 'admin', dc)

        then:
        pending.json.data*.putawayNumber == [mine.identifier]
        done.json.data*.putawayNumber == [completed.identifier]
        bySession.json.data*.putawayNumber == [mine.identifier]
        !(theirs.identifier in pending.json.data*.putawayNumber)
    }
}
