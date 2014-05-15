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
import org.pih.warehouse.jobs.CalculateQuantityJob

class InventorySnapshotController {

    def inventoryService

    def index = {
        redirect(action:"list")

    }

    def list = {
        //def startDate = new Date() - 14
        //def endDate = new Date() + 14


        //def query = InventorySnapshot.createCriteria()
        //def inventorySnapshots = InventorySnapshot.list([max:100])

        //[inventorySnapshots:inventorySnapshots]
    }

    def show = {
        //def inventorySnapshot = InventorySnapshot.get(params.id)
        //[inventorySnapshot:inventorySnapshot]
    }


    def triggerCalculateQuantityOnHandJob = {


        def results = CalculateQuantityJob.triggerNow()

        render ([started:true, results:results] as JSON)

    }

    def dates = {
        def location = Location.get(session.warehouse.id)
        def dates = inventoryService.getTransactionDates()
        render (dates as JSON)
    }

    def locations = {
        def locations = inventoryService.getDepotLocations()

        render (locations as JSON)

    }

}
