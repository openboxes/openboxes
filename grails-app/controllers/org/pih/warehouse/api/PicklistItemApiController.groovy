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
import grails.validation.ValidationException
import org.apache.http.HttpStatus
import org.json.JSONObject
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.core.User

class PicklistItemApiController extends BaseDomainApiController {

    def productAvailabilityService

    def read = {
        PicklistItem picklistItem = PicklistItem.get(params.id)

        def data = picklistItem.toJson()

        data.quantityOnHand =
            productAvailabilityService.getQuantityOnHand(picklistItem.inventoryItem)

        data.quantityAvailableToPromise =
                productAvailabilityService.getQuantityAvailableToPromise(picklistItem.requisitionItem.requisition.origin,
                        picklistItem.binLocation,
                        picklistItem.inventoryItem)

        render ([data: data] as JSON)
    }


    def update = {
        JSONObject jsonObject = request.JSON
        log.info "save " + jsonObject


        PicklistItem picklistItem = PicklistItem.get(params.id)

        // Need to add validation and store quantityPicked if
        if (!jsonObject["product.id"]) {
            throw new IllegalArgumentException("Must scan valid product")
        }

        Product product = Product.get(jsonObject["product.id"])
        if (product != picklistItem?.requisitionItem?.product) {
            throw new IllegalArgumentException("Scanned product ${jsonObject.productCode} does not match picklist item ${picklistItem?.requisitionItem?.product?.productCode}")
        }

        BigDecimal quantityPicked = new BigDecimal(jsonObject.quantityPicked)

        if (!picklistItem.quantityPicked) {
            picklistItem.quantityPicked = 0
        }
        picklistItem.quantityPicked += quantityPicked
        picklistItem.datePicked = new Date()
        picklistItem.picker = User.get(session.user.id)
        if (picklistItem.validate() && !picklistItem.hasErrors()) {
            picklistItem.save(flush:true)
        }
        else {
            throw new ValidationException("Unable to save picklist item", picklistItem.errors)
        }

        render HttpStatus.SC_OK
    }


}
