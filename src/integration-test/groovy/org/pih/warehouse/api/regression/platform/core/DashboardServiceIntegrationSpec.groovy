package org.pih.warehouse.api.regression.platform.core

import org.pih.warehouse.core.*
import org.pih.warehouse.api.regression.platform.common.base.ServiceIntegrationSpec
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.report.InventoryReportCommand

import java.time.LocalDate

/**
 * The expiry lists, fast movers and latest-count export of the dashboard against a depot with three lots of one
 * product: one expired, one expiring within a month and one expiring in more than a year.
 */
class DashboardServiceIntegrationSpec extends ServiceIntegrationSpec {

    DashboardService dashboardService

    Location depot
    Product product
    InventoryItem expired
    InventoryItem expiringSoon
    InventoryItem expiringLater
    // The date of the baseline counts, computed once so the fixture and the assertions agree across midnight
    Date countedOn

    void setup() {
        depot = newDepot('Dashboard depot')
        product = newProduct()
        Date today = new Date()
        countedOn = today - 1
        expired = stock(depot, product, uniqueName('EXPIRED'), today - 10, 4, null, countedOn)
        expiringSoon = stock(depot, product, uniqueName('SOON'), today + 20, 6, null, countedOn)
        expiringLater = stock(depot, product, uniqueName('LATER'), today + 400, 2, null, countedOn)
    }

    private List<String> lotIds(List<InventoryItem> items) {
        return items*.id
    }

    void 'expired and expiring stock of the depot, filtered by window and status'() {
        when:
        Map result = asAdminAt(depot) {
            Location location = Location.get(depot.id)
            [
                    expired      : lotIds(dashboardService.getExpiredStock(new InventoryReportCommand(location: location))),
                    expiredRange : lotIds(dashboardService.getExpiredStock(new InventoryReportCommand(location: location,
                            startDate: LocalDate.now().minusDays(30), endDate: LocalDate.now(), category: Product.get(product.id).category))),
                    expiring     : lotIds(dashboardService.getExpiringStock(new InventoryReportCommand(location: location))),
                    within30     : lotIds(dashboardService.getExpiringStock(new InventoryReportCommand(location: location, status: 'within30Days'))),
                    beyondYear   : lotIds(dashboardService.getExpiringStock(new InventoryReportCommand(location: location, status: 'greaterThan365Days'))),
                    windowed     : lotIds(dashboardService.getExpiringStock(new InventoryReportCommand(location: location,
                            startDate: LocalDate.now(), endDate: LocalDate.now().plusDays(100), category: Product.get(product.id).category))),
            ]
        }

        then:
        result.expired == [expired.id]
        result.expiredRange == [expired.id]
        result.expiring == [expiringSoon.id, expiringLater.id]
        result.within30 == [expiringSoon.id]
        result.beyondYear == [expiringLater.id]
        result.windowed == [expiringSoon.id]
    }

    void 'the dashboard summaries count the stock of the depot'() {
        when:
        Map result = asAdminAt(depot) {
            Location location = Location.get(depot.id)
            [
                    fastMovers : dashboardService.getFastMovers(location, new Date(), 5),
                    value      : dashboardService.getTotalStockValue(location),
                    alerts     : dashboardService.getDashboardAlerts(location),
                    expiration : dashboardService.getExpirationSummary(location),
                    csv        : dashboardService.exportLatestInventoryDate(location),
            ]
        }

        then:
        result.fastMovers.location == depot.id
        result.fastMovers.results == []
        result.value.totalCount == 1
        result.value.totalStockValue == 12
        result.alerts.totalStock == 1
        result.alerts.inStock == 1
        result.expiration.expired == 1
        result.expiration.within30Days == 1
        result.expiration.greaterThan365Days == 1
        result.csv.readLines().size() == 2
        result.csv.contains('Most Recent Stock Count')
        result.csv.contains(product.productCode)
        result.csv.readLines()[1].contains(countedOn.format('dd/MMM/yyyy'))
    }
}
