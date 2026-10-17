package org.pih.warehouse.api.regression.mobile

import grails.core.GrailsApplication
import groovy.sql.Sql
import io.restassured.response.Response
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.PendingFeature

import javax.sql.DataSource

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.ContainerType
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentType

/**
 * Products: catalogue lists and searches, barcode and global scan lookups,
 * product details, barcode (UPC) update, stock adjustment, and label printing.
 * Label calls never leave the JVM: LabelStubs stands in for the Zebra printer
 * (raw TCP) and for labelary.com (the PNG render behind defaultBarcodeLabelUrl).
 */
class ProductContractSpec extends MobileContractSpec {

    @Autowired
    GrailsApplication grailsApplication

    @Autowired
    DataSource dataSource

    @Autowired
    ProductAvailabilityService productAvailabilityService

    Location bin
    Product p
    InventoryItem lot

    void setupData() {
        bin = newBin('PR-BIN', [ActivityCode.PICK_STOCK.id, ActivityCode.PUTAWAY_STOCK.id])
        p = newProduct('PR')
        String lotNumber = "LOT-${uid()}".toString()
        stock(p, [[bin, 9, lotNumber, new Date() + 365]])
        lot = InventoryItem.withNewTransaction { InventoryItem.findByProductAndLotNumber(Product.get(p.id), lotNumber) }
    }

    /** Field paths of an entry except the ones listed: a typed scan result only carries the fields for its type. */
    static List<String> allBut(String id, List<String> keep) {
        return (MobileContract.entry(id).response.fields as List<Map>)*.path.findAll { String path -> !keep.contains(path) }
    }

    // ------------------------------------------------------------------ lists and searches

    @Covers('src/apis/products.ts#getProducts')
    void 'GET /generic/product?max&offset pages the catalogue'() {
        when:
        CallResult r = call('src/apis/products.ts#getProducts', ['pagination.max': 5, 'pagination.offset': 0])

        then:
        r.holds()
        (r.data.data as List).size() <= 5
    }

    @Covers('src/data/product/category/GetProductCategories.ts#getProductCategories')
    void 'GET /generic/category lists the categories for the picker'() {
        when:
        CallResult r = call('src/data/product/category/GetProductCategories.ts#getProductCategories')

        then:
        r.holds()
    }

    @Covers('src/apis/products.ts#searchProductGlobally')
    void 'POST /mobile/products/search {value} finds the product by code'() {
        when:
        CallResult r = call('src/apis/products.ts#searchProductGlobally', [:], [value: p.productCode])

        then:
        r.holds()
        (r.data.data as List<Map>)*.id.contains(p.id)
    }

    @Covers('src/apis/products.ts#searchProductByCode')
    void 'POST /generic/product/search by productCode like (search header, unreachable in the UI)'() {
        when:
        CallResult r = call('src/apis/products.ts#searchProductByCode', [:],
                [searchAttributes: [[property: 'productCode', operator: 'like', value: "${p.productCode}%".toString()]]])

        then:
        r.holds()
        (r.data.data as List<Map>)*.id.contains(p.id)
    }

    @Covers('src/apis/products.ts#searchProductsByCategory')
    @PendingFeature(reason = 'APP/SERVER MISMATCH (dead path): src/apis/products.ts searchProductsByCategory (category picker, unreachable in the UI) reads the body as a bare array; GenericApiController.search answers {data:[...]}')
    void 'POST /generic/product/search by category answers a bare array'() {
        when:
        CallResult r = call('src/apis/products.ts#searchProductsByCategory', [:],
                [searchAttributes: [[property: 'category.id', operator: 'eq', value: rootCategory.id]]])

        then:
        r.holds()
    }

    @Covers('src/apis/products.ts#searchProductsByName')
    @PendingFeature(reason = 'KNOWN MISMATCH: src/apis/products.ts searchProductsByName posts {searchAttributes:[{property:name, operator:like}]} to /mobile/products/search; MobileProductApiController.search reads only `value`, so it always answers {data:[]} (and the caller expects a bare array)')
    void 'POST /mobile/products/search by name like finds the product'() {
        when:
        CallResult r = call('src/apis/products.ts#searchProductsByName', [:],
                [searchAttributes: [[property: 'name', operator: 'like', value: "${p.name}%".toString()]]])

        then:
        r.holds()
        (r.json as List<Map>)*.id.contains(p.id)
    }

    // ------------------------------------------------------------------ barcode and scan lookups

    @Covers('src/apis/products.ts#getProductByBarcode')
    void 'GET /barcodes?id={upc} answers the product'() {
        when:
        CallResult r = call('src/apis/products.ts#getProductByBarcode', [barcode: p.upc])

        then:
        r.holds()
        r.data.data.id == p.id
    }

    @Covers('src/apis/createTransfer.ts#lookupBarcodeProduct')
    void 'GET /barcodes/{productCode} answers the product'() {
        when:
        CallResult r = call('src/apis/createTransfer.ts#lookupBarcodeProduct', [code: p.productCode])

        then:
        r.holds()
        r.data.data.id == p.id
    }

    @Covers(['src/apis/products.ts#getProductByBarcode', 'src/apis/createTransfer.ts#lookupBarcodeProduct'])
    @PendingFeature(reason = 'KNOWN MISMATCH: an unknown barcode on GET /barcodes/{code} and /barcodes?id= answers 500 (ApiController.barcodes throws ApiException, FIXME in the code) instead of 404; getProductByBarcode (src/apis/products.ts) currently treats that 500 as "not found", so the fix needs the app change in the same release')
    void 'an unknown barcode answers 404'() {
        when:
        CallResult q = call('src/apis/products.ts#getProductByBarcode', [barcode: "NOPE-${uid()}".toString()])
        CallResult path = call('src/apis/createTransfer.ts#lookupBarcodeProduct', [code: "NOPE-${uid()}".toString()])

        then:
        q.status == 404
        path.status == 404
    }

    @Covers('src/apis/products.ts#searchBarcode')
    void 'GET /globalSearch/{code} resolves a product, a lot, a bin and an LPN'() {
        given:
        String id = 'src/apis/products.ts#searchBarcode'
        InventoryItem bare = InventoryItem.withNewTransaction {
            new InventoryItem(product: Product.get(p.id), lotNumber: "LOT0-${uid()}".toString()).save(flush: true, failOnError: true)
        }
        String lpn = "CN-${uid()}".toString()
        Container.withNewTransaction {
            Shipment s = new Shipment(name: "S-${lpn}".toString(), shipmentNumber: "S-${lpn}".toString(), origin: Location.get('2'),
                    destination: Location.get('1'), expectedShippingDate: new Date(), shipmentType: ShipmentType.get('1'))
            s.save(flush: true, failOnError: true)
            new Container(name: lpn, containerNumber: lpn, containerType: ContainerType.get('1'), shipment: s).save(flush: true, failOnError: true)
        }

        when:
        CallResult product = call(id, [id: p.productCode])
        CallResult inventoryItem = call(id, [id: bare.lotNumber])
        CallResult location = call(id, [id: bin.locationNumber])
        CallResult container = call(id, [id: lpn])

        then:
        product.data.type == 'Product'
        product.holds([], allBut(id, ['type', 'data', 'data.id']))
        inventoryItem.data.type == 'InventoryItem'
        inventoryItem.holds([], allBut(id, ['type', 'data', 'data.product.id']))
        location.data.type == 'Location'
        location.holds([], allBut(id, ['type', 'data']))
        container.data.type == 'Container'
        container.holds([], allBut(id, ['type', 'data', 'data.id', 'data.shipmentNumber']))
    }

    @Covers('src/apis/products.ts#searchBarcode')
    @PendingFeature(reason = 'KNOWN MISMATCH: GET /globalSearch/{code} with nothing matching answers 500 (ApiController.globalSearch throws IllegalStateException) instead of 404')
    void 'GET /globalSearch/{unknown} answers 404'() {
        when:
        CallResult r = call('src/apis/products.ts#searchBarcode', [id: "NOPE-${uid()}".toString()])

        then:
        r.status == 404
    }

    // ------------------------------------------------------------------ product details, identifiers, adjustments

    @Covers('src/apis/products.ts#getProductById')
    void 'GET /mobile/products/{id}/details answers stock, bins and the label URL'() {
        given:
        ensureLabelTemplates()

        when:
        CallResult r = call('src/apis/products.ts#getProductById', [id: p.id])

        then:
        r.holds()
        r.data.data.defaultBarcodeLabelUrl.id
        r.data.data.quantityOnHand == 9
    }

    @Covers('src/apis/products.ts#getProductById')
    @PendingFeature(reason = 'KNOWN MISMATCH: the app types product images as {url} (src/data/product/ProductImage.ts); MobileProductApiController.details renders images as {id,name,contentType,uri}')
    void 'GET /mobile/products/{id}/details sends images with a url'() {
        given:
        Product.withNewTransaction {
            Product product = Product.get(p.id)
            product.addToDocuments(new Document(name: 'photo', filename: 'photo.png', contentType: 'image/png',
                    fileContents: LabelStubs.PNG_1X1))
            product.save(flush: true, failOnError: true)
        }

        when:
        CallResult r = call('src/apis/products.ts#getProductById', [id: p.id])

        then:
        r.status == 200
        r.data.data.images[0].url instanceof String
    }

    @Covers('src/apis/products.ts#updateProductIdentifier')
    void 'PUT /mobile/products/{id}/identifiers sets the UPC for a product manager'() {
        given:
        Sql sql = new Sql(dataSource)
        sql.execute("""INSERT INTO user_role (user_id, role_id) SELECT '1', id FROM role WHERE role_type = 'ROLE_PRODUCT_MANAGER'
                       AND NOT EXISTS (SELECT 1 FROM user_role ur JOIN role ro ON ro.id = ur.role_id
                                       WHERE ur.user_id = '1' AND ro.role_type = 'ROLE_PRODUCT_MANAGER')""")
        String nano = String.valueOf(System.nanoTime())
        String upc = nano.substring(nano.length() - 12)

        when:
        CallResult r = call('src/apis/products.ts#updateProductIdentifier', [id: p.id, type: 'upc', value: upc],
                [identifier: [type: 'upc', value: upc]])

        then:
        r.holds()
        Product.withNewTransaction { Product.get(p.id).upc } == upc

        cleanup:
        sql.execute("""DELETE FROM user_role WHERE user_id = '1' AND role_id IN (SELECT id FROM role WHERE role_type = 'ROLE_PRODUCT_MANAGER')""")
    }

    @Covers('src/apis/products.ts#stockAdjustments')
    void 'POST /stockAdjustments with the adjust stock body records the new quantity'() {
        given:
        // setupData recorded the stock a moment ago; InventoryService.adjustStock's same-second
        // duplicate check is wiped by command.validate(), and two transactions in one second
        // make the quantity on hand ambiguous, so step past that second as a person would
        Thread.sleep(1500)

        when:
        CallResult r = call('src/apis/products.ts#stockAdjustments', [:],
                ['location.id': '1', 'product.id': p.id, inventoryItem: lot.id, binLocation: bin.id, currentQuantity: 9,
                 reasonCode: 'CORRECTION', newQuantity: 6, comment: 'counted on the handheld'])
        // OBAF commits the adjustment after the response is written, and the test env refreshes
        // availability synchronously inside the request (before that commit), so the row the app
        // reads stays stale; production defers the refresh to a Quartz job. Run that refresh
        // once the adjustment is committed, then read what the app reads.
        Integer onHand = null
        long deadline = System.currentTimeMillis() + 10000
        while (onHand != 6 && System.currentTimeMillis() < deadline) {
            Thread.sleep(200)
            Location.withNewTransaction { productAvailabilityService.refreshProductsAvailability('1', [p.id], true) }
            onHand = call('src/apis/products.ts#getProductById', [id: p.id]).data.data.quantityOnHand as Integer
        }

        then:
        r.holds([], ['data['])
        onHand == 6
    }

    @Covers('src/apis/products.ts#stockAdjustments')
    @PendingFeature(reason = 'APP/SERVER MISMATCH: src/screens/AdjustStock reads data[0].quantityAvailable and data[0].quantityAdjusted from POST /stockAdjustments; OBAF StockAdjustmentApiController.create renders {data: <the AdjustStockCommand>} (one object, no such keys), so the screen shows undefined quantities after a successful adjustment')
    void 'POST /stockAdjustments answers the adjusted row the screen reads'() {
        when:
        CallResult r = call('src/apis/products.ts#stockAdjustments', [:],
                ['location.id': '1', 'product.id': p.id, inventoryItem: lot.id, binLocation: bin.id, currentQuantity: 9,
                 reasonCode: 'CORRECTION', newQuantity: 5, comment: 'counted on the handheld'])

        then:
        r.holds()
    }

    // ------------------------------------------------------------------ labels

    @Covers('src/apis/products.ts#printLabel')
    void 'POST /{products|internalLocations|containers}/{id}/labels/{documentId} prints ZPL to the printer'() {
        given:
        ensureLabelTemplates()
        LabelStubs stubs = new LabelStubs().start()
        Map before = stubs.apply(grailsApplication)
        Map docs = Document.withNewTransaction {
            ['barcodeLabel:product', 'barcodeLabel:internalLocation', 'barcodeLabel:container'].collectEntries { [(it): Document.findByDocumentNumber(it).id] }
        } as Map
        String lpn = "CN-${uid()}".toString()
        String containerId = Container.withNewTransaction {
            Shipment s = new Shipment(name: "S-${lpn}".toString(), shipmentNumber: "S-${lpn}".toString(), origin: Location.get('2'),
                    destination: Location.get('1'), expectedShippingDate: new Date(), shipmentType: ShipmentType.get('1'))
            s.save(flush: true, failOnError: true)
            new Container(name: lpn, containerNumber: lpn, containerType: ContainerType.get('1'), shipment: s).save(flush: true, failOnError: true).id
        }

        when:
        CallResult product = call('src/apis/products.ts#printLabel',
                ['data.type': 'products', 'data.productId': p.id, 'data.barcodeId': docs['barcodeLabel:product']])
        CallResult location = call('src/apis/products.ts#printLabel',
                ['data.type': 'internalLocations', 'data.productId': bin.id, 'data.barcodeId': docs['barcodeLabel:internalLocation']])
        CallResult container = call('src/apis/products.ts#printLabel',
                ['data.type': 'containers', 'data.productId': containerId, 'data.barcodeId': docs['barcodeLabel:container']])
        long deadline = System.currentTimeMillis() + 5000
        while (stubs.printed.size() < 3 && System.currentTimeMillis() < deadline) {
            Thread.sleep(50)
        }

        then:
        [product, location, container].each { CallResult c ->
            c.holds()
            assert !c.data?.errorCode: c.toString()
        }
        stubs.printed.any { it.contains(p.productCode) }
        stubs.printed.any { it.contains(bin.locationNumber) }
        stubs.printed.any { it.contains(lpn) }

        cleanup:
        stubs.restore(grailsApplication, before)
        stubs.stop()
    }

    @Covers('src/apis/products.ts#printLabel')
    @PendingFeature(reason = 'APP/SERVER MISMATCH: when printing fails (printer unreachable, unknown document, the app\'s literal "undefined" barcodeId) the printLabel actions catch the exception and answer HTTP 200 with {errorCode:500,...}; the app only shows an error on a rejected (non-2xx) call, so a failed print looks printed')
    void 'a label that cannot be printed answers an error status'() {
        given:
        ensureLabelTemplates()
        LabelStubs stubs = new LabelStubs().start()
        Map before = stubs.apply(grailsApplication)
        stubs.printer.close() // nothing listens on the printer port any more

        when:
        CallResult r = call('src/apis/products.ts#printLabel',
                ['data.type': 'products', 'data.productId': p.id, 'data.barcodeId': 'undefined'])

        then:
        r.status >= 400

        cleanup:
        stubs.restore(grailsApplication, before)
        stubs.stop()
    }

    @Covers('src/apis/products.ts#getProductById')
    void 'GET defaultBarcodeLabelUrl renders the label image through the labelary stub (no internet)'() {
        given:
        ensureLabelTemplates()
        LabelStubs stubs = new LabelStubs().start()
        Map before = stubs.apply(grailsApplication)
        String url = call('src/apis/products.ts#getProductById', [id: p.id]).data.data.defaultBarcodeLabelUrl.url
        String path = url.substring(url.indexOf('/openboxes/api') + '/openboxes/api'.length())

        when:
        Response image = raw(session(), 'GET', path, null)

        then:
        image.statusCode == 200
        image.contentType.startsWith('image/png')
        stubs.rendered.any { it.contains(p.productCode) }

        cleanup:
        stubs.restore(grailsApplication, before)
        stubs.stop()
    }
}
