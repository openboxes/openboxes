package org.pih.warehouse.api

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
import org.pih.warehouse.inventory.AvailabilityCommand
import org.pih.warehouse.inventory.DepotAvailabilityDto
import org.pih.warehouse.inventory.LotAvailabilityDto
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.ProductLotRequest
import org.pih.warehouse.location.LocationSimpleDto
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

    void 'getAvailabilityInAllDepots should render the availability of the given product lots'() {
        given: 'the following db data'
        Product product = new Product().save(validate: false)

        and: 'the following command'
        AvailabilityCommand command = new AvailabilityCommand(productLots: [
                new ProductLotRequest(product: product, lotNumber: 'LOT-1'),
                new ProductLotRequest(product: product, lotNumber: 'LOT-2'),
        ])

        and: 'the following mocks'
        ProductAvailabilityService productAvailabilityService = Mock(ProductAvailabilityService)
        controller.productAvailabilityService = productAvailabilityService

        when:
        controller.getAvailabilityInAllDepots(command)

        then: 'all of the product lots are read in a single call'
        1 * productAvailabilityService.getAvailabilityInAllDepots(command) >> [
                new LotAvailabilityDto(
                        productId: product.id,
                        lotNumber: 'LOT-1',
                        quantityOnHand: 5,
                        depots: [new DepotAvailabilityDto(
                                depot: new LocationSimpleDto(id: 'depot-1', name: 'Belladere Depot'),
                                quantityOnHand: 5)]),
        ]

        and: 'their availability is rendered'
        JSONObject json = getJsonObjectResponse(controller.response)
        json.data.length() == 1
        json.data[0].lotNumber == 'LOT-1'
        json.data[0].quantityOnHand == 5
        json.data[0].depots[0].depot.name == 'Belladere Depot'
    }

    void 'getAvailabilityInAllDepots should render nothing when the product lots are not in inventory'() {
        given: 'the following db data'
        Product product = new Product().save(validate: false)

        and: 'the following command'
        AvailabilityCommand command = new AvailabilityCommand(
                productLots: [new ProductLotRequest(product: product, lotNumber: 'LOT-1')])

        and: 'the following mocks'
        ProductAvailabilityService productAvailabilityService = Mock(ProductAvailabilityService)
        controller.productAvailabilityService = productAvailabilityService

        when:
        controller.getAvailabilityInAllDepots(command)

        then:
        1 * productAvailabilityService.getAvailabilityInAllDepots(command) >> []

        and: 'nothing is rendered for them'
        JSONObject json = getJsonObjectResponse(controller.response)
        json.data.length() == 0
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
