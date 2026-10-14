package org.pih.warehouse.api.regression.platform.forecasting

import spock.lang.PendingFeature


import org.pih.warehouse.forecasting.*
import grails.core.GrailsApplication
import groovy.sql.Sql
import groovy.time.TimeCategory

import org.pih.warehouse.api.regression.platform.common.base.ServiceIntegrationSpec
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Tag
import org.pih.warehouse.product.Product

import javax.sql.DataSource

/**
 * Demand comes from the product_demand_details table (a materialized copy of the product_demand view), so the
 * spec writes demand rows into it directly: two issued requests from a depot to a ward, 100 and 40 days ago.
 */
class ForecastingServiceIntegrationSpec extends ServiceIntegrationSpec {

    ForecastingService forecastingService
    DataSource dataSource
    GrailsApplication grailsApplication

    Location depot
    Location ward
    Product product
    String requestPrefix
    Boolean forecastingEnabled

    void setup() {
        forecastingEnabled = grailsApplication.config.getProperty('openboxes.forecasting.enabled', Boolean)
        depot = newDepot('Demand depot')
        ward = newDepot('Demand ward')
        product = newProduct()
        requestPrefix = uniqueName('REQ')
        demandRow(1, 100, 30, null)
        demandRow(2, 40, 60, 'STOCKOUT')
    }

    void cleanup() {
        grailsApplication.config.openboxes.forecasting.enabled = forecastingEnabled
        new Sql(dataSource).execute('DELETE FROM product_demand_details WHERE request_number LIKE ?', ["${requestPrefix}%".toString()])
    }

    // Noon, age days before the database's own CURRENT_DATE. The forecasting views count days with the database's
    // date (datediff(CURRENT_DATE, ...)), so a date taken from the JVM's local calendar is a day off whenever the two
    // calendars disagree (e.g. 20:00-24:00 EDT, when it is already tomorrow in UTC).
    private static final String noonDaysAgo = "TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL ? DAY), '12:00:00')"

    private void demandRow(int index, int age, int quantity, String reasonCode) {
        new Sql(dataSource).execute("""
            INSERT INTO product_demand_details (request_id, request_status, request_number, date_created, date_requested,
                date_issued, origin_id, origin_name, destination_id, destination_name, request_item_id, product_id,
                product_code, product_name, quantity_requested, quantity_canceled, quantity_approved, quantity_modified,
                quantity_picked, quantity_demand, reason_code, reason_code_classification)
            VALUES (?, 'ISSUED', ?, ${noonDaysAgo}, ${noonDaysAgo}, ${noonDaysAgo}, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, 0, ?, ?, ?, ?)
            """.toString(), [
                "${requestPrefix}-${index}".toString(), "${requestPrefix}-${index}".toString(), age, age, age,
                depot.id, depot.name, ward.id, ward.name, "${requestPrefix}-item-${index}".toString(), product.id,
                product.productCode, product.name, quantity, quantity, quantity, quantity, reasonCode, reasonCode
        ])
    }

    void 'demand to a destination over the demand period, per day and per month'() {
        when:
        Map demand = asAdminAt(depot) { forecastingService.getDemand(depot, ward, product) }

        then:
        demand.totalDemand == 90
        demand.totalDays == 365
        Math.abs((demand.dailyDemand as BigDecimal) - 90 / 365) < 0.0001
        demand.monthlyDemand == 8
        demand.quantityOnHand == 0
        demand.onHandMonths == 0
    }

    void 'no demand where nothing was issued, and none at all when forecasting is disabled'() {
        when:
        Map none = asAdminAt(ward) { forecastingService.getDemand(ward, depot, product) }

        then:
        none.totalDemand == 0
        none.monthlyDemand == 0

        when:
        grailsApplication.config.openboxes.forecasting.enabled = false
        Map disabled = asAdminAt(depot) { forecastingService.getDemand(depot, ward, product) }
        List details = asAdminAt(depot) { forecastingService.getDemandDetails(depot, product) }
        List summary = asAdminAt(depot) { forecastingService.getDemandSummary(depot, product) }

        then:
        disabled == [:]
        details == []
        summary == []
    }

    void 'demand details list the issued request lines and their destinations'() {
        when:
        List details = asAdminAt(depot) { forecastingService.getDemandDetails(depot, product) }
        List destinations = asAdminAt(depot) {
            forecastingService.getAvailableDestinationsForDemandDetails(depot, product, new Date() - 365, new Date())
        }

        then:
        details*.request_number as Set == ["${requestPrefix}-1", "${requestPrefix}-2"]*.toString() as Set
        details*.quantity_demand*.intValue() as Set == [30, 60] as Set
        details.every { it.product_name.startsWith(product.name) && it.destination_name == ward.name }
        destinations*.id == [ward.id]
        destinations*.name == [ward.name]
    }

    void 'the demand summary has a row per month, the monthly average and the total'() {
        when:
        List rows = asAdminAt(depot) { forecastingService.getDemandSummary(depot, product) }

        then:
        rows.size() >= 4
        rows[-1].year == 'Total Demand'
        rows[-1].quantityDemand == '90'
        rows[-2].year == 'Average Monthly'
        rows[0].quantityDemand == '30'
        rows[0].monthName
        rows.findAll { it.dateKey }*.quantityDemand.collect { it.replace(',', '') as Integer }.sum() == 90
    }

    @PendingFeature(reason = 'the request detail report filtered by product and tags returns nothing')
    void 'the request detail report filters by destination, product, reason, category and tag'() {
        given:
        Tag tag = committed {
            Product saved = Product.get(product.id)
            Tag created = new Tag(tag: uniqueName('tag')).save(failOnError: true, flush: true)
            saved.addToTags(created)
            saved.save(failOnError: true, flush: true)
            created
        }
        Map params = [startDate: new Date() - 365, endDate: new Date(), originId: depot.id, destinationId: ward.id,
                      productId: product.id, category: product.category.id, includeCategoryChildren: true]

        when:
        List all = asAdminAt(depot) { forecastingService.getRequestDetailReport(params) }
        List stockouts = asAdminAt(depot) { forecastingService.getRequestDetailReport(params + [reasonCode: 'STOCKOUT']) }
        List tagged = asAdminAt(depot) { forecastingService.getRequestDetailReport(params.findAll { it.key != 'productId' } + [tags: [tag.id]]) }
        List otherTag = asAdminAt(depot) { forecastingService.getRequestDetailReport(params.findAll { it.key != 'productId' } + [tags: ['no-such-tag']]) }
        List productAndTag = asAdminAt(depot) { forecastingService.getRequestDetailReport(params + [tags: [tag.id]]) }

        then:
        all.size() == 2
        all.every { it.productCode == product.productCode && it.origin == depot.name && it.destination == ward.name }
        all*.quantityDemand*.intValue() as Set == [30, 60] as Set
        stockouts.size() == 1
        stockouts[0].reasonCodeClassification == 'STOCKOUT'
        stockouts[0].quantityIssued == 60
        stockouts[0].productName.startsWith(product.name)
        tagged.size() == 2
        otherTag.empty
        productAndTag.size() == 2
    }

    void 'daily demand averages the demand of each product over the given days'() {
        when:
        List rows = asAdminAt(depot) { forecastingService.getDailyDemand(depot, 365) }

        then:
        Map row = rows.find { it.product_id == product.id }
        row.location_id == depot.id
        Math.abs((row.average_daily_demand as BigDecimal) - (90 / 365)) < 0.001
    }

    void 'stock expiring before it can be used up by the average demand is projected to expire'() {
        given:
        Date expiry = new Date() + 10
        expiry.clearTime()
        stock(depot, product, uniqueName('LOT'), expiry, 20)

        when:
        List expiring = asAdminAt(depot) { forecastingService.getProductExpirySummary(depot, 30) }
        List withDemand = asAdminAt(depot) { forecastingService.getProductExpiryAndAverageDailyDemandSummary(depot, 30) }
        List projected = asAdminAt(depot) { forecastingService.getProductExpiryProjectedSummary(depot, 30) }
        List forProduct = asAdminAt(depot) { forecastingService.getProductExpiry(depot, 30, product.id) }
        List notYet = asAdminAt(depot) { forecastingService.getProductExpiry(depot, 5, product.id) }

        then:
        expiring.find { it.product_id == product.id }.quantity_on_hand == 20
        Map summary = withDemand.find { it.product_id == product.id }
        summary.quantity_on_hand == 20
        BigDecimal averageDailyDemand = summary.average_daily_demand as BigDecimal
        Math.abs(averageDailyDemand - 0.9) < 0.001
        int usableDays = use(TimeCategory) { (summary.expiration_date - new Date()).days }
        Math.abs(projected.find { it.productId == product.id }.quantityExpired - (20 - usableDays * averageDailyDemand)) < 0.01
        forProduct.size() == 1
        forProduct[0].quantity_on_hand == 20
        notYet.empty
    }
}
