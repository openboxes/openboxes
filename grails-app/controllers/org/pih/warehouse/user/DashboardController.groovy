/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.user

import grails.converters.JSON
import grails.plugin.springcache.annotations.CacheFlush
import grails.plugin.springcache.annotations.Cacheable
import org.pih.warehouse.core.Comment
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.User
import org.pih.warehouse.importer.CSVUtils
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.jobs.RefreshProductAvailabilityJob
import org.pih.warehouse.order.Order
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment

import java.text.SimpleDateFormat

class DashboardController {

    def inventoryService
    def dashboardService
    def productService
    def userService
    def sessionFactory
    def grailsApplication
    def locationService
    def userAgentIdentService

    def showCacheStatistics = {
        def statistics = sessionFactory.statistics
        log.info(statistics)
        render statistics
    }


    def globalSearch = {

        def transaction = Transaction.findByTransactionNumber(params.searchTerms)
        if (transaction) {
            redirect(controller: "inventory", action: "showTransaction", id: transaction.id)
            return
        }

        def product = Product.findByProductCodeOrId(params.searchTerms, params.searchTerms)
        if (product) {
            redirect(controller: "inventoryItem", action: "showStockCard", id: product.id)
            return
        }

        def inventoryItem = InventoryItem.findByLotNumber(params.searchTerms)
        if (inventoryItem) {
            redirect(controller: "inventoryItem", action: "showStockCard", id: inventoryItem?.product?.id)
            return
        }

        def requisition = Requisition.findByRequestNumber(params.searchTerms)
        if (requisition) {
            redirect(controller: "stockMovement", action: "show", id: requisition.id)
            return
        }

        def shipment = Shipment.findByShipmentNumber(params.searchTerms)
        if (shipment) {
            redirect(controller: "stockMovement", action: "show", id: shipment.returnOrder?.id ?: shipment.id)
            return
        }

        def receipt = Receipt.findByReceiptNumber(params.searchTerms)
        if (receipt) {
            redirect(controller: "receipt", action: "show", id: receipt.id)
            return
        }
        def order = Order.findByOrderNumber(params.searchTerms)
        if (order) {
            redirect(controller: "order", action: "show", id: order.id)
            return
        }

        redirect(controller: "inventory", action: "browse", params: params)

    }

    def index = {
        if (userAgentIdentService.isMobile()) {
            redirect(controller: "mobile")
            return
        }
        if (userService.hasHighestRole(session?.user, session?.warehouse?.id, RoleType.ROLE_AUTHENTICATED)) {
            redirect(controller: "stockMovement", action: "list", params: [direction: 'INBOUND'] )
            return
        }
        render(template: "/common/react")
    }

    def supplier = {
        render(template: "/common/react")
    }

    def expirationSummary = {
        def location = Location.get(session.warehouse.id)
        def results = dashboardService.getExpirationSummary(location)

        render results as JSON
    }

    def hideTag = {
        Tag tag = Tag.get(params.id)
        tag.isActive = false
        tag.save(flush: true)
        redirect(controller: "dashboard", action: "index", params: [editTags: true])
    }

    def hideCatalog = {
        ProductCatalog productCatalog = ProductCatalog.get(params.id)
        productCatalog.isActive = false
        productCatalog.save(flush: true)
        redirect(controller: "dashboard", action: "index", params: [editCatalogs: true])
    }

    def status = {
        def admin = User.get(1)
        def comments = Comment.findAllBySenderAndRecipient(admin, admin)

        def results = comments.collect {
            if (it.dateSent > new Date()) {
                [id: it.id, comment: warehouse.message(code: it.comment, args: [format.datetime(obj: it.dateSent)]), dateSent: it.dateSent]
            }
        }
        render results as JSON
    }

    @Cacheable("megamenuCache")
    def megamenu = {
        [
                isSuperuser           : userService.isSuperuser(session?.user),
                megamenuConfig        : grailsApplication.config.openboxes.megamenu,
                quickCategories       : productService.quickCategories,
                categories            : productService.rootCategory.categories.groupBy { it.parentCategory?.id },
        ]
    }

    @CacheFlush(["dashboardCache", "megamenuCache", "inventoryBrowserCache", "fastMoversCache",
            "binLocationReportCache", "binLocationSummaryCache", "quantityOnHandCache", "selectTagCache",
            "selectTagsCache", "selectCategoryCache", "selectCatalogsCache", "forecastCache"])
    def flushCache = {
        flash.message = "Data caches have been flushed and inventory snapshot job was triggered"
        RefreshProductAvailabilityJob.triggerNow([locationId: session.warehouse.id, forceRefresh: true])
        redirect(action: "index")
    }

    def chooseLayout = {
        if (params.layout) {
            session.layout = params.layout
        }
        redirect(controller: 'dashboard', action: 'index')
    }

    def chooseLocation = {

        // If the user has selected a new location from the topnav bar, we need
        // to retrieve the location to make sure it exists
        User user = User.get(session.user.id)
        Location warehouse = params.id ? Location.get(params.id) : null

        // If a warehouse has been selected
        if (warehouse) {

            // Save the warehouse selection to the session
            session.warehouse = warehouse

            // Save the warehouse selection for "last logged into" information
            if (user) {
                //userInstance.rememberLastLocation = Boolean.valueOf(params.rememberLastLocation)
                user.lastLoginDate = new Date()
                user.save(flush: true)
                session.user = user
            }

            if (userService.hasHighestRole(session?.user, session?.warehouse?.id, RoleType.ROLE_AUTHENTICATED)) {
                redirect(controller: 'stockMovement', action: 'list' , params: [direction: 'INBOUND'])
                return
            }

            // Successfully logged in and selected a warehouse
            // Try to redirect to the previous action before session timeout
            if (session.targetUri || params.targetUri) {
                def targetUri = params.targetUri ?: session.targetUri
                if (targetUri && !targetUri.contains("chooseLocation") && !targetUri.contains("errors")) {
                    redirect(uri: targetUri)
                    return
                }
            }
            redirect(controller: 'dashboard', action: 'index')
            return
        }

        def loginLocationsMap = locationService.getLoginLocationsMap(user, warehouse)
        def savedLocations = user.warehouse && loginLocationsMap.containsValue(user.warehouse) ? [user.warehouse] : null

        if (userAgentIdentService.isMobile()) {
            render (view: "/mobile/chooseLocation",
                    model: [savedLocations: savedLocations, loginLocationsMap: loginLocationsMap])
            return
        }

        [savedLocations: savedLocations, loginLocationsMap: loginLocationsMap]
    }


    def changeLocation = {
        User user = User.get(session.user.id)
        Map loginLocationsMap = locationService.getLoginLocationsMap(user, null)
        List savedLocations = [user?.warehouse, session?.warehouse].unique() - null
        render(template: "loginLocations", model: [savedLocations: savedLocations, loginLocationsMap: loginLocationsMap])
    }

    def downloadGenericProductSummaryAsCsv = {
        def location = Location.get(session?.warehouse?.id)
        def genericProductSummary = inventoryService.getGenericProductSummary(location)

        def summaries = (params.status == "ALL") ?
            genericProductSummary.values().flatten() :
            genericProductSummary[params.status]

        // Rename columns and filter out debugging columns
        data = summaries.collect {
            [
                Status: it?.status,
                'Generic Product': it?.name,
                'Minimum Qty': it?.minQuantity,
                'Reorder Qty': it?.reorderQuantity,
                'Maximum Qty': it?.maxQuantity,
                'Available Qty': it?.currentQuantity,
            ]
        }

        response.setHeader('Content-disposition', "attachment; filename=\"GenericProductSummary-${params.status}-${location.name}-${new Date().format('yyyyMMdd-hhmm')}.csv\"")
        render(contentType: 'text/csv', text: CSVUtils.dumpMaps(data))
        return
    }

    def downloadFastMoversAsCsv = {
        def location = Location.get(params?.location?.id ?: session?.warehouse?.id)
        String contentType
        String response

        def date = new Date()
        if (params.date) {
            def dateFormat = new SimpleDateFormat("MM/dd/yyyy")
            date = dateFormat.parse(params.date)
            date.clearTime()
        }

        def data = dashboardService.getFastMovers(location, date, params.max)
        if (data?.results) {
            contentType = "text/csv"
            response = CSVUtils.dumpMaps(data.results)
        } else {
            contentType = "text/plain"
            response = warehouse.message(code: 'fastMovers.empty.message')
        }
        response.setHeader('Content-disposition', "attachment; filename=\"FastMovers-${location.name}-${new Date().format('yyyyMMdd-hhmm')}.csv\"")
        render(contentType: contentType, text: response)
        return
    }

}
