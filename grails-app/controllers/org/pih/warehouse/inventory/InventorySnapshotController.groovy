/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.inventory

import grails.converters.JSON
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.data.DataService
import org.pih.warehouse.jobs.CalculateQuantityJob
import org.pih.warehouse.product.Product

import java.text.DateFormat
import java.text.SimpleDateFormat

class InventorySnapshotController {

    DataService dataService
    InventorySnapshotService inventorySnapshotService

    def index = {
        redirect(action: "list")
    }

    def list = {}

    def show = {}

    def edit = {}

    def update = {
        println "Update inventory snapshot " + params
        try {
            def dateFormat = new SimpleDateFormat("MM/dd/yyyy")
            def date = dateFormat.parse(params.date)
            date.clearTime()

            def results = CalculateQuantityJob.triggerNow([locationId: params.location.id, date: date])
            render([results: results] as JSON)

        }
        catch (Exception e) {
            log.error("An error occurred while attempting to trigger inventory snapshot update: " + e.message, e)
            render([error: e.class.name, message: e.message] as JSON)
        }
    }

    def triggerCalculateQuantityOnHandJob = {
        def results = CalculateQuantityJob.triggerNow(
                [
                        productId      : params.product.id,
                        locationId     : params.location.id,
                        includeAllDates: false,
                        forceRefresh   : true
                ]
        )
        render([started: true, results: results] as JSON)
    }

    def refresh = {
        log.info("Refresh inventory snapshot data: " + params)
        User user = User.get(session?.user?.id)
        Location location = Location.get(params?.location?.id)
        try {
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy")
            Date date = (params.date) ? dateFormat.parse(params.date) : new Date()

            CalculateQuantityJob.triggerNow([date: date, productId: params.product?.id, locationId: location?.id, userId: user?.id])
        } catch (Exception e) {
            log.error("An error occurred " + e.message, e)
            render([message: "An error occurred: " + e.message] as JSON)
        }
        render([message: "Triggered data refresh at " + new Date().format("MMM dd yyyy hh:mm:ss a") + ". You will receive an email to '${user?.email}' when the process has completed. This may take several minutes."] as JSON)
    }

    def download = {

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy")
        Location location = Location.get(params?.location?.id ?: session?.warehouse?.id)
        Date date = (params.date) ? dateFormat.parse(params.date) : new Date()

        def data = inventorySnapshotService.findInventorySnapshotByDateAndLocation(date, location)

        def csv = dataService.generateCsv(data)
        println "CSV: " + csv
        def filename = "Stock-${location?.name}-${date.format("dd MMM yyyy")}.csv"
        response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
        render(contentType: "text/csv", text: csv.toString(), encoding: "UTF-8")

    }

    /**
     * Analytics > Inventory Snapshot data table
     */

    def findByDateAndLocation = {
        log.info "getInventorySnapshotsByDate: " + params
        try {
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy")
            Date date = (params.date) ? dateFormat.parse(params.date) : new Date()
            Location location = Location.get(params?.location?.id ?: session?.warehouse?.id)

            List data = inventorySnapshotService.findInventorySnapshotByDateAndLocation(date, location)
            render(["aaData": data, "iTotalRecords": data.size() ?: 0, "iTotalDisplayRecords": data.size() ?: 0, "sEcho": 1] as JSON)
        }
        catch (Exception e) {
            log.error("Exception occurred: " + e.message, e)
            response.status = 500
            render([errorMessage: e.message] as JSON)
            return
        }

    }

    def transactionDates = {

        Product product = Product.get(params.id)
        Location location = Location.get(session.warehouse.id)

        def transactionDates = (product) ?
                inventorySnapshotService.getTransactionDates(location, product) :
                inventorySnapshotService.transactionDates

        render([dates: transactionDates] as JSON)
    }

}
