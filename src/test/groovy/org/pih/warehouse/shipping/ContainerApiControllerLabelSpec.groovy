package org.pih.warehouse.shipping

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.pih.warehouse.core.BeanPropertyTemplateService
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.DocumentCode
import org.pih.warehouse.core.DocumentType
import org.pih.warehouse.core.ZebraService
import spock.lang.Specification

/**
 * Container labels are rendered and printed only from ZEBRA_TEMPLATE documents.
 */
class ContainerApiControllerLabelSpec extends Specification implements ControllerUnitTest<ContainerApiController>, DataTest {

    void setupSpec() {
        mockDomains(Document, DocumentType, Container)
    }

    void setup() {
        controller.zebraService = Spy(ZebraService) {
            postData(_, _) >> { throw new AssertionError("no render request expected") }
        }
        controller.zebraService.beanPropertyTemplateService = new BeanPropertyTemplateService()
    }

    private static Document saveDocument(DocumentCode documentCode) {
        DocumentType documentType = new DocumentType(name: documentCode.name(), documentCode: documentCode).save(validate: false)
        return new Document(name: "label", filename: "label.zpl", fileContents: '^XA^FD${container.name}^FS^XZ'.bytes,
                documentType: documentType).save(validate: false)
    }

    private static Container saveContainer() {
        return new Container(name: "Pallet 7").save(validate: false)
    }

    void "renderLabel should refuse a document that is not a Zebra template"() {
        given:
        params.id = saveContainer().id
        params.documentId = saveDocument(DocumentCode.SHIPPING_TEMPLATE).id

        when:
        controller.renderLabel()

        then:
        thrown(IllegalArgumentException)
    }

    void "printLabel should refuse a document that is not a Zebra template"() {
        given:
        params.id = saveContainer().id
        params.documentId = saveDocument(DocumentCode.SHIPPING_TEMPLATE).id

        when:
        controller.printLabel()

        then:
        response.json.errorCode == 500
        response.json.errorMessage.contains("ZEBRA_TEMPLATE")
    }

    void "renderLabel should render a Zebra template with the container"() {
        given:
        params.id = saveContainer().id
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
        body == '^XA^FDPallet 7^FS^XZ'
        response.contentType == "image/png"
    }
}
