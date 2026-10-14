package org.pih.warehouse.regression.platform.jobs

import org.pih.warehouse.jobs.*
import org.grails.testing.GrailsUnitTest
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.report.NotificationService
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import spock.lang.Specification
import util.LiquibaseUtil

/**
 * Every active depot that has notifications enabled gets the expiry alert and the four stock alerts; the other
 * depots get none. The alerts are sent from a real GPars pool, so the service calls happen on worker threads.
 */
class SendStockAlertsJobSpec extends Specification implements GrailsUnitTest {

    LocationService locationService = Mock()
    NotificationService notificationService = Mock()
    SendStockAlertsJob job = new SendStockAlertsJob(
            gparsService: new GparsService(), locationService: locationService, notificationService: notificationService)

    JobExecutionContext context = Stub() {
        getMergedJobDataMap() >> new JobDataMap([:])
    }

    Location notifying = new Location(name: 'Notifying depot', active: true,
            supportedActivities: [ActivityCode.ENABLE_NOTIFICATIONS.id] as Set)

    void setup() {
        GroovyMock(LiquibaseUtil, global: true)
        LiquibaseUtil.isRunningMigrations() >> false
        config.openboxes.jobs.sendStockAlertsJob.enabled = true
        config.openboxes.jobs.sendStockAlertsJob.skipOnEmpty = true
        config.openboxes.jobs.sendStockAlertsJob.daysUntilExpiry = 30
    }

    void "an active depot with notifications enabled gets the expiry alert and every stock alert"() {
        when:
        job.execute(context)

        then:
        1 * locationService.getDepots() >> [notifying]
        1 * notificationService.sendExpiryAlerts(notifying, 30,
                [RoleType.ROLE_ITEM_ALL_NOTIFICATION, RoleType.ROLE_ITEM_EXPIRY_NOTIFICATION], true)
        1 * notificationService.sendStockAlerts(notifying, 'out_of_stock',
                [RoleType.ROLE_ITEM_ALL_NOTIFICATION, RoleType.ROLE_ITEM_OUT_OF_STOCK_NOTIFICATION], true)
        1 * notificationService.sendStockAlerts(notifying, 'low_stock',
                [RoleType.ROLE_ITEM_ALL_NOTIFICATION, RoleType.ROLE_ITEM_LOW_STOCK_NOTIFICATION], true)
        1 * notificationService.sendStockAlerts(notifying, 'reorder_stock',
                [RoleType.ROLE_ITEM_ALL_NOTIFICATION, RoleType.ROLE_ITEM_REORDER_NOTIFICATION], true)
        1 * notificationService.sendStockAlerts(notifying, 'over_stock',
                [RoleType.ROLE_ITEM_ALL_NOTIFICATION, RoleType.ROLE_ITEM_OVERSTOCK_NOTIFICATION], true)
        0 * notificationService._
    }

    void "inactive depots and depots without notifications get no alerts"() {
        given:
        Location inactive = new Location(name: 'Inactive depot', active: false,
                supportedActivities: [ActivityCode.ENABLE_NOTIFICATIONS.id] as Set)
        Location silent = new Location(name: 'Silent depot', active: true,
                supportedActivities: [ActivityCode.MANAGE_INVENTORY.id] as Set)

        when:
        job.execute(context)

        then:
        1 * locationService.getDepots() >> [inactive, silent, notifying]
        1 * notificationService.sendExpiryAlerts(notifying, *_)
        4 * notificationService.sendStockAlerts(notifying, *_)
        0 * notificationService._
    }

    void "the settings are passed on: skipOnEmpty, and 60 days until expiry when it is unset or 0"() {
        given:
        config.openboxes.jobs.sendStockAlertsJob.skipOnEmpty = false
        config.openboxes.jobs.sendStockAlertsJob.daysUntilExpiry = 0

        when:
        job.execute(context)

        then:
        1 * locationService.getDepots() >> [notifying]
        1 * notificationService.sendExpiryAlerts(notifying, 60, _, false)
        4 * notificationService.sendStockAlerts(notifying, _, _, false)
    }

    void "does nothing when disabled"() {
        given:
        config.openboxes.jobs.sendStockAlertsJob.enabled = false

        when:
        job.execute(context)

        then:
        0 * locationService._
        0 * notificationService._
    }
}
