package org.pih.warehouse.regression.platform.jobs

import org.pih.warehouse.jobs.*
import org.grails.testing.GrailsUnitTest
import spock.lang.Specification
import util.LiquibaseUtil

/**
 * A job runs only when its configuration enables it and no database migration is under way. The migration check
 * needs a database, so it is replaced here by a global mock of LiquibaseUtil.
 */
class JobUtilsSpec extends Specification implements GrailsUnitTest {

    /** A job that application.yml knows nothing about. */
    static class UnconfiguredJob {
    }

    void setup() {
        GroovyMock(LiquibaseUtil, global: true)
    }

    void "a disabled job does not run, and the database is not asked about migrations"() {
        given:
        config.openboxes.jobs.dataMigrationJob.enabled = false

        when:
        boolean result = JobUtils.shouldExecute(DataMigrationJob)

        then:
        !result
        0 * LiquibaseUtil.isRunningMigrations()
    }

    void "a job without an enabled setting does not run"() {
        expect:
        !JobUtils.shouldExecute(UnconfiguredJob)
    }

    void "an enabled job runs when no migration is under way"() {
        given:
        config.openboxes.jobs.dataMigrationJob.enabled = true

        when:
        boolean result = JobUtils.shouldExecute(DataMigrationJob)

        then:
        1 * LiquibaseUtil.isRunningMigrations() >> false
        result
    }

    void "an enabled job waits while the migrations run"() {
        given:
        config.openboxes.jobs.dataMigrationJob.enabled = true

        when:
        boolean result = JobUtils.shouldExecute(DataMigrationJob)

        then:
        1 * LiquibaseUtil.isRunningMigrations() >> true
        !result
    }

    void "the setting is looked up under the job's name with a lower-case first letter"() {
        given:
        config.openboxes.jobs.sendStockAlertsJob.enabled = true
        config.openboxes.jobs.SendStockAlertsJob.enabled = false
        LiquibaseUtil.isRunningMigrations() >> false

        expect:
        JobUtils.shouldExecute(SendStockAlertsJob)
    }

    void "the cron trigger is named after the job"() {
        expect:
        JobUtils.getCronName(RefreshTransactionFactJob) == 'refreshTransactionFactJobCronTrigger'
        JobUtils.getCronName(SendStockAlertsJob) == 'sendStockAlertsJobCronTrigger'
    }

    void "the cron expression comes from the job's configuration"() {
        given:
        config.openboxes.jobs.updateExchangeRatesJob.cronExpression = '0 0 3 * * ?'

        expect:
        JobUtils.getCronExpression(UpdateExchangeRatesJob) == '0 0 3 * * ?'
        JobUtils.getCronExpression(UnconfiguredJob) == null
    }
}
