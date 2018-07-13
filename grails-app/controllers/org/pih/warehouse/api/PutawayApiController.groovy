/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.api

import grails.converters.JSON
import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import org.codehaus.groovy.grails.validation.Validateable
import org.codehaus.groovy.grails.web.binding.DataBindingUtils
import org.codehaus.groovy.grails.web.json.JSONObject
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Container

/**
 * Should not extend BaseDomainApiController since stocklist is not a valid domain.
 */
class PutawayApiController {

    def putawayService
    def inventoryService
    def identifierService

    def list = {
        Location location = Location.get(params?.location?.id)
        if (!location) {
            throw new IllegalArgumentException("Must provide location.id as request parameter")
        }
        List putawayItems = putawayService.getPutawayCandidates(location)
        render ([data:putawayItems.collect { it.toJson() }] as JSON)
	}

    def create = { Putaway putaway ->
        JSONObject jsonObject = request.JSON
        bindData(putaway, jsonObject)
        jsonObject.putawayItems.each { putawayItemMap ->
            PutawayItem putawayItem = new PutawayItem()
            bindData(putawayItem, putawayItemMap)

            if (!putaway.putawayNumber) {
                putaway.putawayNumber = identifierService.generateOrderIdentifier()
            }

            putawayItem.availableItems =
                    inventoryService.getAvailableBinLocations(putawayItem.currentFacility, putawayItem.product)

            putaway.putawayItems.add(putawayItem)
        }

        if (putaway?.putawayStatus?.equals(PutawayStatus.COMPLETE)) {
            putawayService.putawayStock(putaway)
        }

        render ([data:putaway?.toJson()] as JSON)
    }
}

enum PutawayStatus {
    TODO, PENDING, COMPLETE
}

@Validateable
class Putaway {

    String putawayNumber
    Person putawayAssignee
    Date putawayDate

    PutawayStatus putawayStatus
    List<PutawayItem> putawayItems = []
        //LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(PutawayItem.class));

    static constrants = {
        putawayNumber(nullable:true)
        putawayStatus(nullable:true)
        putawayAssignee(nullable:true)
        putawayDate(nullable:true)
        putawayItems(nullable:true)
    }

    Map toJson() {
        return [
                putawayNumber: putawayNumber,
                putawayStatus: putawayStatus?.name(),
                putawayDate: putawayDate,
                putawayAssignee: putawayAssignee,
                putawayItems: putawayItems.collect { it?.toJson() }
        ]
    }

}

@Validateable
class PutawayItem {

    Product product
    InventoryItem inventoryItem
    Container container
    Location currentFacility
    Location currentLocation
    Location putawayFacility
    Location putawayLocation
    Person recipient
    BigDecimal quantity
    List<AvailableItem> availableItems
    PutawayStatus putawayStatus
    Transaction transaction


    Map toJson() {
        return [
                putawayStatus: transaction ? PutawayStatus.COMPLETE.name() : putawayStatus?.name(),
                transactionNumber: transaction?.transactionNumber,
                "currentFacility.id": currentFacility?.id,
                "currentFacility.name": currentFacility?.name,
                "currentLocation.id": currentLocation?.id,
                "currentLocation.name": currentLocation?.name,
                container: container,
                "product.id": product?.id,
                productCode: product?.productCode,
                "product.name": product?.name,
                "inventoryItem.id": inventoryItem?.id,
                lotNumber: inventoryItem?.lotNumber,
                expirationDate: inventoryItem?.expirationDate,
                "recipient.id": recipient?.id,
                "recipient.name": recipient?.name,
                availableBins: availableItems?.collect { it?.binLocation?.name },
                "putawayFacility.id": putawayFacility?.id,
                "putawayFacility.name": putawayFacility?.name,
                "putawayLocation.id": putawayLocation?.id,
                "putawayLocation.name": putawayLocation?.name,
                quantity: quantity
        ]
    }
}