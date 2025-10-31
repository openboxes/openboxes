package unit.org.pih.warehouse.api

import grails.orm.PagedResultList
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.web.json.JSONElement
import org.grails.web.json.JSONObject
import org.hibernate.Criteria
import org.json.JSONArray
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.api.ProductApiController
import org.pih.warehouse.core.Location
import org.pih.warehouse.forecasting.ForecastingService
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductService

@Unroll
class ProductApiControllerSpec extends Specification implements DataTest, ControllerUnitTest<ProductApiController> {

    @Shared
    private ForecastingService forecastingServiceStub

    @Shared
    private ProductService productServiceStub

    void setupSpec() {
        mockDomains(Product, Location)
    }

    void setup() {
        productServiceStub = Stub(ProductService)
        forecastingServiceStub = Stub(ForecastingService)

        controller.forecastingService = forecastingServiceStub
        controller.productService = productServiceStub
    }

    void 'getDemand should return correctly'() {
        given: 'the following db data'
        Product product = new Product().save(validate: false)
        Location location = new Location().save(validate: false)

        and: 'the following params'
        String productIdToLookFor = shouldFindProduct ? product.id : '-1'
        String locationIdToLookFor = shouldFindLocation ? location.id : '-1'

        params.id = productIdToLookFor
        session.warehouse = [id: locationIdToLookFor]

        and: 'the following mocks'
        Map stubbedDemandResult = [fakeDemand : 'test']
        forecastingServiceStub.getDemand(_, _, _) >> stubbedDemandResult

        when:
        controller.demand()

        then:
        JSONObject json = getJsonObjectResponse(controller.response)
        if (shouldFindLocation) {
            assert json.data.location != null
        } else {
            assert json.data.location == null
        }

        if (shouldFindProduct) {
            assert json.data.product != null
        } else {
            assert json.data.product == null
        }

        json.data.demand == stubbedDemandResult

        where:
        shouldFindLocation | shouldFindProduct
        true | true
        true | false
        false | true
        false | false
    }

    void "when there are #numProductsInDB product(s), list should return all of them"() {
        given: 'the following params'

        and: 'the following mocks'
        List<Product> products = generateTestProducts(numProductsInDB)
        productServiceStub.getProducts(*_) >> constructPagedResultList(products)

        when:
        controller.list()

        then:
        JSONObject json = getJsonObjectResponse(controller.response)
        json.data.length() == products.size()
        json.totalCount == products.size()

        where:
        numProductsInDB << [0, 1, 3]
    }

    private List<Product> generateTestProducts(int numToGenerate) {
        List<Product> products = []
        for (int i=1; i<=numToGenerate; i++) {
            products.add(new Product())
        }
        return products
    }

    private static PagedResultList constructPagedResultList(List results) {
        PagedResultList stub = new PagedResultList(null, [list:{->results}] as Criteria)
        stub.totalCount = results.size()
        return stub
    }

    private static JSONArray getJsonArrayResponse(GrailsMockHttpServletResponse response) {
        return getJsonElementResponse(response) as JSONArray
    }

    private static JSONObject getJsonObjectResponse(GrailsMockHttpServletResponse response) {
        return getJsonElementResponse(response) as JSONObject
    }

    private static JSONElement getJsonElementResponse(GrailsMockHttpServletResponse response) {
        response.status == 200
        return response.json
    }
}
