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
}
