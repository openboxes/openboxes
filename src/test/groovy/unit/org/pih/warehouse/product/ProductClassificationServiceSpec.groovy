package unit.org.pih.warehouse.product

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductClassificationService

@Unroll
class ProductClassificationServiceSpec extends Specification implements ServiceUnitTest<ProductClassificationService>, DataTest {

    void setupSpec() {
        mockDomains(Product, InventoryLevel)
    }

    void 'list returns all valid classifications'() {
        given:
        new Product(id: 1, abcClass: "A").save(validate: false)
        new Product(id: 2, abcClass: "B").save(validate: false)
        new Product(id: 3, abcClass: null).save(validate: false)
        new Product(id: 4, abcClass: "").save(validate: false)

        and:
        new InventoryLevel(id: 1, abcClass: "A").save(validate: false)
        new InventoryLevel(id: 2, abcClass: "C").save(validate: false)

        when:
        List<String> classifications = service.list()

        then:
        assert classifications.size() == 4
        assert classifications.containsAll(["A", "B", "C", ""])
    }

    void 'list can gracefully handle when we have no classifications configured'() {
        when:
        List<String> classifications = service.list()

        then:
        assert classifications != null
        assert classifications.size() == 0
    }
}
