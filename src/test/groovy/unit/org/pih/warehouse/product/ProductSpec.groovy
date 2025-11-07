package unit.org.pih.warehouse.product

import grails.testing.gorm.DomainUnitTest
import java.time.Instant
import org.grails.plugins.web.taglib.ApplicationTagLib
import spock.lang.Specification

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductAssociation
import org.pih.warehouse.product.ProductAssociationTypeCode

class ProductSpec extends Specification implements DomainUnitTest<Product> {

    void setupSpec() {
        mockDomains(Product, ProductAssociation, Inventory, InventoryLevel)

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

    void 'getInventoryLevel should return expected inventory level'() {
        given:
        Inventory inventory = new Inventory().save(validate:false)
        Location location = new Location(
                inventory: inventory
        ).save(validate: false)
        Product product = new Product().save(validate: false)
        InventoryLevel inventoryLevel = new InventoryLevel(
                product: product,
                inventory: inventory,
        ).save(validate: false)

        expect:
        product.getInventoryLevel(location.id) == inventoryLevel
    }

    void 'getBinLocation should return the bin location of the associated inventory level'() {
        given:
        Inventory inventory = new Inventory().save(validate:false)
        Location location = new Location(
                inventory: inventory
        ).save(validate: false)
        Product product = new Product().save(validate: false)
        InventoryLevel inventoryLevel = new InventoryLevel(
                product: product,
                inventory: inventory,
                binLocation: "TEST BIN"
        ).save(validate: false)

        expect:
        product.getBinLocation(location.id) == "TEST BIN"
    }

    void 'getBinLocation should return null when there is no matching inventory level'() {
        given:
        Inventory inventory = new Inventory().save(validate:false)
        Location location = new Location(
                inventory: inventory
        ).save(validate: false)
        Product product = new Product().save(validate: false)

        expect:
        product.getBinLocation(location.id) == null
    }

    void 'getBinLocation should return null when the bin location in the inventory level is null'() {
        given:
        Inventory inventory = new Inventory().save(validate:false)
        Location location = new Location(
                inventory: inventory
        ).save(validate: false)
        Product product = new Product().save(validate: false)
        InventoryLevel inventoryLevel = new InventoryLevel(
                product: product,
                inventory: inventory,
                binLocation: null
        ).save(validate: false)

        expect:
        product.getBinLocation(location.id) == null
    }
}
