package org.pih.warehouse.core

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Container
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Zebra labels are rendered through BeanPropertyTemplateService and only from
 * ZEBRA_TEMPLATE documents.
 */
class ZebraServiceSpec extends Specification implements ServiceUnitTest<ZebraService>, DataTest {

    private static final String TEMPLATE_DIR = "/templates/zpl-boarcodes-templates"

    void setupSpec() {
        mockDomains(Document, DocumentType)
    }

    private static final List<String> LABELARY_OPTIONS = ["enabled", "url", "documentPreviewUrl", "viewerUrl"]

    private Map savedLabelaryConfig

    void setup() {
        service.beanPropertyTemplateService = new BeanPropertyTemplateService()
        Map labelaryApi = grailsApplication.config.openboxes.barcode.labelaryApi
        savedLabelaryConfig = LABELARY_OPTIONS.collectEntries { [(it): labelaryApi.containsKey(it) ? labelaryApi[it] : null] }
    }

    void cleanup() {
        savedLabelaryConfig.each { String name, Object value -> setLabelary(name, value) }
    }

    /** Sets (or, for null, removes) one openboxes.barcode.labelaryApi option. */
    private void setLabelary(String name, Object value) {
        if (value == null) {
            grailsApplication.config.openboxes.barcode.labelaryApi.remove(name)
        } else {
            grailsApplication.config.openboxes.barcode.labelaryApi."${name}" = value
        }
    }

    private ZebraService spyService() {
        ZebraService zebraService = Spy(ZebraService)
        zebraService.beanPropertyTemplateService = new BeanPropertyTemplateService()
        zebraService.grailsApplication = grailsApplication
        return zebraService
    }

    private static Document document(String documentCode, String contents) {
        DocumentType documentType = documentCode ? new DocumentType(name: documentCode, documentCode: documentCode as DocumentCode) : null
        return new Document(name: "label", filename: "label.zpl", fileContents: contents.bytes, documentType: documentType)
    }

    private static String bundledTemplate(String name) {
        URL template = ZebraServiceSpec.getResource("${TEMPLATE_DIR}/${name}.zpl")
        assert template: "bundled template ${name}.zpl not found on the classpath"
        return template.text
    }

    @Unroll
    void "renderZpl should render the bundled #template template with its label model"() {
        given:
        Document document = document("ZEBRA_TEMPLATE", bundledTemplate(template))

        when:
        String zpl = service.renderZpl(document, model)

        then:
        zpl.contains("^FD ${expected}^FS")
        zpl.contains("^FD${expected}^FS")
        !zpl.contains('${')

        where:
        template           | model                                                     || expected
        "product"          | [product: new Product(productCode: "AB123")]               || "AB123"
        "internalLocation" | [internalLocation: new Location(name: "A-01-02")]          || "A-01-02"
        "container"        | [container: new Container(name: "Pallet 7")]               || "Pallet 7"
    }

    void "renderZpl should insert values as they are, without HTML encoding"() {
        given:
        Document document = document("ZEBRA_TEMPLATE", bundledTemplate("internalLocation"))

        when:
        String zpl = service.renderZpl(document, [internalLocation: new Location(name: 'Aisle A&B <"1">')])

        then:
        zpl.contains('^FDAisle A&B <"1">^FS')
    }

    @Unroll
    void "renderZpl should refuse a document of type #documentCode"() {
        given:
        Document document = document(documentCode, '^XA^FD${product.productCode}^FS^XZ')

        when:
        service.renderZpl(document, [product: new Product(productCode: "AB123")])

        then:
        LabelTemplateException e = thrown()
        e.message.contains("ZEBRA_TEMPLATE")

        where:
        documentCode << ["SHIPPING_TEMPLATE", "IMAGE", null]
    }

    @Unroll
    void "renderZpl should report a template it cannot render (#reason) with the template's name"() {
        given:
        Document document = document("ZEBRA_TEMPLATE", template)
        document.documentNumber = "barcodeLabel:product"

        when:
        service.renderZpl(document, [product: new Product(productCode: "AB123")])

        then:
        LabelTemplateException e = thrown()
        e.message.contains("Label template 'label' (barcodeLabel:product) can't be rendered")
        e.message.contains("upload it again")

        where:
        reason                   | template
        "an expression"          | '^XA^FD${7*7}^FS^XZ'
        "safe navigation"        | '^XA^FD${product?.productCode}^FS^XZ'
        "GSP tags"               | '<% if (true) { %>^XA^XZ<% } %>'
        "a value not provided"   | '^XA^FD${location.name}^FS^XZ'
        "an empty association"   | '^XA^FD${product.category.name}^FS^XZ'
    }

    void "renderZpl should print the default of a placeholder whose link is empty"() {
        given: "a product without a category"
        Document document = document("ZEBRA_TEMPLATE", '^XA^FD${product.productCode}|${product.category.name:-}|${product.category.name:-none}^FS^XZ')
        document.documentNumber = "barcodeLabel:product"

        when:
        String zpl = service.renderZpl(document, [product: new Product(productCode: "AB123")])

        then:
        zpl == '^XA^FDAB123||none^FS^XZ'
    }

    void "renderDocument should send the rendered label to the configured render API"() {
        given:
        ZebraService zebraService = Spy(ZebraService)
        zebraService.beanPropertyTemplateService = new BeanPropertyTemplateService()
        zebraService.grailsApplication = grailsApplication
        grailsApplication.config.openboxes.barcode.labelaryApi.url = "http://localhost/render"
        Document document = document("ZEBRA_TEMPLATE", bundledTemplate("product"))

        when:
        InputStream image = zebraService.renderDocument(document, [product: new Product(productCode: "AB123")])

        then:
        1 * zebraService.postData("http://localhost/render", { it.contains("^FDAB123^FS") }) >> new ByteArrayInputStream("png".bytes)
        image.text == "png"
    }

    void "renderDocument and printDocument should refuse a document that is not a Zebra template before any request"() {
        given:
        ZebraService zebraService = Spy(ZebraService)
        zebraService.beanPropertyTemplateService = new BeanPropertyTemplateService()
        zebraService.grailsApplication = grailsApplication
        Document document = document("SHIPPING_TEMPLATE", '^XA^FD${product.productCode}^FS^XZ')
        Map model = [product: new Product(productCode: "AB123")]

        when:
        zebraService.renderDocument(document, model)

        then:
        thrown(LabelTemplateException)
        0 * zebraService.postData(_, _)

        when:
        zebraService.printDocument(document, model)

        then:
        thrown(LabelTemplateException)
    }

    void "renderDocument should use the default render URL when none is configured"() {
        given:
        ZebraService zebraService = spyService()
        setLabelary("url", null)
        Document document = document("ZEBRA_TEMPLATE", '^XA^FD${product.productCode}^FS^XZ')

        when:
        zebraService.renderDocument(document, [product: new Product(productCode: "AB123")])

        then:
        1 * zebraService.postData("https://api.labelary.com/v1/printers/12dpmm/labels/2x1/0/", "^XA^FDAB123^FS^XZ") >> new ByteArrayInputStream("png".bytes)
    }

    @Unroll
    void "renderDocument should make no request when label previews are turned off (enabled: #enabled)"() {
        given:
        ZebraService zebraService = spyService()
        setLabelary("url", "http://localhost/render")
        setLabelary("enabled", enabled)
        Document document = document("ZEBRA_TEMPLATE", '^XA^FD${product.productCode}^FS^XZ')

        when:
        zebraService.renderDocument(document, [product: new Product(productCode: "AB123")])

        then:
        LabelPreviewDisabledException e = thrown()
        e.message.contains("openboxes.barcode.labelaryApi.enabled")
        0 * zebraService.postData(_, _)

        where:
        enabled << [false, "false", " FALSE "]
    }

    @Unroll
    void "isLabelaryEnabled should be #expected for enabled: #enabled"() {
        given:
        ZebraService zebraService = spyService()
        setLabelary("enabled", enabled)

        expect:
        zebraService.labelaryEnabled == expected

        where:
        enabled || expected
        null    || true
        true    || true
        "true"  || true
        ""      || true
        false   || false
        "false" || false
    }

    void "renderPreview should send the ZPL to the configured document preview URL"() {
        given:
        ZebraService zebraService = spyService()
        setLabelary("documentPreviewUrl", "http://localhost/preview")

        when:
        InputStream image = zebraService.renderPreview("^XA^FDAB123^FS^XZ")

        then:
        1 * zebraService.requestImage("http://localhost/preview", "^XA^FDAB123^FS^XZ") >> new ByteArrayInputStream("png".bytes)
        0 * zebraService.postData(_, _)
        image.text == "png"
    }

    void "renderPreview should use the default document preview URL when none is configured"() {
        given:
        ZebraService zebraService = spyService()
        setLabelary("documentPreviewUrl", null)

        when:
        zebraService.renderPreview("^XA^XZ")

        then:
        1 * zebraService.requestImage("https://api.labelary.com/v1/printers/8dpmm/labels/3x2/0/", "^XA^XZ") >> new ByteArrayInputStream(new byte[0])
    }

    void "renderPreview should make no request when label previews are turned off"() {
        given:
        ZebraService zebraService = spyService()
        setLabelary("documentPreviewUrl", "http://localhost/preview")
        setLabelary("enabled", false)

        when:
        zebraService.renderPreview("^XA^XZ")

        then:
        thrown(LabelPreviewDisabledException)
        0 * zebraService.requestImage(_, _)
    }

    @Unroll
    void "getViewerUrl should URL-encode the ZPL for viewer URL #viewerUrl"() {
        given:
        ZebraService zebraService = spyService()
        setLabelary("viewerUrl", viewerUrl)

        expect:
        zebraService.getViewerUrl('^XA^FDA&B #1+2 50%?=^FS^XZ') == expected

        where:
        viewerUrl                        || expected
        null                             || "https://labelary.com/viewer.html?zpl=%5EXA%5EFDA%26B%20%231%2B2%2050%25%3F%3D%5EFS%5EXZ"
        "http://localhost/viewer.html"   || "http://localhost/viewer.html?zpl=%5EXA%5EFDA%26B%20%231%2B2%2050%25%3F%3D%5EFS%5EXZ"
        "http://localhost/viewer?dpmm=8" || "http://localhost/viewer?dpmm=8&zpl=%5EXA%5EFDA%26B%20%231%2B2%2050%25%3F%3D%5EFS%5EXZ"
    }

    void "getViewerUrl should refuse when label previews are turned off"() {
        given:
        ZebraService zebraService = spyService()
        setLabelary("enabled", false)

        when:
        zebraService.getViewerUrl("^XA^XZ")

        then:
        thrown(LabelPreviewDisabledException)
    }
}
