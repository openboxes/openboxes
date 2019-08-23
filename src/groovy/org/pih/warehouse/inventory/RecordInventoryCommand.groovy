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

import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import org.pih.warehouse.product.Product

class RecordInventoryCommand {

    Product product
    Inventory inventory
    InventoryLevel inventoryLevel
    Integer totalQuantity
    Date transactionDate = new Date()
    String comment
    RecordInventoryRowCommand recordInventoryRow
    List<RecordInventoryRowCommand> recordInventoryRows =
            LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(RecordInventoryRowCommand.class))

    static constraints = {
        product(nullable: false)
        inventory(nullable: true)
        inventoryLevel(nullable: true)
        totalQuantity(nullable: true)
        transactionDate(nullable: false)
        comment(nullable: true)
        recordInventoryRows(validator: { val, obj, errors ->
            def errorsFound = false
            val.each { row ->
                if (row) {
                    println "validate row " + row
                    if (!row?.validate()) {
                        errorsFound = true
                        row.errors.allErrors.each { error ->
                            obj.errors.rejectValue('recordInventoryRows', "recordInventoryCommand.recordInventoryRows.invalid",
                                    [row, error.getField(), error.getRejectedValue()] as Object[],
                                    "Property [${error.getField()}] of lot number #${row?.lotNumber} with value [${error.getRejectedValue()}] is invalid.")
                        }
                    }
                }
                return errorsFound
            }
        })
    }
}


