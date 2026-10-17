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

    private static final String TEMPLATE_DIR = "grails-app/conf/templates/zpl-boarcodes-templates"

    void setupSpec() {
        mockDomains(Document, DocumentType)
    }

    void setup() {
        service.beanPropertyTemplateService = new BeanPropertyTemplateService()
    }

    private static Document document(String documentCode, String contents) {
        DocumentType documentType = documentCode ? new DocumentType(name: documentCode, documentCode: documentCode as DocumentCode) : null
        return new Document(name: "label", filename: "label.zpl", fileContents: contents.bytes, documentType: documentType)
    }

    private static String bundledTemplate(String name) {
        return new File("${TEMPLATE_DIR}/${name}.zpl").text
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

    @Unroll
    void "renderZpl should refuse a document of type #documentCode"() {
        given:
        Document document = document(documentCode, '^XA^FD${product.productCode}^FS^XZ')

        when:
        service.renderZpl(document, [product: new Product(productCode: "AB123")])

        then:
        IllegalArgumentException e = thrown()
        e.message.contains("ZEBRA_TEMPLATE")

        where:
        documentCode << ["SHIPPING_TEMPLATE", "IMAGE", null]
    }

    void "renderZpl should not evaluate expressions in a template"() {
        given:
        Document document = document("ZEBRA_TEMPLATE", '^XA^FD${7*7}^FS^XZ')

        when:
        service.renderZpl(document, [product: new Product(productCode: "AB123")])

        then:
        thrown(IllegalArgumentException)
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
        thrown(IllegalArgumentException)
        0 * zebraService.postData(_, _)

        when:
        zebraService.printDocument(document, model)

        then:
        thrown(IllegalArgumentException)
    }
}
