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

    private static Document saveDocument(DocumentCode documentCode) {
        DocumentType documentType = new DocumentType(name: documentCode.name(), documentCode: documentCode).save(validate: false)
        return new Document(name: "label", filename: "label.zpl", fileContents: '^XA^FD${internalLocation.name}^FS^XZ'.bytes,
                documentType: documentType).save(validate: false)
    }

    void "renderLabel should refuse a document that is not a Zebra template"() {
        given:
        params.id = "bin-1"
        params.documentId = saveDocument(DocumentCode.SHIPPING_TEMPLATE).id

        when:
        controller.renderLabel()

        then:
        thrown(IllegalArgumentException)
    }

    void "printLabel should refuse a document that is not a Zebra template"() {
        given:
        params.id = "bin-1"
        params.documentId = saveDocument(DocumentCode.SHIPPING_TEMPLATE).id

        when:
        controller.printLabel()

        then:
        response.json.errorCode == 500
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
}
