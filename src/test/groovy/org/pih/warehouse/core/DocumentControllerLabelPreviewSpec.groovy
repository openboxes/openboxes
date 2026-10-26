package org.pih.warehouse.core

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product
import spock.lang.Specification

/**
 * The label preview (renderZebraTemplate) and the viewer link (exportZebraTemplate) use the
 * Labelary URLs configured under openboxes.barcode.labelaryApi and can be turned off.
 */
class DocumentControllerLabelPreviewSpec extends Specification implements ControllerUnitTest<DocumentController>, DataTest {

    private static final List<String> LABELARY_OPTIONS = ["enabled", "documentPreviewUrl", "viewerUrl"]

    private Map savedLabelaryConfig
    private List<List<String>> previewRequests = []

    void setupSpec() {
        mockDomains(Document, DocumentType, Location, InventoryItem, Product)
    }

    void setup() {
        Map labelaryApi = grailsApplication.config.openboxes.barcode.labelaryApi
        savedLabelaryConfig = LABELARY_OPTIONS.collectEntries { [(it): labelaryApi.containsKey(it) ? labelaryApi[it] : null] }

        controller.beanPropertyTemplateService = new BeanPropertyTemplateService()
        controller.zebraService = Spy(ZebraService) {
            requestImage(_, _) >> { String url, String zpl -> previewRequests << [url, zpl]; new ByteArrayInputStream("png".bytes) }
            postData(_, _) >> { throw new AssertionError("no label API request expected") }
        }
        controller.zebraService.grailsApplication = grailsApplication

        Location facility = new Location(name: "Main Warehouse").save(validate: false)
        Product product = new Product(productCode: "AB 1&2", name: "Gauze").save(validate: false)
        DocumentType zebraType = new DocumentType(name: "Zebra", documentCode: DocumentCode.ZEBRA_TEMPLATE).save(validate: false)
        Document template = new Document(name: "label", filename: "label.zpl", fileContents: '^XA^FD${product.productCode}^FS^XZ'.bytes,
                documentType: zebraType).save(validate: false)
        session.warehouse = [id: facility.id]
        params.id = template.id
        params.product = [id: product.id]
    }

    void cleanup() {
        savedLabelaryConfig.each { String name, Object value -> setLabelary(name, value) }
    }

    private void setLabelary(String name, Object value) {
        if (value == null) {
            grailsApplication.config.openboxes.barcode.labelaryApi.remove(name)
        } else {
            grailsApplication.config.openboxes.barcode.labelaryApi."${name}" = value
        }
    }

    void "renderZebraTemplate should send the rendered label to the configured document preview URL"() {
        given:
        setLabelary("documentPreviewUrl", "http://localhost/preview/")

        when:
        controller.renderZebraTemplate()

        then:
        previewRequests == [["http://localhost/preview/", "^XA^FDAB 1&2^FS^XZ"]]
        response.status == 200
        response.contentType == "image/png"
        response.contentAsByteArray == "png".bytes
    }

    void "renderZebraTemplate should use the default document preview URL when none is configured"() {
        given:
        setLabelary("documentPreviewUrl", null)

        when:
        controller.renderZebraTemplate()

        then:
        previewRequests == [["https://api.labelary.com/v1/printers/8dpmm/labels/3x2/0/", "^XA^FDAB 1&2^FS^XZ"]]
    }

    void "renderZebraTemplate should answer 503 without a request when label previews are turned off"() {
        given:
        setLabelary("documentPreviewUrl", "http://localhost/preview/")
        setLabelary("enabled", false)

        when:
        controller.renderZebraTemplate()

        then:
        previewRequests.isEmpty()
        response.status == 503
        response.text.contains("Label previews are turned off")
    }

    void "exportZebraTemplate should redirect to the configured viewer with the label URL-encoded"() {
        given:
        setLabelary("viewerUrl", "http://localhost/viewer.html")

        when:
        controller.exportZebraTemplate()

        then:
        response.status == 302
        response.redirectedUrl == "http://localhost/viewer.html?zpl=%5EXA%5EFDAB%201%262%5EFS%5EXZ"
        previewRequests.isEmpty()
    }

    void "exportZebraTemplate should use the default viewer when none is configured"() {
        given:
        setLabelary("viewerUrl", null)

        when:
        controller.exportZebraTemplate()

        then:
        response.redirectedUrl == "https://labelary.com/viewer.html?zpl=%5EXA%5EFDAB%201%262%5EFS%5EXZ"
    }

    void "exportZebraTemplate should answer 503 without a redirect when label previews are turned off"() {
        given:
        setLabelary("enabled", false)

        when:
        controller.exportZebraTemplate()

        then:
        response.status == 503
        response.redirectedUrl == null
        response.text.contains("Label previews are turned off")
    }
}
