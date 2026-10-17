package org.pih.warehouse.regression.platform.jobs

import org.pih.warehouse.jobs.*
import org.grails.testing.GrailsUnitTest
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventorySnapshotService
import org.pih.warehouse.product.Product
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import spock.lang.Specification
import util.LiquibaseUtil

/**
 * The job is triggered with a location and optionally a product in its data map: it refreshes the snapshots of
 * that product at that location, or of the whole location.
 */
class RefreshInventorySnapshotJobSpec extends Specification implements GrailsUnitTest {

    InventorySnapshotService inventorySnapshotService = Mock()
    RefreshInventorySnapshotJob job = new RefreshInventorySnapshotJob(inventorySnapshotService: inventorySnapshotService)

    Location location = new Location(name: 'Depot')
    Product product = new Product(name: 'Product')

    void setup() {
        GroovyMock(LiquibaseUtil, global: true)
        LiquibaseUtil.isRunningMigrations() >> false
        config.openboxes.jobs.refreshInventorySnapshotJob.enabled = true

        GroovySpy(Location, global: true)
        Location.load(_) >> { String id -> id == 'location-1' ? location : null }
        GroovySpy(Product, global: true)
        Product.load(_) >> { String id -> id == 'product-1' ? product : null }
    }

    private JobExecutionContext contextWith(Map data) {
        return Stub(JobExecutionContext) {
            getMergedJobDataMap() >> new JobDataMap(data)
        }
    }

    void "a location and a product: the snapshots of that product at that location"() {
        when:
        job.execute(contextWith(locationId: 'location-1', productId: 'product-1', user: 'user-1', date: new Date()))

        then:
        1 * inventorySnapshotService.populateInventorySnapshots(location, product)
        0 * inventorySnapshotService._
    }

    void "a location alone: the snapshots of the whole location"() {
        when:
        job.execute(contextWith(locationId: 'location-1'))

        then:
        1 * inventorySnapshotService.populateInventorySnapshots(location)
        0 * inventorySnapshotService._
    }

    void "no location: nothing is refreshed, even with a product"() {
        when:
        job.execute(contextWith(productId: 'product-1'))

        then:
        0 * inventorySnapshotService._
    }

    void "a failing refresh is logged, not thrown to the scheduler"() {
        when:
        job.execute(contextWith(locationId: 'location-1'))

        then:
        1 * inventorySnapshotService.populateInventorySnapshots(location) >> { throw new RuntimeException('database down') }
        noExceptionThrown()
    }

    void "does nothing when disabled"() {
        given:
        config.openboxes.jobs.refreshInventorySnapshotJob.enabled = false

        when:
        job.execute(contextWith(locationId: 'location-1', productId: 'product-1'))

        then:
        0 * inventorySnapshotService._
    }
}
