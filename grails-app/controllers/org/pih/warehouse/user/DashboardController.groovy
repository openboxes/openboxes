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
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.plugin.cache.CacheEvict
import org.apache.commons.lang.StringEscapeUtils
import org.grails.gsp.GroovyPagesTemplateEngine
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Comment
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.RoleType
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.jobs.RefreshOrderSummaryJob
import org.pih.warehouse.jobs.RefreshProductAvailabilityJob
import org.pih.warehouse.order.Order
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductCatalog
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment
import org.springframework.boot.info.GitProperties

import java.text.SimpleDateFormat

class DashboardController {

    def inventoryService
    def dashboardService
    def userService
    def sessionFactory
    def locationService
    GrailsApplication grailsApplication
    GitProperties gitProperties
    def userAgentIdentService
    def megamenuService
    GroovyPagesTemplateEngine groovyPagesTemplateEngine

    def showCacheStatistics() {
        def statistics = sessionFactory.statistics
        log.info("${statistics}")
        render statistics
    }


    def globalSearch() {

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

        List<Requisition> requisitions = Requisition.findAllByRequestNumber(params.searchTerms, [sort: 'dateCreated', order: 'desc'])
        if (requisitions) {
            Requisition requisition = requisitions.first()
            redirect(controller: "stockMovement", action: "show", id: requisition.id)
            return
        }

        List<Shipment> shipments = Shipment.findAllByShipmentNumber(params.searchTerms, [sort: 'dateCreated', order: 'desc'])
        if (shipments) {
            Shipment shipment = shipments.first()
            redirect(controller: "stockMovement", action: "show", id: shipment.returnOrder?.id ?: shipment.id)
            return
        }

        def receipt = Receipt.findByReceiptNumber(params.searchTerms)
        if (receipt) {
            redirect(controller: "receipt", action: "show", id: receipt.id)
            return
        }
        List<Order> orders = Order.findAllByOrderNumber(params.searchTerms, [sort: 'dateCreated', order: 'desc'])
        if (orders) {
            Order order = orders.first()
            if (order.isReturnOrder) {
                redirect(controller: "stockMovement", action: "show", id: order.id)
                return
            }
            redirect(controller: "order", action: "show", id: order.id)
            return
        }

        redirect(controller: "inventory", action: "browse", params: params)

    }

    def index() {
        if (userAgentIdentService.isMobile()) {
            redirect(controller: "mobile")
            return
        }
        if (userService.hasHighestRole(session?.user, session?.warehouse?.id, RoleType.ROLE_AUTHENTICATED)) {
            redirect(controller: "stockMovement", action: "list", params: [direction: 'INBOUND'] )
            return
        }
        render(view: "/common/react")
    }

    def supplier() {
        render(view: "/common/react")
    }

    def expirationSummary() {
        def location = Location.get(session.warehouse.id)
        def results = dashboardService.getExpirationSummary(location)

        render results as JSON
    }

    def hideTag() {
        Tag tag = Tag.get(params.id)
        tag.isActive = false
        tag.save(flush: true)
        redirect(controller: "dashboard", action: "index", params: [editTags: true])
    }

    def hideCatalog() {
        ProductCatalog productCatalog = ProductCatalog.get(params.id)
        productCatalog.active = false
        productCatalog.save(flush: true)
        redirect(controller: "dashboard", action: "index", params: [editCatalogs: true])
    }

    def status() {
        def admin = User.get(1)
        def comments = Comment.findAllBySenderAndRecipient(admin, admin)

        def results = comments.collect {
            if (it.dateSent > new Date()) {
                [id: it.id, comment: warehouse.message(code: it.comment, args: [format.datetime(obj: it.dateSent)]), dateSent: it.dateSent]
            }
        }
        render results as JSON
    }

    def footer() {
        render(template: "/common/footer", model: [gitProperties:gitProperties])
    }

    def megamenu() {
        Location location = Location.get(session.warehouse?.id)
        if (!location.supports(ActivityCode.MANAGE_INVENTORY) && location.supports(ActivityCode.SUBMIT_REQUEST)) {
            return [ menu: [] ]
        }

        Map menuConfig = grailsApplication.config.openboxes.megamenu;
        User user = User.get(session?.user?.id)

        if (userService.hasHighestRole(user, session?.warehouse?.id, RoleType.ROLE_AUTHENTICATED)) {
            menuConfig = grailsApplication.config.openboxes.requestorMegamenu;
        }
        List translatedMenu = megamenuService.buildAndTranslateMenu(menuConfig, user, location)

        // Separate configuration section from the rest of the megamenu since it will be rendered separately as menuicon
        def indexOfConfigurationSection = translatedMenu.findIndexOf{ it?.id == 'configuration' }
        def configurationSection = indexOfConfigurationSection > 0 ? translatedMenu.remove(indexOfConfigurationSection) : null

        [
                menu                    : translatedMenu,
                configurationSection    : configurationSection,
                megamenuConfig          : grailsApplication.config.openboxes.megamenu,
                menuSectionsUrlParts    : grailsApplication.config.openboxes.menuSectionsUrlParts as JSON
        ]
    }

    //@CacheFlush(["dashboardCache", "megamenuCache", "inventoryBrowserCache", "fastMoversCache",
    //        "binLocationReportCache", "binLocationSummaryCache", "quantityOnHandCache", "selectTagCache",
    //        "selectTagsCache", "selectCategoryCache", "selectCatalogsCache", "forecastCache"])
    @CacheEvict(value = "dashboardCache", allEntries = true)
    def flushCache() {
        flash.message = "Data caches have been flushed and inventory snapshot job was triggered"
        RefreshProductAvailabilityJob.triggerNow([locationId: session.warehouse.id, forceRefresh: true])
        RefreshOrderSummaryJob.triggerNow()
        // Required in order to refresh the GSP template cache (primarily for barcode label templates)
        groovyPagesTemplateEngine.clearPageCache()
        redirect(action: "index")
    }

    def chooseLayout() {
        if (params.layout) {
            session.layout = params.layout
        }
        redirect(controller: 'dashboard', action: 'index')
    }

    @Transactional
    def chooseLocation() {

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

            if (warehouse.supports(ActivityCode.SUBMIT_REQUEST) && !warehouse.supports(ActivityCode.MANAGE_INVENTORY)) {
                redirect(controller: 'dashboard', action: 'index')
                return
            }

            // Successfully logged in and selected a warehouse
            // Try to redirect to the previous action before session timeout
            def targetUri = params.targetUri ?: session.targetUri
            if (targetUri && !targetUri.contains("chooseLocation") && !targetUri.contains("errors")) {
                redirect(uri: targetUri)
                return
            }
            redirect(controller: 'dashboard', action: 'index')
            return
        }

        def loginLocationsMap = locationService.getLoginLocationsMap(user, warehouse, true)
        def savedLocations = user.warehouse && loginLocationsMap.containsValue(user.warehouse) ? [user.warehouse] : null

        if (userAgentIdentService.isMobile()) {
            render (view: "/mobile/chooseLocation",
                    model: [savedLocations: savedLocations, loginLocationsMap: loginLocationsMap])
            return
        }

        [savedLocations: savedLocations, loginLocationsMap: loginLocationsMap]
    }


    def changeLocation() {
        User user = User.get(session.user.id)
        Map loginLocationsMap = locationService.getLoginLocationsMap(user, null, true)
        List savedLocations = [user?.warehouse, session?.warehouse].unique() - null
        render(template: "loginLocations", model: [savedLocations: savedLocations, loginLocationsMap: loginLocationsMap])
    }

    def downloadGenericProductSummaryAsCsv() {
        def location = Location.get(session?.warehouse?.id)
        def genericProductSummary = inventoryService.getGenericProductSummary(location)

        def data = (params.status == "ALL") ?
                genericProductSummary.values().flatten() :
                genericProductSummary[params.status]

        // Rename columns and filter out debugging columns
        data = data.collect {
            ["Status"         : it.status,
             "Generic Product": it.name,
             "Minimum Qty"    : it.minQuantity,
             "Reorder Qty"    : it.reorderQuantity,
             "Maximum Qty"    : it.maxQuantity,
             "Available Qty"  : it.currentQuantity]
        }

        def sw = new StringWriter()
        if (data) {
            def columns = data[0].keySet().collect { value -> StringEscapeUtils.escapeCsv(value) }
            sw.append(columns.join(",")).append("\n")
            data.each { row ->
                def values = row.values().collect { value ->
                    if (value?.toString()?.isNumber()) {
                        value
                    } else if (value instanceof Collection) {
                        StringEscapeUtils.escapeCsv(value.toString())
                    } else {
                        StringEscapeUtils.escapeCsv(value.toString())
                    }
                }
                sw.append(values.join(","))
                sw.append("\n")
            }
        }
        response.setHeader("Content-disposition", "attachment; filename=\"GenericProductSummary-${params.status}-${location.name}-${new Date().format("yyyyMMdd-hhmm")}.csv\"")
        render(contentType: "text/csv", text: sw.toString())
        return
    }

    def downloadFastMoversAsCsv() {
        println "exportFastMoversAsCsv: " + params
        def location = Location.get(params?.location?.id ?: session?.warehouse?.id)

        def date = new Date()
        if (params.date) {
            def dateFormat = new SimpleDateFormat("MM/dd/yyyy")
            date = dateFormat.parse(params.date)
            date.clearTime()
        }

        def data = dashboardService.getFastMovers(location, date, params.max)
        def sw = new StringWriter()
        if (data?.results) {
            // Write column headers
            def columns = data?.results[0]?.keySet()?.collect { value -> StringEscapeUtils.escapeCsv(value) }
            sw.append(columns?.join(",")).append("\n")

            // Write all data
            data.results.each { row ->
                def values = row.values().collect { value ->
                    if (value?.toString()?.isNumber()) {
                        value
                    } else if (value instanceof Collection) {
                        StringEscapeUtils.escapeCsv(value.toString())
                    } else {
                        StringEscapeUtils.escapeCsv(value.toString())
                    }
                }
                sw.append(values?.join(","))
                sw.append("\n")
            }
        } else {
            sw.append("${warehouse.message(code: 'fastMovers.empty.message')}")
        }
        response.setHeader("Content-disposition", "attachment; filename=\"FastMovers-${location.name}-${new Date().format("yyyyMMdd-hhmm")}.csv\"")
        render(contentType: "text/csv", text: sw.toString())
        return
    }

}
