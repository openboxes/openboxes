package org.pih.warehouse.api

import grails.converters.JSON
import grails.core.GrailsApplication
import grails.util.Holders
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import grails.gorm.transactions.Transactional
import org.pih.warehouse.core.User
import org.pih.warehouse.dashboard.GraphData
import org.pih.warehouse.dashboard.IndicatorDataService
import org.pih.warehouse.dashboard.NumberData

@Transactional
class DashboardApiController {

    def numberDataService
    IndicatorDataService indicatorDataService
    def userService
    def messageService
    GrailsApplication grailsApplication
    AuthService authService

    def config() {
        User user = User.get(session.user.id)
        def config = userService.getDashboardConfig(user, params.id)

        render(config as JSON)
    }

    def getSubdashboardKeys() {
        def mainDashboardId = grailsApplication.config.openboxes.dashboardConfig.mainDashboardId
        def config = grailsApplication.config.openboxes.dashboardConfig.dashboards[params.id ?: mainDashboardId]
        render(config.keySet() as JSON)
    }

    def updateConfig() {
        User user = User.get(session.user.id)
        def config = userService.updateDashboardConfig(user, request.JSON)

        render(config as JSON)
    }

    def breadcrumbsConfig() {
        render(grailsApplication.config.breadcrumbsConfig as JSON)
    }

    def getInventoryByLotAndBin() {
        Location location = Location.get(params.locationId)
        NumberData numberData = numberDataService.getInventoryByLotAndBin(location)
        render(numberData as JSON)
    }

    def getLostAndFoundInventoryItems() {
        Location location = Location.get(params.locationId)
        NumberData numberData = numberDataService.getLostAndFoundInventoryItems(location)
        render(numberData as JSON)
    }

    def getInProgressShipments() {
        Location location = Location.get(params.locationId)
        User user = params.userId ? User.get(params.userId) : null
        NumberData numberData = numberDataService.getInProgressShipments(user ?: session.user, location)
        render(numberData as JSON)
    }

    def getInProgressPutaways() {
        Location location = Location.get(params.locationId)
        User user = params.userId ? User.get(params.userId) : null
        NumberData numberData = numberDataService.getInProgressPutaways(user ?: session.user, location)
        render(numberData as JSON)
    }

    def getReceivingBin() {
        Location location = Location.get(params.locationId)
        NumberData numberData = numberDataService.getReceivingBin(location)
        render(numberData as JSON)
    }

    def getItemsInventoried() {
        Location location = Location.get(params.locationId)
        NumberData numberData = numberDataService.getItemsInventoried(location)
        render(numberData as JSON)
    }

    def getDefaultBin() {
        Location location = Location.get(params.locationId)
        NumberData numberData = numberDataService.getDefaultBin(location)
        render(numberData as JSON)
    }

    def getExpiredProductsInStock() {
        Location location = Location.get(params.locationId)
        NumberData numberData = numberDataService.getExpiredProductsInStock(location)
        render (numberData as JSON)
    }

    def getExpirationSummary() {
        Location location = Location.get(params.locationId)
        def expirationSummary = indicatorDataService.getExpirationSummaryData(location, params)
        render(expirationSummary as JSON)
    }

    def getFillRate() {
        Location location = Location.get(params.locationId)
        Location destination = Location.get(params.destinationLocation)
        def fillRate = indicatorDataService.getFillRate(location, destination, params)
        render(fillRate as JSON)
    }

    def getFillRateSnapshot() {
        Location location = Location.get(params.locationId)
        def fillRateSnapshot = indicatorDataService.getFillRateSnapshot(location, params)
        render(fillRateSnapshot as JSON)
    }

    def getFillRateDestinations() {
        Location location = Location.get(params.locationId ?: session.warehouse.id)
        def destinations = []
        def defaultDestination = [code : "react.dashboard.locationFilter.all.label", message : messageService.getMessage("react.dashboard.locationFilter.all")]
        destinations << [id: "", name: defaultDestination]
        destinations.addAll(indicatorDataService.getFillRateDestinations(location))
        render([data: destinations] as JSON)
    }

    def getInventorySummary() {
        Location location = Location.get(params.locationId)
        def inventorySummary = indicatorDataService.getInventorySummaryData(location)
        render(inventorySummary as JSON)
    }

    def getSentStockMovements() {
        Location location = Location.get(params.locationId)
        def sentStockMovements = indicatorDataService.getSentStockMovements(location, params)
        render(sentStockMovements as JSON)
    }

    def getRequisitionsByYear() {
        Location location = Location.get(params.locationId)
        def requisitionsByYear = indicatorDataService.getRequisitionsByYear(location, params)
        render(requisitionsByYear as JSON)
    }

    def getReceivedStockMovements() {
        Location location = Location.get(params.locationId)
        def receivedStockMovements = indicatorDataService.getReceivedStockData(location, params)
        render(receivedStockMovements as JSON)
    }

    def getOutgoingStock() {
        Location location = Location.get(params.locationId)
        def outgoingStock = indicatorDataService.getOutgoingStock(location)
        render(outgoingStock as JSON)
    }

    def getIncomingStock() {
        Location location = Location.get(params.locationId)
        def incomingStock = indicatorDataService.getIncomingStock(location)
        render(incomingStock as JSON)
    }

    def getDiscrepancy() {
        Location location = Location.get(params.locationId)
        def discrepancy = indicatorDataService.getDiscrepancy(location, params)
        render(discrepancy as JSON)
    }

    def getDelayedShipments() {
        Location location = Location.get(params.locationId)
        def delayedShipments = indicatorDataService.getDelayedShipments(location)
        render(delayedShipments as JSON)
    }

    def getProductWithNegativeInventory() {
        Location location = Location.get(params.locationId)
        def productsWithNegativeInventory = numberDataService.getProductWithNegativeInventory(location)
        render(productsWithNegativeInventory as JSON)
    }

    def getLossCausedByExpiry() {
        Location location = Location.get(params.locationId)
        def lossCausedByExpiry = indicatorDataService.getLossCausedByExpiry(location, params)
        render(lossCausedByExpiry as JSON)
    }

    def getProductsInventoried() {
        Location location = Location.get(params.locationId)
        def productsInventoried = indicatorDataService.getProductsInventoried(location)
        render(productsInventoried as JSON)
    }

    def getPercentageAdHoc() {
        Location location = Location.get(session?.warehouse?.id)
        def percentageAdHoc = indicatorDataService.getPercentageAdHoc(location)
        render(percentageAdHoc as JSON)
    }

    def getStockOutLastMonth() {
        Location location = Location.get(session?.warehouse?.id)
        def stockOutLastMonth = indicatorDataService.getStockOutLastMonth(location)
        render(stockOutLastMonth as JSON)
    }

    def getOpenStockRequests() {
        Location location = Location.get(params.locationId)
        NumberData numberData = numberDataService.getOpenStockRequests(location)
        render(numberData as JSON)
    }

    def getRequestsPendingApproval() {
        Location location = Location.get(params.locationId)
        NumberData numberData = numberDataService.getRequestsPendingApproval(location, authService.currentUser)
        render(numberData as JSON)
    }

    def getInventoryValue() {
        Location location = Location.get(params.locationId)
        NumberData numberData = numberDataService.getInventoryValue(location)
        render(numberData as JSON)
    }

    def getOpenPurchaseOrdersCount() {
        NumberData numberData = numberDataService.getOpenPurchaseOrdersCount(params)
        render(numberData as JSON)
    }

    def getBackdatedOutboundShipments() {
        Integer defaultMonthsLimit = Holders.config.openboxes.dashboard.backdatedShipments.monthsLimit
        Integer monthsLimit = params.int('querySize', defaultMonthsLimit)
        Map backdatedShipments = indicatorDataService.getBackdatedOutboundShipmentsData(
                params.locationId,
                monthsLimit > defaultMonthsLimit ? defaultMonthsLimit : monthsLimit
        )
        render(backdatedShipments as JSON)
    }

    def getBackdatedInboundShipments() {
        Integer defaultMonthsLimit = Holders.config.openboxes.dashboard.backdatedShipments.monthsLimit
        Integer monthsLimit = params.int('querySize', defaultMonthsLimit)
        Map backdatedShipments = indicatorDataService.getBackdatedInboundShipmentsData(
                params.locationId,
                monthsLimit > defaultMonthsLimit ? defaultMonthsLimit : monthsLimit
        )
        render(backdatedShipments as JSON)
    }

    def getItemsWithBackdatedShipments() {
        Location location = Location.get(params.locationId)
        GraphData graphData = indicatorDataService.getItemsWithBackdatedShipments(location)
        render(graphData as JSON)
    }

    def getOpenPutawayTasks() {
        Location location = Location.get(params.locationId)
        NumberData numberData = numberDataService.getOpenPutawayTasks(location)
        render(numberData as JSON)
    }

    def getInboundSortationItems() {
        Location location = Location.get(params.locationId)
        NumberData numberData = numberDataService.getInboundSortationItems(location)
        render(numberData as JSON)
    }

    def getAverageInboundSortationTime() {
        Location location = Location.get(params.locationId)
        NumberData numberData = numberDataService.getAverageInboundSortationTime(location)
        render(numberData as JSON)
    }
}
