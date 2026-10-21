package org.pih.warehouse.core

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Container
import org.springframework.beans.BeansException

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
                '^XA^FD${location.name:-no bin}^FS^XZ',
                '',
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
        "a blocked property" | '^XA^FD${product.class.name}^FS^XZ'      || "reads 'class'"
        "an indexed blocked property" | '^XA^FD${product.constraints[0]}^FS^XZ' || "reads 'constraints'"
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

    @Unroll
    void "findProblems should agree with BeanPropertyTemplateService for #template"() {
        given: "a model where every allowed name and the properties these templates read are present"
        Product product = new Product(productCode: "AB123", name: "Gauze")
        Map model = [
                product         : product,
                location        : new Location(name: "A-01"),
                facility        : new Location(name: "Main"),
                internalLocation: new Location(name: "B-02"),
                container       : new Container(name: "Pallet 7"),
                inventoryItem   : new InventoryItem(lotNumber: "LOT-1", product: product),
                document        : new Document(name: "label"),
        ]
        boolean renders
        try {
            new BeanPropertyTemplateService().renderTemplateContents(template, "label", model)
            renders = true
        } catch (IllegalArgumentException | BeansException ignored) {
            renders = false
        }

        expect:
        service.findProblems(template).isEmpty() == renders

        where:
        template << [
                '${product.productCode}',
                '${ product.name }',
                '${inventoryItem.product.productCode}',
                '${container.name}${internalLocation.name}${facility.name}${location.name}',
                '${document.name}',
                '${product.name:-none}',
                '$${product.name}',
                '${product?.name}',
                '${7*191}',
                '${product.name.toUpperCase()}',
                '${shipment.name}',
                '${product.class}',
                '${product.constraints}',
                '${product[0]}',
                '<% out << 1 %>',
                '<g:set var="x" value="1"/>',
        ]
    }

    @Unroll
    void "findProblems and BeanPropertyTemplateService on #template with #reason (they agree except where the check cannot know the values)"() {
        given:
        boolean renders
        String rendered = null
        try {
            rendered = new BeanPropertyTemplateService().renderTemplateContents(template, "label", model)
            renders = true
        } catch (IllegalArgumentException | BeansException ignored) {
            renders = false
        }

        expect:
        service.findProblems(template).isEmpty() == checkAccepts
        renders == rendersExpected
        rendered == expected

        where:
        reason                          | template                       | model                                              || checkAccepts | rendersExpected | expected
        "no bin passed"                 | '${location.name:-no bin}'     | [location: null]                                   || true         | true            | 'no bin'
        "a product without a category"  | '${product.category.name:-}'   | [product: new Product(productCode: "AB123")]       || true         | true            | ''
        "a name that is not allowed"    | '${shipment.name:-x}'          | [product: new Product(productCode: "AB123")]       || false        | false           | null
        "an index that is not a number" | '${product.tags[x]:-}'         | [product: new Product(productCode: "AB123")]       || false        | false           | null
        // known difference: the check cannot know which values a print request provides,
        // so a template reading a value the page does not pass fails only when printed
        "no container passed"           | '${container.name:-}'          | [product: new Product(productCode: "AB123")]       || true         | false           | null
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
