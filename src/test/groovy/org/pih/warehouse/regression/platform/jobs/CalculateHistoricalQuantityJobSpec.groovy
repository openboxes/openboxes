package org.pih.warehouse.regression.platform.jobs

import spock.lang.PendingFeature

import spock.lang.Ignore
import org.pih.warehouse.jobs.*
import org.grails.testing.GrailsUnitTest
import org.pih.warehouse.inventory.InventorySnapshotService
import spock.lang.Specification
import util.LiquibaseUtil

/**
 * The job keeps its queue of dates in one node's memory, so it must not run on a clustered job store, even when
 * an operator's configuration enables it (which outranks the cluster profile).
 */
class CalculateHistoricalQuantityJobSpec extends Specification implements GrailsUnitTest {

    InventorySnapshotService inventorySnapshotService = Mock()
    CalculateHistoricalQuantityJob job = new CalculateHistoricalQuantityJob(inventorySnapshotService: inventorySnapshotService)

    void setup() {
        GroovyMock(LiquibaseUtil, global: true)
        LiquibaseUtil.isRunningMigrations() >> false
        CalculateHistoricalQuantityJob.dates = []
        config.openboxes.jobs.calculateHistoricalQuantityJob.enabled = true
        config.openboxes.jobs.calculateHistoricalQuantityJob.daysToProcess = '30'
        config.quartz.jobStore.isClustered = false
    }

    void cleanup() {
        CalculateHistoricalQuantityJob.dates = []
    }

    void "does nothing when disabled"() {
        given:
        config.openboxes.jobs.calculateHistoricalQuantityJob.enabled = false

        when:
        job.execute(null)

        then:
        0 * inventorySnapshotService._
    }

    void "the first run queues the transaction dates within the window and processes the oldest"() {
        given:
        Date now = new Date()
        Date tooOld = now - 100
        Date older = now - 20
        Date newer = now - 5

        when:
        job.execute(null)

        then:
        1 * inventorySnapshotService.getTransactionDates() >> [tooOld, older, newer]
        1 * inventorySnapshotService.populateInventorySnapshots(older, Boolean.FALSE)
        0 * inventorySnapshotService._
        CalculateHistoricalQuantityJob.dates == [newer]
    }

    @PendingFeature(reason = 'OBAF pops the empty date queue (NoSuchElementException) when no date falls in the window')
    void "a window without transaction dates processes nothing and does not fail"() {
        when:
        job.execute(null)

        then:
        1 * inventorySnapshotService.getTransactionDates() >> [new Date() - 100]
        0 * inventorySnapshotService._
        noExceptionThrown()
        CalculateHistoricalQuantityJob.dates == []
    }

    void "later runs work through the queue without reading the transaction dates again"() {
        given:
        Date first = new Date() - 3
        Date second = new Date() - 2
        CalculateHistoricalQuantityJob.dates = [second, first]

        when:
        job.execute(null)

        then:
        1 * inventorySnapshotService.populateInventorySnapshots(first, Boolean.FALSE)
        0 * inventorySnapshotService._
        CalculateHistoricalQuantityJob.dates == [second]

        when:
        job.execute(null)

        then:
        1 * inventorySnapshotService.populateInventorySnapshots(second, Boolean.FALSE)
        0 * inventorySnapshotService._
        CalculateHistoricalQuantityJob.dates == []
    }

    void "with an empty daysToProcess setting only dates from now on are queued"() {
        given:
        config.openboxes.jobs.calculateHistoricalQuantityJob.daysToProcess = ''
        Date future = new Date() + 1

        when:
        job.execute(null)

        then:
        1 * inventorySnapshotService.getTransactionDates() >> [new Date() - 1, future]
        1 * inventorySnapshotService.populateInventorySnapshots(future, Boolean.FALSE)
        CalculateHistoricalQuantityJob.dates == []
    }

    void "a numeric daysToProcess setting, as application.yml has it, is accepted"() {
        given:
        config.openboxes.jobs.calculateHistoricalQuantityJob.daysToProcess = 540
        Date recent = new Date() - 10

        when:
        job.execute(null)

        then:
        1 * inventorySnapshotService.getTransactionDates() >> [recent]
        1 * inventorySnapshotService.populateInventorySnapshots(recent, Boolean.FALSE)
    }
}
