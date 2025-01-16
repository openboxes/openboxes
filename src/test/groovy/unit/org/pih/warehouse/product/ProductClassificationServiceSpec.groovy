package unit.org.pih.warehouse.product

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductClassificationDto
import org.pih.warehouse.product.ProductClassificationService

@Unroll
class ProductClassificationServiceSpec extends Specification implements ServiceUnitTest<ProductClassificationService>, DataTest {

    void setupSpec() {
        mockDomains(Product, InventoryLevel, Inventory, Location)
    }

    void 'given a valid facility, list returns a unique list of all valid classifications'() {
        given:
        new Product(id: "1", abcClass: "A").save(validate: false)
        new Product(id: "2", abcClass: "B").save(validate: false)
        new Product(id: "3", abcClass: null).save(validate: false)  // Should be excluded
        new Product(id: "4", abcClass: "").save(validate: false)

        and:
        Location location = new Location(id: "1").save(validate: false)
        Inventory inventory = new Inventory(warehouse: location).save(validate: false)

        and:
        new InventoryLevel(id: "1", inventory: inventory, abcClass: "A").save(validate: false)
        new InventoryLevel(id: "2", inventory: inventory, abcClass: "C").save(validate: false)

        when:
        List<ProductClassificationDto> classifications = service.list("1")

        then:
        assert classifications.size() == 4
        assert asNames(classifications).containsAll(["A", "B", "C", ""])
    }

    void 'given a valid facility, list excludes classifications from inventories of other facilities'() {
        given:
        Location location = new Location(id: "1").save(validate: false)
        Inventory inventory = new Inventory(warehouse: location).save(validate: false)
        Location otherLocation = new Location(id: "2").save(validate: false)
        Inventory otherInventory = new Inventory(warehouse: otherLocation).save(validate: false)

        and:
        new InventoryLevel(id: "1", inventory: inventory, abcClass: "A").save(validate: false)
        new InventoryLevel(id: "2", inventory: otherInventory, abcClass: "B").save(validate: false)

        expect:
        asNames(service.list("1")) == ["A"]
    }

    void 'given an invalid facility, list correctly errors'() {
        when:
        service.list("1")

        then:
        thrown(IllegalArgumentException)
    }

    void 'given a valid facility with no classifications, list returns nothing'() {
        given:
        Location location = new Location(id: "1").save(validate: false)
        new Inventory(warehouse: location).save(validate: false)

        when:
        List<ProductClassificationDto> classifications = service.list("1")

        then:
        assert classifications != null
        assert classifications.size() == 0
    }

    private List<String> asNames(List<ProductClassificationDto> classifications) {
        return classifications.collect { it.name }
    }
}
