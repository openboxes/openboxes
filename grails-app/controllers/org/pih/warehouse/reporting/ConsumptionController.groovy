/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 * */
package org.pih.warehouse.reporting

import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransactionEntry
import org.pih.warehouse.product.Product

class ConsumptionController {

    InventoryService inventoryService

    def show = { ShowConsumptionCommand command ->

        println "Params: " + params
        println "Show consumption command " + command.toLocations

        if (!command.fromLocation) {
            command.fromLocation = Location.get(session.warehouse.id)
        }


        List selectedLocations = [] //= session.invoiceList
        params.each {
            if (it.key.contains("selectedLocation_")){
                if (it.value.contains("on")){
                    //InvoiceItem invoiceItem = invoiceList.get((it.key - "invoiceItem_") as Integer)
                    Location location = Location.get((it.key - "selectedLocation_"))
                    if (location) {
                        selectedLocations << location
                    }
                }
            }
        }

        println "toLocations: " + command.toLocations.size()
        println "selectedLocations: " + selectedLocations.size()
        command.selectedLocations = selectedLocations

        command.transactions = inventoryService.getTransferOutBetweenDates(command.fromLocation, selectedLocations, command.fromDate, command.toDate)
        command.transactions.each { transaction ->
            command.toLocations << transaction.destination
            transaction.transactionEntries.each { transactionEntry ->
                def currentQuantity =
                    command.productMap[transactionEntry.inventoryItem.product]

                if (!currentQuantity) {
                    command.productMap[transactionEntry.inventoryItem.product] = 0
                }

                command.productMap[transactionEntry.inventoryItem.product] += transactionEntry.quantity
            }
        }

        command?.toLocations?.unique()?.sort()
        if (!command?.selectedLocations) {
            command.selectedLocations = command.toLocations
        }
        [command:command]
    }


}


class ShowConsumptionCommand {

    Date fromDate
    Date toDate

    List<Location> toLocations = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Location.class));
    List<Location> selectedLocations = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Location.class));
    Location fromLocation
    Location toLocation
    List<Transaction> transactions = []
    List<TransactionEntry> transactionEntries = []
    def productMap = new TreeMap();

    static constraints = {
        toLocations(nullable: true)
        fromLocation(nullable: true)
        toLocation(nullable: true)
        fromDate(nullable: true)
        toDate(nullable: true)
    }

}
