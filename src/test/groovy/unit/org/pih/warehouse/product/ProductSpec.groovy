package unit.org.pih.warehouse.product

import grails.testing.gorm.DomainUnitTest
import java.time.Instant
import org.grails.plugins.web.taglib.ApplicationTagLib
import spock.lang.Specification

import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAssociation
import org.pih.warehouse.product.ProductAssociationTypeCode
import org.pih.warehouse.product.ProductGroup

class ProductSpec extends Specification implements DomainUnitTest<Product> {

    void setupSpec() {
        mockDomains(Product, ProductAssociation, ProductGroup)

        ApplicationTagLib gStub = Stub(ApplicationTagLib)
        Product.metaClass.getApplicationTagLib = { -> gStub }
    }

    void toJson() {
        given:
        Category category = new Category(name: "categoryName")
        Instant now = Instant.now()

        domain.id = "1"
        domain.productCode = "productCode"
        domain.name = "productName"
        domain.description = "description"
        domain.category = category
        domain.unitOfMeasure = "each"
        domain.pricePerUnit = 1.00
        domain.dateCreated = now
        domain.lastUpdated = now
        domain.color = "red"
        domain.lotAndExpiryControl = false

        when:
        Map map = domain.toJson()

        then:
        assert map.id == "1"
        assert map.productCode == "productCode"
        assert map.name == "productName"
        assert map.description == "description"
        assert map.category == domain.category
        assert map.unitOfMeasure == "each"
        assert map.pricePerUnit == 1.00
        assert map.dateCreated == now
        assert map.lastUpdated == now
        assert map.color == "red"
        assert map.handlingIcons != null
        assert map.lotAndExpiryControl == false
    }

    void alternativeProducts_shouldReturnAlternativeProducts() {
        given:
        ProductAssociationTypeCode substituteCode = ProductAssociationTypeCode.SUBSTITUTE
        Product p1 = new Product(name: "p1").save(validate: false)
        Product p2 = new Product(name: "p2").save(validate: false)
        Product p3 = new Product(name: "p3").save(validate: false)

        new ProductAssociation(code: substituteCode, product: domain, associatedProduct: p1).save(validate: false)
        new ProductAssociation(code: substituteCode, product: domain, associatedProduct: p2).save(validate: false)

        when:
        Set<Product> alternativeProducts = domain.alternativeProducts()

        then:
        assert alternativeProducts.size() == 2
        assert alternativeProducts.contains(p1)
        assert alternativeProducts.contains(p2)
    }
}
