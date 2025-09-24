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

import grails.databinding.BindUsing
import org.apache.commons.lang.StringUtils
import org.pih.warehouse.EmptyStringsToNullBinder
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import util.InventoryUtil

class InventoryLevel {

    String id

    // Product assigned to inventory level rule
    Product product

    // Internal location assigned to inventory level rule (optional)
    Location internalLocation

    // Inventory status of rule (whether it's enabled / disabled)
    InventoryStatus status = InventoryStatus.SUPPORTED

    // Preferred for reorder (@deprecated)
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
    @BindUsing({ obj, source -> EmptyStringsToNullBinder.bindEmptyStringToNull(source, "expectedLeadTimeDays") })
    BigDecimal expectedLeadTimeDays

    // Replenishment period in days
    @BindUsing({ obj, source -> EmptyStringsToNullBinder.bindEmptyStringToNull(source, "replenishmentPeriodDays") })
    BigDecimal replenishmentPeriodDays

    // Demand time period in days
    @BindUsing({ obj, source -> EmptyStringsToNullBinder.bindEmptyStringToNull(source, "demandTimePeriodDays") })
    BigDecimal demandTimePeriodDays

    // Preferred bin location
    Location preferredBinLocation

    // Location from which we should replenish stock (could be external supplier or internal bin location)
    Location replenishmentLocation

    // Preferred bin location (@deprecated)
    String binLocation

    // ABC analysis class
    @BindUsing({ obj, source -> EmptyStringsToNullBinder.bindEmptyStringToNull(source, "abcClass") })
    String abcClass

    // Additional comments about
    String comments

    // Auditing
    Date dateCreated
    Date lastUpdated

    static belongsTo = [inventory: Inventory]

    static mapping = {
        id generator: 'uuid'
        product index: 'inventory_level_prod_inv_idx'
        inventory index: 'inventory_level_prod_inv_idx'
        cache true
    }

    static transients = ["facilityLocation", "forecastPeriod", "forecastPeriodOptions", "monthlyForecastQuantity"]

    static constraints = {
        status(nullable: true)
        inventory(nullable: false)
        product(nullable: false)
        internalLocation(nullable: true, unique: ["inventory", "product"])
        minQuantity(nullable: true, range: 0..2147483646)
        reorderQuantity(nullable: true, range: 0..2147483646)
        maxQuantity(nullable: true, range: 0..2147483646)
        forecastQuantity(nullable: true, range: 0..2147483646)
        forecastPeriodDays(nullable: true)
        expectedLeadTimeDays(nullable: true)
        preferredBinLocation(nullable: true)
        replenishmentLocation(nullable: true)
        binLocation(nullable: true)
        abcClass(nullable: true, blank: false)
        preferred(nullable: true)
        comments(nullable: true)
        replenishmentPeriodDays(nullable: true)
        demandTimePeriodDays(nullable: true)
    }

    Location getFacilityLocation() {
        return inventory?.warehouse
    }

    List<Location> getAssignedLocations() {
        List<Location> assignedLocations = []
        // The preferred locations are only available at the facility level replenishment rule
        if (!internalLocation) {
            assignedLocations = getAssignedLocationsFromInventoryLevels()
            // Make sure the preferred bin specified on the facility level rule is the at the top of the list
            if (preferredBinLocation) assignedLocations.add(0, preferredBinLocation)
        }
        return assignedLocations.unique()
    }

    List<Location> getAssignedLocationsFromBinLocation() {
        return binLocation
                .split(",")
                .collect { it.trim() }
                .findAll { StringUtils.isNotBlank(it) }
                .collect { String locationNumber ->
                    Location.findByLocationNumber(locationNumber)
                }
                .findAll { it } // remove nulls
    }

    List<Location> getAssignedLocationsFromInventoryLevels() {
        return InventoryLevel.where {
            product == product &&
                    inventory == inventory &&
                    internalLocation != null
        }.projections {
            property "internalLocation"
        }.list()
    }


    def statusMessage(Long currentQuantity) {
        return InventoryUtil.getStatusMessage(status, minQuantity, reorderQuantity, maxQuantity, currentQuantity)
    }

    Integer getMonthlyForecastQuantity() {
        return forecastPeriodDays ? Math.ceil(((Double) (forecastQuantity) / forecastPeriodDays) * 30) : (forecastQuantity * 30)
    }

    static PROPERTIES = [
            productCode          : "product.productCode",
            productName          : "product.name",
            facility             : "inventory",
            status               : "status",
            internalLocation     : "internalLocation",
            preferredBinLocation : "preferredBinLocation",
            replenishmentLocation: "replenishmentLocation",
            abcClass             : "abcClass",
            minQuantity          : "minQuantity",
            reorderQuantity      : "reorderQuantity",
            maxQuantity          : "maxQuantity"
    ]
}
