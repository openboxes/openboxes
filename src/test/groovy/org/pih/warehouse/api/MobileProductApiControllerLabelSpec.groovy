package org.pih.warehouse.api

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.pih.warehouse.core.BeanPropertyTemplateService
import org.pih.warehouse.core.Document
import org.pih.warehouse.core.DocumentCode
import org.pih.warehouse.core.DocumentType
import org.pih.warehouse.core.ZebraService
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductService
import spock.lang.Specification

/**
 * Product labels are rendered and printed only from ZEBRA_TEMPLATE documents.
 */
class MobileProductApiControllerLabelSpec extends Specification implements ControllerUnitTest<MobileProductApiController>, DataTest {

    void setupSpec() {
        mockDomains(Document, DocumentType)
    }

    void setup() {
        controller.zebraService = Spy(ZebraService) {
            postData(_, _) >> { throw new AssertionError("no render request expected") }
        }
        controller.zebraService.beanPropertyTemplateService = new BeanPropertyTemplateService()
        controller.productService = Stub(ProductService) {
            getProduct(_) >> new Product(productCode: "AB123")
        }
    }

    private static Document saveDocument(DocumentCode documentCode) {
        DocumentType documentType = new DocumentType(name: documentCode.name(), documentCode: documentCode).save(validate: false)
        return new Document(name: "label", filename: "label.zpl", fileContents: '^XA^FD${product.productCode}^FS^XZ'.bytes,
                documentType: documentType).save(validate: false)
    }

    void "renderLabel should refuse a document that is not a Zebra template"() {
        given:
        params.id = "product-1"
        params.documentId = saveDocument(DocumentCode.IMAGE).id

        when:
        controller.renderLabel()

        then:
        thrown(IllegalArgumentException)
    }

    void "printLabel should refuse a document that is not a Zebra template"() {
        given:
        params.id = "product-1"
        params.documentId = saveDocument(DocumentCode.IMAGE).id

        when:
        controller.printLabel()

        then:
        response.json.errorCode == 500
        response.json.errorMessage.contains("ZEBRA_TEMPLATE")
    }

    void "renderLabel should render a Zebra template with the product"() {
        given:
        params.id = "product-1"
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
        body == '^XA^FDAB123^FS^XZ'
        response.contentType == "image/png"
    }
}
