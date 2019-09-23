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

import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import util.InventoryUtil

class InventoryLevel {

    String id

    Product product

    InventoryStatus status = InventoryStatus.SUPPORTED

    Boolean preferred = Boolean.FALSE

    // Should warn user when stock is below safety stock level
    Integer minQuantity

    // Should reorder product when quantity falls below this value
    Integer reorderQuantity

    // Should warn user when quantity is above this value
    Integer maxQuantity

    // Amount of stock typically used during forecast period
    BigDecimal forecastQuantity

    // The period for which the forecast quantity is relevant (default is monthly)
    BigDecimal forecastPeriodDays = 30

    // Lead time in days (safety stock is lead time days x daily forecast quantity)
    BigDecimal expectedLeadTimeDays

    // Preferred bin location
    Location preferredBinLocation

    // Location from which we should replenish stock
    Location replenishmentLocation

    // Preferred bin location (deprecated)
    String binLocation

    // ABC analysis class
    String abcClass

    //
    String comments

    // Auditing
    Date dateCreated
    Date lastUpdated

    static mapping = {
        id generator: 'uuid'
        product index: 'inventory_level_prod_inv_idx'
        inventory index: 'inventory_level_prod_inv_idx'
        cache true
    }

    static transients = ["forecastPeriod", "forecastPeriodOptions", "monthlyForecastQuantity"]
    static belongsTo = [inventory: Inventory]

    static constraints = {
        status(nullable: true)
        product(nullable: false, unique: "inventory")
        minQuantity(nullable: true, range: 0..2147483646)
        reorderQuantity(nullable: true, range: 0..2147483646)
        maxQuantity(nullable: true, range: 0..2147483646)
        forecastQuantity(nullable: true, range: 0..2147483646)
        forecastPeriodDays(nullable: true)
        expectedLeadTimeDays(nullable: true)
        preferredBinLocation(nullable: true)
        replenishmentLocation(nullable: true)
        binLocation(nullable: true)
        abcClass(nullable: true)
        preferred(nullable: true)
        comments(nullable: true)
    }

    def statusMessage(Long currentQuantity) {
        return InventoryUtil.getStatusMessage(status, minQuantity, reorderQuantity, maxQuantity, currentQuantity)
    }

    String toString() {
        return "${product?.productCode}:${preferred}:${minQuantity}:${reorderQuantity}:${maxQuantity}:${lastUpdated}"
    }

    Integer getMonthlyForecastQuantity() {
        return forecastPeriodDays ? Math.ceil(((Double) (forecastQuantity) / forecastPeriodDays) * 30) : (forecastQuantity * 30)
    }

}
