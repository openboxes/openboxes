package org.pih.warehouse.api

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.pih.warehouse.core.BeanPropertyTemplateService
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.DocumentCode
import org.pih.warehouse.core.DocumentType
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.core.ZebraService
import spock.lang.Specification

/**
 * Bin location labels are rendered and printed only from ZEBRA_TEMPLATE documents.
 */
class InternalLocationApiControllerLabelSpec extends Specification implements ControllerUnitTest<InternalLocationApiController>, DataTest {

    void setupSpec() {
        mockDomains(Document, DocumentType)
    }

    void setup() {
        controller.zebraService = Spy(ZebraService) {
            postData(_, _) >> { throw new AssertionError("no render request expected") }
        }
        controller.zebraService.beanPropertyTemplateService = new BeanPropertyTemplateService()
        controller.locationService = Stub(LocationService) {
            getInternalLocation(_, _) >> new Location(name: "A-01-02")
        }
        session.warehouse = [id: "warehouse-1"]
    }

    private String labelSubjectId() {
        return "bin-1"
    }

    private static Document saveDocument(DocumentCode documentCode, String contents = null) {
        DocumentType documentType = new DocumentType(name: documentCode.name(), documentCode: documentCode).save(validate: false)
        return new Document(name: "label", filename: "label.zpl", fileContents: (contents ?: '^XA^FD${internalLocation.name}^FS^XZ').bytes,
                documentType: documentType).save(validate: false)
    }

    void "renderLabel should refuse a document that is not a Zebra template"() {
        given:
        params.id = "bin-1"
        params.documentId = saveDocument(DocumentCode.SHIPPING_TEMPLATE).id

        when:
        controller.renderLabel()

        then:
        response.status == 400
        response.json.errorCode == 400
        response.json.errorMessage.contains("ZEBRA_TEMPLATE")
    }

    void "printLabel should refuse a document that is not a Zebra template"() {
        given:
        params.id = "bin-1"
        params.documentId = saveDocument(DocumentCode.SHIPPING_TEMPLATE).id

        when:
        controller.printLabel()

        then:
        response.status == 400
        response.json.errorCode == 400
        response.json.errorMessage.contains("ZEBRA_TEMPLATE")
    }

    void "renderLabel should render a Zebra template with the bin location"() {
        given:
        params.id = "bin-1"
        params.documentId = saveDocument(DocumentCode.ZEBRA_TEMPLATE).id
        String body = null
        controller.zebraService = Spy(ZebraService) {
            postData(_, _) >> { String url, String zpl -> body = zpl; new ByteArrayInputStream("png".bytes) }
        }
        controller.zebraService.beanPropertyTemplateService = new BeanPropertyTemplateService()
        controller.zebraService.grailsApplication = grailsApplication

        when:
        controller.renderLabel()

        then:
        body == '^XA^FDA-01-02^FS^XZ'
        response.contentType == "image/png"
    }

    void "renderLabel and printLabel should answer 400 with the template's name for a template the service refuses"() {
        given:
        params.id = labelSubjectId()
        params.documentId = saveDocument(DocumentCode.ZEBRA_TEMPLATE, '^XA^FD${7*191}^FS^XZ').id

        when:
        controller.renderLabel()

        then:
        response.status == 400
        response.json.errorMessage.contains("Label template 'label'")
        !response.text.contains("1337")

        when:
        response.reset()
        controller.printLabel()

        then:
        response.status == 400
        response.json.errorMessage.contains("Label template 'label'")
    }

    void "renderLabel should answer 503 without a render request when label previews are turned off"() {
        given:
        params.id = labelSubjectId()
        params.documentId = saveDocument(DocumentCode.ZEBRA_TEMPLATE).id
        controller.zebraService.grailsApplication = grailsApplication
        Map labelaryApi = grailsApplication.config.openboxes.barcode.labelaryApi
        boolean wasSet = labelaryApi.containsKey("enabled")
        Object enabled = labelaryApi.enabled
        labelaryApi.enabled = false

        when: "a render request would fail the test (see setup)"
        controller.renderLabel()

        then:
        response.status == 503
        response.json.errorCode == 503
        response.json.errorMessage.contains("Label previews are turned off")

        cleanup:
        if (wasSet) {
            labelaryApi.enabled = enabled
        } else {
            labelaryApi.remove("enabled")
        }
    }
}
