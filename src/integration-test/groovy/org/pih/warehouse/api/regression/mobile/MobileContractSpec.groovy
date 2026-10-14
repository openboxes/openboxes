package org.pih.warehouse.api.regression.mobile

import groovy.json.JsonOutput
import io.restassured.RestAssured
import io.restassured.config.RedirectConfig
import io.restassured.http.Cookie
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared

import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.common.domain.builder.inventory.RecordInventoryCommandTestBuilder
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Address
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.DocumentCode
import org.pih.warehouse.core.DocumentType
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.RefreshProductAvailabilityEventService
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.inventory.RecordInventoryCommand
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductType

/**
 * Base class of the mobile contract specs. It signs in exactly like
 * openboxes-mobile (POST /login with {username,password} only, GET
 * /getAppContext, PUT /chooseLocation/{id}, then the JSESSIONID cookie on every
 * call) and issues each call from its contract entry: the method and the path
 * template come from contract/mobile-contract.json, the spec supplies the
 * values of the `{x}` placeholders, so a call can only be made the way the app
 * makes it. Headers are axios's: Accept `application/json, text/plain, * /*`,
 * Content-Type application/json when there is a body; no X-Requested-With.
 */
abstract class MobileContractSpec extends ApiSpec {
    private static final Map<String, Long> LAST_RECORD_STOCK = [:].asSynchronized()


    static final String AXIOS_ACCEPT = 'application/json, text/plain, */*'

    @Shared
    Cookie appCookie

    @Autowired
    MobileFixtures fixtures

    /** Seeded bin LocationType (BIN_LOCATION; supports PUTAWAY_STOCK, PICK_STOCK). */
    static final String BIN_TYPE = 'cab2b4f35ba2d867015ba2e17e390001'

    /**
     * The ZPL label templates the app's print buttons need (no migration seeds
     * them; DocumentService looks them up by document number, and two documents
     * with one number make the details calls fail, so create each once).
     */
    void ensureLabelTemplates() {
        Document.withNewTransaction {
            DocumentType zt = DocumentType.findByDocumentCode(DocumentCode.ZEBRA_TEMPLATE) ?:
                    new DocumentType(name: 'Zebra template', documentCode: DocumentCode.ZEBRA_TEMPLATE).save(flush: true, failOnError: true)
            [(Constants.DEFAULT_PRODUCT_LABEL_DOCUMENT_NUMBER): '^XA^FO50,60^A0,40^FD${product.productCode}^FS^XZ',
             'barcodeLabel:internalLocation'               : '^XA^FO50,60^A0,40^FD${internalLocation.locationNumber}^FS^XZ',
             'barcodeLabel:container'                      : '^XA^FO50,60^A0,40^FD${container.containerNumber}^FS^XZ'].each { String number, String zpl ->
                if (!Document.findByDocumentNumber(number)) {
                    new Document(name: number, filename: "${number.replace(':', '-')}.zpl".toString(), contentType: 'text/plain',
                            documentNumber: number, documentType: zt, fileContents: zpl.getBytes('UTF-8'))
                            .save(flush: true, failOnError: true)
                }
            }
        }
    }

    /** A short unique suffix: the database is shared by every spec of the run and never cleaned. */
    static String uid() {
        return Long.toString(System.nanoTime(), 36).toUpperCase()
    }

    /**
     * A bin (internal location) under the facility, with its own supported
     * activities when given (they REPLACE the location type's, so include
     * PICK_STOCK / PUTAWAY_STOCK when the bin must still hold stock). Plain GORM:
     * TestDataConfig would give a built Location its own Inventory.
     */
    static String dcId

    @Autowired
    RefreshProductAvailabilityEventService refreshProductAvailabilityEventService

    /**
     * Location.afterInsert publishes a product availability refresh, which the
     * test environment runs synchronously (alwaysSynchronous) inside the insert,
     * before the cascaded Inventory row exists: "Location must have an
     * inventory". New depots are therefore saved with the refresh deferred to
     * its Quartz job, as in production.
     */
    def <T> T withDeferredAvailabilityRefresh(Closure<T> c) {
        Boolean before = refreshProductAvailabilityEventService.alwaysSynchronous
        refreshProductAvailabilityEventService.alwaysSynchronous = false
        try {
            return c.call()
        } finally {
            refreshProductAvailabilityEventService.alwaysSynchronous = before
        }
    }

    /**
     * "Test Mobile DC": the depot the picking flows run in, created once per
     * JVM. It carries the depot type's activities plus REQUIRE_MOBILE_PICKING
     * (allocation leaves items to be picked on the handheld instead of marking
     * them picked) and TRACK_INTERNAL_TRANSACTIONS (pick moves stock into the
     * tote and drop moves it to staging), so Main Warehouse, which upstream's
     * own specs use, is left untouched.
     */
    Location regressionDc() {
        if (dcId == null) {
            dcId = withDeferredAvailabilityRefresh { Location.withNewTransaction {
                Location existing = Location.findByLocationNumber('TEST-MOBILE-DC')
                if (existing) {
                    return existing.id
                }
                LocationType depot = LocationType.get('2')
                Location dc = new Location(name: 'Test Mobile DC', locationNumber: 'TEST-MOBILE-DC', locationType: depot,
                        organization: Location.get('1').organization, active: true,
                        address: new Address(address: '1 Dock Road', city: 'Boston', stateOrProvince: 'MA', postalCode: '02110', country: 'US'))
                dc.supportedActivities = ((depot.supportedActivities ?: []) as Set<String>) +
                        [ActivityCode.REQUIRE_MOBILE_PICKING.id, ActivityCode.TRACK_INTERNAL_TRANSACTIONS.id] as Set<String>
                dc.inventory = new Inventory(warehouse: dc)
                dc.save(flush: true, failOnError: true)
                return dc.id
            } }
        }
        return Location.withNewTransaction {
            Location.createCriteria().get { eq('id', dcId); join('inventory'); join('locationType') } as Location
        } as Location
    }

    /** A destination depot with an address (pick tasks render destinationAddress). */
    Location newDepot(String prefix) {
        String name = "${prefix}-${uid()}".toString()
        return withDeferredAvailabilityRefresh { Location.withNewTransaction {
            Location l = new Location(name: name, locationNumber: name, locationType: LocationType.get('2'),
                    organization: Location.get('1').organization, active: true,
                    address: new Address(address: '2 Ward Street', city: 'Cambridge', stateOrProvince: 'MA', postalCode: '02139', country: 'US'))
            l.inventory = new Inventory(warehouse: l)
            l.save(flush: true, failOnError: true)
        } } as Location
    }

    Location newBin(String prefix, Collection<String> activities = null, String typeId = BIN_TYPE, Location parent = null) {
        String name = "${prefix}-${uid()}".toString()
        return Location.withNewTransaction {
            Location bin = new Location(name: name, locationNumber: name,
                    locationType: LocationType.get(typeId),
                    parentLocation: Location.get((parent ?: facility).id), active: true)
            if (activities) {
                bin.supportedActivities = activities as Set<String>
            }
            bin.save(flush: true, failOnError: true)
        } as Location
    }

    /** A product with a unique code and UPC (the upstream builder gives every product the same UPC). */
    Product newProduct(String prefix, Map extra = [:]) {
        String code = "${prefix}-${uid()}".toString()
        String upc = String.valueOf(Math.abs(code.hashCode()) % 1000000000000L).padLeft(12, '0')
        return Product.withNewTransaction {
            Product p = new Product([name: "Test ${code}".toString(), productCode: code, upc: upc,
                                     category: rootCategory, productType: ProductType.get('DEFAULT'),
                                     unitOfMeasure: 'each', active: true] + extra)
            p.save(flush: true, failOnError: true)
        } as Product
    }

    /**
     * Records stock for one product in one call (several bins/lots): rows are
     * [bin, quantity] or [bin, quantity, lotNumber, expirationDate]. Record stock
     * sets a baseline, through the same API the web app uses.
     */
    void stock(Product p, List<List> rows, Location at = null) {
        Location fac = at ?: facility
        // Two record stocks of one product in the same second collide (400 "A transaction already exists at
        // time ..."): keep at least 1.1 s between them.
        Long last = LAST_RECORD_STOCK[p.id]
        long wait = last == null ? 0 : last + 1100 - System.currentTimeMillis()
        if (wait > 0) {
            sleep(wait)
        }
        LAST_RECORD_STOCK[p.id] = System.currentTimeMillis()
        RecordInventoryCommandTestBuilder b = new RecordInventoryCommandTestBuilder()
                .product(p).inventory(fac.inventory).transactionDateNow()
        rows.each { List r -> b.row(r.size() > 2 ? r[2] as String : null, r.size() > 3 ? r[3] as Date : null, r[0] as Location, r[1] as int) }
        RecordInventoryCommand cmd = b.build()
        recordStockApiWrapper.saveRecordStockOK(fac, cmd)
    }

    /** The app's sign-in; returns the session cookie. Each step must answer 2xx. */
    Cookie appSignIn(String user = 'admin', String pw = 'password', String locationId = '1') {
        Response login = raw(null, 'POST', '/login', [username: user, password: pw])
        assert login.statusCode == 200: "POST /login -> ${login.statusCode} ${login.asString()}"
        Cookie c = login.getDetailedCookie('JSESSIONID')
        assert c: 'POST /login set no JSESSIONID cookie'
        Response ctx = raw(c, 'GET', '/getAppContext', null)
        assert ctx.statusCode == 200: "GET /getAppContext -> ${ctx.statusCode}"
        Response choose = raw(c, 'PUT', "/chooseLocation/${locationId}", null)
        assert choose.statusCode == 200: "PUT /chooseLocation -> ${choose.statusCode} ${choose.asString()}"
        return c
    }

    /** The location the app session chooses; specs that run in Test Mobile DC override this. */
    String sessionLocationId() {
        return '1'
    }

    Cookie session() {
        if (appCookie == null) {
            appCookie = appSignIn('admin', 'password', sessionLocationId())
        }
        return appCookie
    }

    Response raw(Cookie cookie, String method, String url, Object body) {
        RequestSpecification spec = RestAssured.given()
                .urlEncodingEnabled(false)
                .config(RestAssured.config().redirect(RedirectConfig.redirectConfig().followRedirects(false)))
                .header('Accept', AXIOS_ACCEPT)
        if (cookie) {
            spec = spec.cookie(cookie)
        }
        if (body != null) {
            spec = spec.contentType('application/json').body(body instanceof String ? body : JsonOutput.toJson(body))
        }
        return spec.request(method, url)
    }

    static String enc(Object v) {
        return URLEncoder.encode(String.valueOf(v), 'UTF-8').replace('+', '%20')
    }

    /** Builds the app's URL for a contract entry from placeholder values. */
    static String url(String id, Map vars) {
        Map e = MobileContract.entry(id)
        String path = (e.path as String).replaceAll(/\{([^}]+)\}/) { all, String name ->
            if (!vars.containsKey(name)) {
                throw new IllegalArgumentException("${id}: no value for path placeholder {${name}}")
            }
            enc(vars[name])
        }
        List<String> query = []
        (e.query as List<Map>).each { Map q ->
            String value = q.value as String
            def m = value =~ /^\{([^}]+)\}$/
            if (m.matches()) {
                String name = m.group(1)
                if (!vars.containsKey(name) || vars[name] == null) {
                    if (q.optional) {
                        return
                    }
                    throw new IllegalArgumentException("${id}: no value for query placeholder {${name}}")
                }
                def v = vars[name]
                (v instanceof Collection ? v : [v]).each { query << "${q.name}=${enc(it)}".toString() }
            } else if (value.contains('{')) {
                throw new IllegalArgumentException("${id}: partial placeholder in ${q.name}=${value}")
            } else {
                query << "${q.name}=${value}".toString()
            }
        }
        if (e.extraQueryFrom && vars[e.extraQueryFrom] instanceof Map) {
            (vars[e.extraQueryFrom] as Map).each { k, v -> query << "${k}=${enc(v)}".toString() }
        }
        if (e.axiosConfig) {
            String name = (e.axiosConfig as String)[1..-2]
            Map cfg = vars[name] as Map
            (cfg?.params as Map)?.each { k, v -> query << "${k}=${enc(v)}".toString() }
        }
        return query ? "${path}?${query.join('&')}" : path
    }

    /** Issues one contract call the app's way with the signed-in session. */
    CallResult call(String id, Map vars = [:], Object body = null, Cookie cookie = null) {
        Map e = MobileContract.entry(id)
        String u = url(id, vars)
        Object b = body
        if (b == null && e.method in ['POST', 'PUT', 'PATCH'] && e.body instanceof Map && (e.body as Map).isEmpty()) {
            b = [:]
        }
        Response r = raw(cookie ?: session(), e.method as String, u, b)
        String redirectedFrom = null
        if (r.statusCode in [301, 302, 303] && r.header('Location')) {
            // the app's XMLHttpRequest follows redirects (a POST becomes a GET), so the
            // app sees the target's answer; record where it came from
            redirectedFrom = "${r.statusCode} from ${e.method} ${u}".toString()
            r = raw(cookie ?: session(), 'GET', r.header('Location'), null)
        }
        CallResult result = CallResult.of(id, e.method as String, u, r.statusCode, r.contentType, r.asString())
        result.redirectedFrom = redirectedFrom
        println "[test-contract] ${result}"; if (System.getenv("REGRESSION_DEBUG")) { println "[test-contract-body] ${result.text}" }
        return result
    }
}
