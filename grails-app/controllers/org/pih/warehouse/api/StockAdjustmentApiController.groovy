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
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.pih.warehouse.inventory.AdjustStockCommand
import org.pih.warehouse.core.Location

@Transactional
class StockAdjustmentApiController {

    def inventoryService
    def productAvailabilityService

    def create(AdjustStockCommand adjustStockCommand) {
        String locationId = params?.location?.id ?: session?.warehouse?.id
        Location location = Location.get(locationId)

        if (!location) {
            throw new IllegalArgumentException("Cannot create stock adjustments without a location - sign in or provide location.id as a request parameter")
        }

        adjustStockCommand.location = location
        inventoryService.adjustStock(adjustStockCommand)

        if (adjustStockCommand.hasErrors()) {
            throw new ValidationException("Unable to adjust stock", adjustStockCommand.errors)
        }

        render([data: adjustStockCommand] as JSON)
    }
}

