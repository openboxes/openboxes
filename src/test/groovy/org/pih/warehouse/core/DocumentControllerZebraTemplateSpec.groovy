package org.pih.warehouse.core

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.hibernate.ObjectNotFoundException
import spock.lang.Specification
import org.pih.warehouse.inventory.InventoryItem

/**
 * Zebra template rendering endpoints should reject documents
 * that are not of type ZEBRA_TEMPLATE.
 */
class DocumentControllerZebraTemplateSpec extends Specification implements ControllerUnitTest<DocumentController>, DataTest {

    void setupSpec() {
        mockDomains(Document, DocumentType, Location, InventoryItem)
    }

    private Document createZebraTemplateDocument() {
        DocumentType zebraType = new DocumentType(
            name: "Zebra Label Template",
            documentCode: DocumentCode.ZEBRA_TEMPLATE,
        ).save(failOnError: true, validate: false)

        return new Document(
            name: "test-zebra-template",
            filename: "label.zpl",
            fileContents: "^XA^FO50,50^ADN,36,20^FDHello^FS^XZ".bytes,
            contentType: "text/plain",
            documentType: zebraType,
        ).save(failOnError: true, validate: false)
    }

    /*
     * At the controller level we only guard against invalid document types.
     * Content-level safety is tested in BeanPropertyTemplateServiceSpec.
     */
    private Document createShippingDocument() {
        DocumentType shippingType = new DocumentType(
            name: "Shipping Document",
            documentCode: DocumentCode.SHIPPING_DOCUMENT,
        ).save(failOnError: true, validate: false)

        return new Document(
            name: "test-shipping-document",
            filename: "shipping.ext",
            fileContents: "unexpected contents".bytes,
            contentType: "text/plain",
            documentType: shippingType,
        ).save(failOnError: true, validate: false)
    }

    private Document createUntypedDocument() {
        return new Document(
            name: "untyped-document",
            filename: "untyped.txt",
            fileContents: "some content".bytes,
            contentType: "text/plain",
            documentType: null,
        ).save(failOnError: true, validate: false)
    }

    void "buildZebraTemplate should reject nonexistent document ID"() {
        given:
        controller.params.id = "no-such-document"

        when:
        controller.buildZebraTemplate()

        then:
        thrown(ObjectNotFoundException)
    }

    void "buildZebraTemplate should reject non-ZEBRA_TEMPLATE documents"() {
        given:
        Document doc = createShippingDocument()
        controller.params.id = doc.id

        when:
        controller.buildZebraTemplate()

        then:
        thrown(IllegalArgumentException)
    }

    void "buildZebraTemplate should reject documents with no type"() {
        given:
        Document doc = createUntypedDocument()
        controller.params.id = doc.id

        when:
        controller.buildZebraTemplate()

        then:
        thrown(IllegalArgumentException)
    }

    void "buildZebraTemplate should accept ZEBRA_TEMPLATE documents"() {
        given:
        Document doc = createZebraTemplateDocument()
        controller.params.id = doc.id
        controller.beanPropertyTemplateService = Stub(BeanPropertyTemplateService) {
            renderTemplate(_ as Document, _ as Map) >> "rendered-content"
        }
        session.warehouse = [id: "warehouse-1"]

        when:
        controller.buildZebraTemplate()

        then:
        noExceptionThrown()
        response.text == "rendered-content"
    }

    void "printZebraTemplate should reject non-ZEBRA_TEMPLATE documents"() {
        given:
        Document doc = createShippingDocument()
        controller.params.id = doc.id

        when:
        controller.printZebraTemplate()

        then:
        thrown(IllegalArgumentException)
    }

    void "renderZebraTemplate should reject non-ZEBRA_TEMPLATE documents"() {
        given:
        Document doc = createShippingDocument()
        controller.params.id = doc.id

        when:
        controller.renderZebraTemplate()

        then:
        thrown(IllegalArgumentException)
    }

    void "exportZebraTemplate should reject non-ZEBRA_TEMPLATE documents"() {
        given:
        Document doc = createShippingDocument()
        controller.params.id = doc.id

        when:
        controller.exportZebraTemplate()

        then:
        thrown(IllegalArgumentException)
    }
}
