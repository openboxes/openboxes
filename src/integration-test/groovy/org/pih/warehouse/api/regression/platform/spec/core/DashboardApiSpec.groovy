package org.pih.warehouse.api.regression.platform.spec.core

import spock.lang.PendingFeature



import org.pih.warehouse.api.regression.platform.base.EnforceSpecValidation
import org.pih.warehouse.api.regression.platform.base.RegressionApiSpec
import org.pih.warehouse.api.spec.core.*
import groovy.json.JsonOutput
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.response.Response
import org.apache.http.HttpStatus
import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

import org.pih.warehouse.api.regression.platform.client.core.DashboardApi
import org.pih.warehouse.api.spec.base.ApiSpec
import org.pih.warehouse.core.User

/**
 * The dashboard as the React dashboard page loads it: its configuration, then one call per widget, each to the
 * endpoint the widget names in the dashboard configuration, for the current location.
 */
// OpenAPI validation enforced during this spec's features
@EnforceSpecValidation
class DashboardApiSpec extends RegressionApiSpec {

    static final String MAIN_DASHBOARD = 'mainDashboard'

    /** Every indicator endpoint the shipped dashboard configuration names (openboxes.dashboardConfig.dashboardWidgets). */
    /** OBAF-only dashboard widgets. */
    static final List<String> OBAF_INDICATORS = ['openPutawayTasks', 'readyToBeStaged', 'lostAndFound',
                                                 'averageInboundSortationTime', 'inboundSortationItems']

    static final List<String> INDICATORS = [
            'inventoryByLotAndBin', 'inProgressShipments', 'inProgressPutaways', 'receivingBin', 'itemsInventoried',
            'defaultBin', 'expiredProductsInStock', 'expirationSummary', 'fillRate', 'fillRateSnapshot',
            'inventorySummary', 'sentStockMovements', 'requisitionsByYear', 'receivedStockMovements', 'outgoingStock',
            'incomingStock', 'discrepancy', 'delayedShipments', 'productWithNegativeInventory', 'lossCausedByExpiry',
            'productsInventoried', 'percentageAdHoc', 'stockOutLastMonth', 'openStockRequests',
            'requestsPendingApproval', 'inventoryValue', 'openPurchaseOrdersCount', 'backdatedOutboundShipments',
            'backdatedInboundShipments', 'itemsWithBackdatedShipments', 'fillRateDestinations',
    ]

    /** Indicators that use the session's location (or none) instead of the locationId the page sends. */
    static final List<String> SESSION_OR_NO_LOCATION = [
            'percentageAdHoc', 'stockOutLastMonth', 'openPurchaseOrdersCount', 'fillRateDestinations',
    ]

    @Autowired
    DashboardApi dashboardApi

    @Override
    void cleanupData() {
        // The configuration test saves the test user's own dashboard layout: put the default back.
        User.withNewTransaction { User.findByUsername(username)?.dashboardConfig = null }
    }

    @Unroll
    void 'the #name indicator answers for the current location'() {
        expect:
        dashboardApi.indicator(name, [locationId: facility.id], responseSpecUtil.OK_RESPONSE_SPEC)

        where:
        name << INDICATORS
    }

    @Unroll
    void 'the #name indicator answers without a locationId'() {
        expect:
        dashboardApi.indicator(name, [:], responseSpecUtil.OK_RESPONSE_SPEC)

        where:
        name << SESSION_OR_NO_LOCATION
    }

    void 'the shipped configuration names exactly the indicators this spec covers'() {
        given: 'every widget endpoint of the dashboard configuration served to the page'
        Map dashboardWidgets = dashboardApi.config(MAIN_DASHBOARD, responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath().getMap('dashboardWidgets')
        Set<String> endpoints = dashboardWidgets.values()
                .collect { Map widget -> widget.endpoint as String }
                .findAll { it?.startsWith('/api/dashboard/') }
                .collect { it.substring('/api/dashboard/'.length()).split(/[?\/]/)[0] } as Set

        expect: 'every widget names an indicator, and every one of those is covered here'
        !endpoints.isEmpty()
        // OBAF: the shipped configuration adds five OBAF-only widgets (putaway tasks, inbound sortation, lost and
        // found); this spec does not cover them (the OBAF specs do), so they are named here and left out
        (endpoints - INDICATORS - OBAF_INDICATORS).isEmpty()
    }

    void 'the configuration of a dashboard lists its sub-dashboards and widgets'() {
        when:
        Response config = dashboardApi.config(MAIN_DASHBOARD, new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody('dashboard', Matchers.notNullValue())
                .expectBody('dashboardWidgets', Matchers.notNullValue())
                .build())
        Response keys = dashboardApi.subdashboardKeys(MAIN_DASHBOARD, responseSpecUtil.OK_RESPONSE_SPEC)

        then:
        keys.jsonPath().getList('').containsAll(config.jsonPath().getMap('dashboard').keySet())
    }

    void 'saving the widget order of the personal dashboard makes it the user\'s'() {
        given: 'the personal dashboard (the one a user may rearrange) with its first widget only'
        Map dashboard = dashboardApi.config(MAIN_DASHBOARD, responseSpecUtil.OK_RESPONSE_SPEC)
                .jsonPath().getMap('dashboard')
        String key = 'personal'
        Map firstWidget = ((List<Map>) dashboard[key].widgets).first()
        dashboard[key].widgets = [[widgetId: firstWidget.widgetId, order: 1]]

        when:
        dashboardApi.updateConfig(JsonOutput.toJson(dashboard), responseSpecUtil.OK_RESPONSE_SPEC)

        then: 'the next read shows it (the save may still be committing when its response arrives)'
        new PollingConditions(timeout: 10, delay: 0.2).eventually {
            assert dashboardApi.config(MAIN_DASHBOARD, responseSpecUtil.OK_RESPONSE_SPEC)
                    .jsonPath().getList("dashboard.${key}.widgets.widgetId".toString()) == [firstWidget.widgetId]
        }
    }

    @PendingFeature(reason = 'an unknown dashboard answers 200')
    void 'an unknown dashboard has no sub-dashboards and no configuration: 404'() {
        expect:
        dashboardApi.subdashboardKeys('noSuchDashboard', responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
        dashboardApi.config('noSuchDashboard', responseSpecUtil.NOT_FOUND_RESPONSE_SPEC)
    }
}
