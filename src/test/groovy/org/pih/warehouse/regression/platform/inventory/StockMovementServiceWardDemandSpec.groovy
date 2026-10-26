package org.pih.warehouse.regression.platform.inventory

import org.pih.warehouse.inventory.*
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationType
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.forecasting.ForecastingService
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.ReplenishmentTypeCode
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionSourceType
import org.pih.warehouse.requisition.RequisitionType

/**
 * Pins the demand figures on the add-items page (StockMovementService.getAddPageItem): a stock list request
 * to a Ward location (clinic pack) uses demand from origin to that ward; other pull requests use the
 * downstream-consumer or destination demand. The Ward branch is dormant where no Ward location type exists.
 */
class StockMovementServiceWardDemandSpec extends Specification implements ServiceUnitTest<StockMovementService>, DataTest {

    Product product = new Product(name: 'Paracetamol')
    Location origin = new Location(name: 'Depot')
    ForecastingService forecastingService = Mock(ForecastingService)

    void setup() {
        service.forecastingService = forecastingService
        service.productAvailabilityService = Stub(ProductAvailabilityService) { getQuantityOnHand(*_) >> 40 }
        service.inventoryService = Stub(InventoryService) { getQuantityAvailableToPromise(*_) >> 30 }
    }

    private Requisition stockListRequest(LocationTypeCode destinationType, ReplenishmentTypeCode replenishment) {
        Location destination = new Location(name: 'Destination', locationType: new LocationType(name: 'type', locationTypeCode: destinationType))
        // Groovy truth of a GroovyStub is false unless stubbed
        Requisition template = GroovyStub(Requisition) {
            asBoolean() >> true
            getReplenishmentPeriod() >> 14
            getReplenishmentTypeCode() >> replenishment
        }
        return GroovyStub(Requisition) {
            asBoolean() >> true
            getSourceType() >> RequisitionSourceType.ELECTRONIC
            getRequisitionTemplateId() >> 'template'
            getRequisitionTemplate() >> template
            getType() >> RequisitionType.STOCK
            getOrigin() >> origin
            getDestination() >> destination
        }
    }

    private Map item() {
        return [id: 'item', product: product, productCode: 'P1', quantityRequested: 5, quantityAllowed: 10]
    }

    void 'a stock list request to a ward uses demand from origin to the ward, per replenishment period'() {
        given:
        Requisition request = stockListRequest(LocationTypeCode.WARD, ReplenishmentTypeCode.PUSH)

        when:
        Map line = service.getAddPageItem(request, item())

        then:
        1 * forecastingService.getDemand(origin, request.destination, product) >> [monthlyDemand: 90, dailyDemand: 2.5]
        line.monthlyDemand == 90
        line.demandPerReplenishmentPeriod == Math.ceil(2.5 * 14)
        line.quantityOnHand == 40
        !line.containsKey('quantityAvailable')
    }

    void 'the same request to a depot uses the non-ward path: push stock list, no demand'() {
        given:
        Requisition request = stockListRequest(LocationTypeCode.DEPOT, ReplenishmentTypeCode.PUSH)

        when:
        Map line = service.getAddPageItem(request, item())

        then:
        0 * forecastingService.getDemand(*_)
        line.quantityAvailable == 30
        !line.containsKey('monthlyDemand')
    }

    void 'a pull stock list to a depot uses demand going out of the destination'() {
        given:
        Requisition request = stockListRequest(LocationTypeCode.DEPOT, ReplenishmentTypeCode.PULL)

        when:
        Map line = service.getAddPageItem(request, item())

        then:
        1 * forecastingService.getDemand(request.destination, null, product) >> [monthlyDemand: 12, dailyDemand: 0.4]
        line.monthlyDemand == 12
        line.demandPerReplenishmentPeriod == Math.ceil(0.4 * 14)
    }
}
