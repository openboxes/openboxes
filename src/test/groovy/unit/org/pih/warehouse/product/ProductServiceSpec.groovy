package unit.org.pih.warehouse.product

import grails.testing.gorm.DataTest
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductIdentifierService
import org.pih.warehouse.product.ProductService
import org.pih.warehouse.product.ProductType

@Unroll
class ProductServiceSpec extends Specification implements DataTest {

    @Shared
    ProductService service

    void setupSpec() {
        mockDomain Product
    }

    void setup() {
        service = new ProductService()
    }

    void 'getProducts returns the requested products'() {
        given:
        new Product(id: 1).save(validate: false)
        new Product(id: 2).save(validate: false)
        new Product(id: 3, active: false).save(validate: false)

        when:
        List<Product> products = service.getProducts(productIds as String[])

        then:
        products.size() == expectedNumProducts

        where:
        productIds      || expectedNumProducts
        null            || 0
        []              || 0
        ['2']           || 1
        ['3']           || 0
        ['1', '2', '3'] || 2
    }

    @Ignore('The executeQuery in ProductService.validateProductIdentifier cannot be stubbed easily. It should be moved to a static method in the Domain class.')
    void 'saveProduct can create a product'() {
        given:
        ProductType productType = new ProductType()
        String productCode = 'testcode'

        and: 'the following mocks'
        // Product.metaClass.static.executeQuery = {String query, List params -> return [0]}
        service.productIdentifierService = Stub(ProductIdentifierService) {
            generate(_ as Product) >> productCode
        }

        when:
        def returnedProduct = service.saveProduct(new Product(id: 1, productType: productType))

        then:
        returnedProduct != null
        // Verify other fields such as productCode
    }
}
