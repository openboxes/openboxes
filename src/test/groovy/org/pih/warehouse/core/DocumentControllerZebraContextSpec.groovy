package org.pih.warehouse.core

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product
import spock.lang.Specification
import spock.lang.Unroll

/**
 * The Zebra template actions render with the product, facility, bin location
 * and inventory item of the request.
 */
class DocumentControllerZebraContextSpec extends Specification implements ControllerUnitTest<DocumentController>, DataTest {

    void setupSpec() {
        mockDomains(Document, DocumentType, Location, InventoryItem, Product)
    }

    void setup() {
        controller.beanPropertyTemplateService = new BeanPropertyTemplateService()
    }

    private static Document saveTemplate(String contents) {
        DocumentType zebraType = new DocumentType(name: "Zebra", documentCode: DocumentCode.ZEBRA_TEMPLATE).save(validate: false)
        return new Document(name: "label", filename: "label.zpl", fileContents: contents.bytes, documentType: zebraType).save(validate: false)
    }

    void "buildZebraTemplate should render the product, facility and bin location of the request"() {
        given:
        Location facility = new Location(name: "Main Warehouse").save(validate: false)
        Location bin = new Location(name: "A-01-02", locationNumber: "A-01-02").save(validate: false)
        Product product = new Product(productCode: "AB123", name: "Gauze").save(validate: false)
        Document template = saveTemplate('^XA^FD${product.productCode}|${facility.name}|${location.name}^FS^XZ')
        session.warehouse = [id: facility.id]
        params.id = template.id
        params.product = [id: product.id]
        params.location = [id: bin.id]

        when:
        controller.buildZebraTemplate()

        then:
        response.text == '^XA^FDAB123|Main Warehouse|A-01-02^FS^XZ'
        response.getHeader("Content-disposition").contains("AB123.zpl")
    }

    void "buildZebraTemplate should take the product from the inventory item"() {
        given:
        Location facility = new Location(name: "Main Warehouse").save(validate: false)
        Product product = new Product(productCode: "AB123", name: "Gauze").save(validate: false)
        InventoryItem inventoryItem = new InventoryItem(product: product, lotNumber: "LOT-1").save(validate: false)
        Document template = saveTemplate('^XA^FD${product.productCode}|${inventoryItem.lotNumber}^FS^XZ')
        session.warehouse = [id: facility.id]
        params.id = template.id
        params.inventoryItem = [id: inventoryItem.id]

        when:
        controller.buildZebraTemplate()

        then:
        response.text == '^XA^FDAB123|LOT-1^FS^XZ'
    }

    @Unroll
    void "#action should answer 400 naming a template it cannot render"() {
        given:
        Location facility = new Location(name: "Main Warehouse").save(validate: false)
        Document template = saveTemplate('^XA^FD${product.productCode}^FS^XZ')
        session.warehouse = [id: facility.id]
        params.id = template.id

        when: "the request has no product"
        controller."${action}"()

        then:
        response.status == 400
        response.text.contains("Label template 'label' can't be rendered")
        response.text.contains("upload it again")

        where:
        action << ["buildZebraTemplate", "renderZebraTemplate", "exportZebraTemplate"]
    }

    void "printZebraTemplate should show which template cannot be rendered and return to the page"() {
        given:
        Location facility = new Location(name: "Main Warehouse").save(validate: false)
        Document template = saveTemplate('^XA^FD${product?.productCode}^FS^XZ')
        session.warehouse = [id: facility.id]
        params.id = template.id
        params.protocol = "raw"

        when:
        controller.printZebraTemplate()

        then:
        flash.message.contains("Label template 'label' can't be rendered")
        response.status == 302
        response.redirectedUrl == "/"
    }

}
