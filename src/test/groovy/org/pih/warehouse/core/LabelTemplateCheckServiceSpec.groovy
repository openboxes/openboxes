package org.pih.warehouse.core

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

/**
 * The startup check reports the stored label templates BeanPropertyTemplateService will refuse.
 */
class LabelTemplateCheckServiceSpec extends Specification implements ServiceUnitTest<LabelTemplateCheckService>, DataTest {

    void setupSpec() {
        mockDomains(Document, DocumentType)
    }

    @Unroll
    void "findProblems should accept #template"() {
        expect:
        service.findProblems(template) == []

        where:
        template << [
                '^XA^FD${product.productCode}^FS^XZ',
                '^XA^FD${internalLocation.name}^FS^FD${container.name}^FS^XZ',
                '^XA^FD${ inventoryItem.product.name }^FS^XZ',
                '^XA^FD${product.tags[0]}^FS^XZ',
                '^XA^FD${facility}^FS^XZ',
                '^XA^FD$${literal}^FS^XZ',
                '^XA^FO50,60^FDno placeholders^FS^XZ',
        ]
    }

    @Unroll
    void "findProblems should report #reason"() {
        when:
        List<String> problems = service.findProblems(template)

        then:
        problems.size() == 1
        problems[0].contains(expected)

        where:
        reason               | template                                  || expected
        "safe navigation"    | '^XA^FD${container?.name}^FS^XZ'          || 'uses ?.'
        "GSP tags"           | '<% if (true) { %>^XA^XZ<% } %>'          || 'GSP tags'
        "a tag library call" | '<g:set var="x" value="1"/>^XA^XZ'        || 'GSP tags'
        "an expression"      | '^XA^FD${7*191}^FS^XZ'                    || 'is an expression'
        "a method call"      | '^XA^FD${product.name.toUpperCase()}^FS^XZ' || 'is an expression'
        "root indexing"      | '^XA^FD${product[0]}^FS^XZ'               || 'is an expression'
        "an unknown name"    | '^XA^FD${shipment.name}^FS^XZ'            || "reads 'shipment'"
        "an empty template"  | ''                                        || 'empty'
    }

    void "checkStoredTemplates should name each stored Zebra template that will not render, and only those"() {
        given:
        DocumentType zebra = new DocumentType(name: "Zebra", documentCode: DocumentCode.ZEBRA_TEMPLATE).save(validate: false, flush: true)
        DocumentType shipping = new DocumentType(name: "Shipping", documentCode: DocumentCode.SHIPPING_TEMPLATE).save(validate: false, flush: true)
        save("good", "barcodeLabel:product", zebra, '^XA^FD${product.productCode}^FS^XZ')
        save("old container", "barcodeLabel:container", zebra, '^XA^FD${container?.name}^FS^XZ')
        save("not a label", "x", shipping, '${a?.b}')

        when:
        List<String> warnings = service.checkStoredTemplates()

        then:
        warnings.size() == 1
        warnings[0].contains("Label template 'old container' (barcodeLabel:container)")
        warnings[0].contains("uses ?.")
    }

    void "the startup listener should never throw"() {
        given:
        LabelTemplateCheckService failing = Spy(LabelTemplateCheckService) {
            checkStoredTemplates() >> { throw new IllegalStateException("database unavailable") }
        }

        when:
        failing.onApplicationEvent(null)

        then:
        noExceptionThrown()
    }

    private static Document save(String name, String documentNumber, DocumentType documentType, String contents) {
        return new Document(name: name, documentNumber: documentNumber, filename: "${name}.zpl", fileContents: contents.bytes,
                documentType: documentType).save(validate: false, flush: true)
    }
}
