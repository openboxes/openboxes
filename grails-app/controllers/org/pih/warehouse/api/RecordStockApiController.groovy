/**
 * Copyright (c) 2025 Partners In Health.  All rights reserved.
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

import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.ProductAvailabilityService
import org.pih.warehouse.inventory.RecordInventoryCommand

class RecordStockApiController {

    InventoryService inventoryService
    ProductAvailabilityService productAvailabilityService

    def saveRecordStock(RecordInventoryCommand command) {
        inventoryService.saveRecordInventoryCommand(command, params)
        // TODO: refactor saveRecordInventoryCommand to not catch exceptions. It does so now because it's used
        //       in a non-API GSP controller which doesn't gracefully handle exceptions. We'll likely need to split it
        //       into API and non-API service methods. Once we do that, we can remove this if check because exceptions
        //       will be thrown in the service if the command is invalid.
        if (command.hasErrors()) {
            throw new ValidationException("Invalid record stock", command.errors)
        }

        // TODO: Move this refresh into saveRecordInventoryCommand. They don't need to be in separate transactions.
        // We disabled recalculating product availability during the record stock operation (to avoid
        // recalculating it multiple times) so now we need to refresh it manually.
        productAvailabilityService.refreshProductsAvailability(
                command?.inventory?.warehouse?.id,
                [command?.product?.id],
                false)

        render([data: command] as JSON)
    }
}
