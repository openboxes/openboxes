package org.pih.warehouse.api.regression.obaf.labels

import fr.w3blog.zpl.model.ZebraPrintException
import grails.util.GrailsWebMockUtil
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.PendingFeature

import org.pih.warehouse.api.InternalLocationApiController
import org.pih.warehouse.api.MobileProductApiController
import org.pih.warehouse.api.regression.mobile.LabelStubs
import org.pih.warehouse.api.regression.obaf.events.support.LoopbackReceiver
import org.pih.warehouse.api.regression.obaf.support.ObafIntegrationSpec
import org.pih.warehouse.api.regression.support.CaptureServers
import org.pih.warehouse.core.BeanPropertyTemplateService
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.DocumentCode
import org.pih.warehouse.core.DocumentService
import org.pih.warehouse.core.DocumentType
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.ZebraService
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.ContainerApiController

/**
 * The label path behaviour: OBAF's three bundled ZPL
 * templates rendered by BeanPropertyTemplateService with real models (golden files under
 * src/integration-test/resources/regression-labels/), ZebraService.renderDocument against a
 * loopback labelary, printDocument against a loopback printer socket (written, refused,
 * unresponsive), the three printLabel actions when printing fails, and DocumentService's label
 * lookups. Labelary and the printer only ever point at 127.0.0.1.
 */
class ZebraLabelIntegrationSpec extends ObafIntegrationSpec {

    static final String TEMPLATES = 'grails-app/conf/templates/zpl-boarcodes-templates'

    @Autowired
    BeanPropertyTemplateService beanPropertyTemplateService

    @Autowired
    ZebraService zebraService

    @Autowired
    DocumentService documentService

    Map savedBarcodeConfig
    List<String> createdDocuments = []
    List<Closeable> closeables = []

    void setup() {
        savedBarcodeConfig = [ip  : grailsApplication.config.getProperty('openboxes.barcode.printer.ipAddress', String),
                              port: grailsApplication.config.getProperty('openboxes.barcode.printer.port', Integer),
                              url : grailsApplication.config.getProperty('openboxes.barcode.labelaryApi.url', String)]
    }

    void cleanup() {
        closeables.reverseEach { try { it.close() } catch (Exception ignored) { } }
        LabelStubs.set(grailsApplication, savedBarcodeConfig.ip as String, savedBarcodeConfig.port as Integer, savedBarcodeConfig.url as String)
        if (createdDocuments) {
            tx { createdDocuments.each { Document.get(it)?.delete(flush: true) } }
            createdDocuments.clear()
        }
    }

    // ------------------------------------------------------------------ fixtures

    String zebraDocument(String number, String zpl) {
        String id = tx {
            DocumentType zt = DocumentType.findByDocumentCode(DocumentCode.ZEBRA_TEMPLATE) ?:
                    new DocumentType(name: 'Zebra template', documentCode: DocumentCode.ZEBRA_TEMPLATE).save(flush: true, failOnError: true)
            new Document(name: number, filename: "${number.replace(':', '-')}.zpl".toString(), contentType: 'text/plain',
                    documentNumber: number, documentType: zt, fileContents: zpl.getBytes('UTF-8')).save(flush: true, failOnError: true).id
        } as String
        createdDocuments << id
        return id
    }

    static String bundled(String name) {
        return new File(TEMPLATES, "${name}.zpl").getText('UTF-8')
    }

    static String golden(String name) {
        return ZebraLabelIntegrationSpec.getResource("/regression-labels/${name}.zpl.golden").getText('UTF-8')
    }

    String labelProduct(String code) {
        String existing = tx { Product.findByProductCode(code)?.id } as String
        if (existing) {
            return existing
        }
        String id = product('LBL')
        tx { Product p = Product.get(id); p.productCode = code; p.save(flush: true, failOnError: true) }
        return id
    }

    String labelBin(String name) {
        String id = bin(depot('LBL-DC'), 'LBL')
        tx { Location b = Location.get(id); b.name = name; b.save(flush: true, failOnError: true) }
        return id
    }

    String render(String template, Map model) {
        return tx {
            Document d = new Document(name: "test-${template}", fileContents: bundled(template).getBytes('UTF-8'))
            beanPropertyTemplateService.renderTemplate(d, model.collectEntries { k, v -> [(k): v instanceof Closure ? v.call() : v] })
        } as String
    }

    CaptureServers.Printer printer() {
        CaptureServers.Printer p = new CaptureServers.Printer()
        closeables << p
        LabelStubs.set(grailsApplication, '127.0.0.1', p.port, 'http://127.0.0.1:9/test-unused/')
        return p
    }

    // ------------------------------------------------------------------ the bundled templates

    @spock.lang.Unroll
    void 'the bundled #template template renders the golden ZPL for a real #template'() {
        expect:
        render(template, model.call(this)) == golden(template)

        where:
        template           | model
        'product'          | { s -> String id = s.labelProduct('TEST-LBL-0001'); [product: { Product.get(id) }] }
        'container'        | { s -> [container: new Container(name: 'LPN-TEST-00001', containerNumber: 'LPN-TEST-00001')] }
        'internalLocation' | { s -> String id = s.labelBin('TEST-BIN-A01'); [internalLocation: { Location.get(id) }] }
    }

    void 'characterization: the bundled templates print the value twice, as text (with a leading space) and as a Code 128 barcode'() {
        expect:
        ['product', 'container', 'internalLocation'].every { String t ->
            String zpl = bundled(t)
            zpl.startsWith('^XA') && zpl.trim().endsWith('^XZ') && zpl.contains('^A0,40^FD ${') && zpl.contains('^BCN,60,,,,A^FD${')
        }
    }

    @spock.lang.Unroll
    void 'a missing model fails the #template template, naming the value it needed'() {
        when:
        render(template, [:])

        then:
        IllegalArgumentException e = thrown()
        e.message.contains("'${template}' was not provided")

        where:
        template << ['product', 'container', 'internalLocation']
    }

    // ------------------------------------------------------------------ renderDocument (labelary)

    void 'renderDocument POSTs the rendered ZPL (raw, labelled form-urlencoded) to the configured labelary URL and returns its answer'() {
        given:
        LoopbackReceiver labelary = new LoopbackReceiver()
        closeables << labelary
        labelary.contentType = 'image/png'
        labelary.responseBody = 'PNG-BYTES'
        LabelStubs.set(grailsApplication, '127.0.0.1', 9, labelary.url)
        String doc = zebraDocument("test-render-${uid()}", '^XA^FD${product.productCode}^FS^XZ')
        String p = labelProduct('TEST-LBL-0001')

        when:
        String answer = tx { zebraService.renderDocument(Document.get(doc), [product: Product.get(p)]).getText('UTF-8') } as String

        then:
        labelary.hits.size() == 1
        labelary.hits[0].method == 'POST'
        labelary.hits[0].headers['content-type'].startsWith('application/x-www-form-urlencoded')
        labelary.hits[0].body == '^XA^FDTEST-LBL-0001^FS^XZ'
        answer == 'PNG-BYTES'
    }

    @PendingFeature(reason = 'ZebraService.groovy:30-38 returns labelary\'s response body whatever the status: an invalid label (labelary 400 "ERROR: ...") or a labelary outage (5xx) is streamed to the app as the label image with content type image/png')
    void 'renderDocument refuses a labelary error answer instead of passing it on as the image'() {
        given:
        LoopbackReceiver labelary = new LoopbackReceiver()
        closeables << labelary
        labelary.status = 400
        labelary.contentType = 'text/plain'
        labelary.responseBody = 'ERROR: Label has no content'
        LabelStubs.set(grailsApplication, '127.0.0.1', 9, labelary.url)
        String doc = zebraDocument("test-render-err-${uid()}", '^XA^XZ')

        when:
        tx { zebraService.renderDocument(Document.get(doc), [:]).getText('UTF-8') }

        then:
        thrown(Exception)
    }

    // ------------------------------------------------------------------ printDocument (raw TCP)

    void 'printDocument writes the rendered ZPL to the printer socket, framed by DataOutputStream.writeUTF (a two-byte length first)'() {
        given:
        CaptureServers.Printer printer = printer()
        String doc = zebraDocument("test-print-${uid()}", '^XA^FD${product.productCode}^FS^XZ')
        String p = labelProduct('TEST-LBL-0001')
        String zpl = '^XA^FDTEST-LBL-0001^FS^XZ'

        when:
        tx { zebraService.printDocument(Document.get(doc), [product: Product.get(p)]) }
        String job = printer.awaitJobs(1)[0]

        then: 'the printer gets the ZPL preceded by its length as two big-endian bytes (here 0x00 0x18)'
        job.endsWith(zpl)
        job.length() == zpl.length() + 2
        (int) job.charAt(0) == 0
        (int) job.charAt(1) == zpl.length()
    }

    void 'a printer that refuses the connection fails the print at once with a ZebraPrintException'() {
        given:
        LabelStubs.set(grailsApplication, '127.0.0.1', LoopbackReceiver.closedPort(), 'http://127.0.0.1:9/test-unused/')
        String doc = zebraDocument("test-print-refused-${uid()}", '^XA^XZ')
        long t0 = System.currentTimeMillis()

        when:
        tx { zebraService.printDocument(Document.get(doc), [:]) }

        then:
        ZebraPrintException e = thrown()
        System.currentTimeMillis() - t0 < 3000
        e.message.contains('127.0.0.1')
    }

    // ------------------------------------------------------------------ the printLabel actions

    Map callController(Object controller, Map params, Closure<Void> action) {
        GrailsWebRequest web = GrailsWebMockUtil.bindMockWebRequest(grails.util.Holders.applicationContext)
        try {
            web.currentRequest.method = 'POST'
            params.each { k, v -> web.params.put(k, v) }
            tx {
                web.session.setAttribute('warehouse', Location.get(params.facility as String))
                web.session.setAttribute('user', org.pih.warehouse.core.User.findByUsername('admin'))
                action.call(controller)
            }
            return [status: web.currentResponse.status, body: web.currentResponse.contentAsString]
        } finally {
            RequestContextHolder.resetRequestAttributes()
        }
    }

    @PendingFeature(reason = 'the three printLabel actions (MobileProductApiController.groovy:98-111, ContainerApiController.groovy:45-57, InternalLocationApiController.groovy:100-114) catch every exception and render {errorCode: 500, ...} with HTTP 200: a print that failed (printer refused, unknown document) reads as printed to any client that checks the status (the app shows "printed"); the mobile contract suite pends the same at HTTP level')
    void 'a failed print answers an error status from each printLabel action'() {
        given:
        LabelStubs.set(grailsApplication, '127.0.0.1', LoopbackReceiver.closedPort(), 'http://127.0.0.1:9/test-unused/')
        String dc = depot('LBL-PR')
        String binId = bin(dc, 'LBLPR')
        String p = labelProduct('TEST-LBL-0001')
        String doc = zebraDocument("test-print-fail-${uid()}", '^XA^XZ')
        def ctx = grails.util.Holders.applicationContext

        when:
        List<Map> answers = [
                callController(ctx.getBean(MobileProductApiController), [id: p, documentId: doc, facility: dc]) { it.printLabel() },
                callController(ctx.getBean(ContainerApiController), [id: 'no-such-container', documentId: doc, facility: dc]) { it.printLabel() },
                callController(ctx.getBean(InternalLocationApiController), [id: binId, documentId: doc, facility: dc]) { it.printLabel() },
        ]

        then: 'today each body is {errorCode: 500, ...} under status 200'
        answers.every { it.body.contains('"errorCode":500') }
        answers.every { it.status >= 400 }
    }

    // ------------------------------------------------------------------ DocumentService label lookups

    void 'getProductBarcodeLabel / getContainerBarcodeLabel / getInternalLocationBarcodeLabel link the ZEBRA_TEMPLATE numbered barcodeLabel:<kind>'() {
        given:
        String number = "test-lookup-${uid()}"
        String doc = zebraDocument(number, '^XA^XZ')

        when:
        Map label = withWebRequest { tx { documentService.getBarcodeLabel(number, '/api/products/%s/labels/%s', 'CODE-1') } } as Map

        then:
        label.id == doc
        label.name == number
        label.url.endsWith("/api/products/CODE-1/labels/${doc}")
        label.url.startsWith('http')

        and: 'no document with that number: null'
        withWebRequest { tx { documentService.getBarcodeLabel("test-none-${uid()}", '/api/products/%s/labels/%s', 'CODE-1') } } == null
    }

    @PendingFeature(reason = 'DocumentService.groovy:108-115 getDocument looks the label template up with createCriteria().get: a second ZEBRA_TEMPLATE with the same number (an admin uploading a new version) makes it throw NonUniqueResultException, and with it product details, container details and every label link (the mobile contract suite builds its templates "once" to avoid exactly this)')
    void 'two templates with the same label number: the lookup still answers one of them'() {
        given:
        String number = "test-dup-${uid()}"
        String first = zebraDocument(number, '^XA^FDv1^XZ')
        String second = zebraDocument(number, '^XA^FDv2^XZ')

        when:
        Map label = withWebRequest { tx { documentService.getBarcodeLabel(number, '/api/products/%s/labels/%s', 'CODE-1') } } as Map

        then:
        label.id in [first, second]
    }
}
