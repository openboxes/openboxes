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

import org.codehaus.groovy.grails.web.json.JSONObject
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.product.Product

class InventoryItemApiController {

    ProductAvailabilityService productAvailabilityService

    def validateLot = {
        JSONObject jsonObject = request.JSON
        List<Boolean> areLotsValid = jsonObject?.items?.collect {
            InventoryItem inventoryItem = InventoryItem.findByLotNumberAndProduct(it?.lotNumber, Product.findByProductCode(it?.productCode))

            if (!inventoryItem) {
                return true
            }

            Integer availableQuantity = productAvailabilityService.getQuantityOnHand(inventoryItem)
            if (inventoryItem?.expirationDate != Date.parse("MM/dd/yyyy", it?.expirationDate) && availableQuantity > 0) {
                return false
            }

            return true
        }

        if (areLotsValid.contains(false)) {
            render(status: 400)
            return
        }

        render(status: 200)
    }
}
