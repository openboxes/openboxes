/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.importer

import grails.gorm.transactions.Transactional
import grails.plugins.csv.CSVWriter
import org.grails.plugins.excelimport.ExpectedPropertyType
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.InventoryStatus
import org.pih.warehouse.product.Product

@Transactional
class InventoryLevelImportDataService implements ImportDataService {

    @Override
    void validateData(ImportDataCommand command) {
        log.info "validate inventory levels " + command.filename

        command.data.eachWithIndex { row, index ->
            if (!validateInventoryLevel(row)) {
                command.errors.reject("Row ${index + 1}: Failed validation")
            }
        }
    }

    @Override
    void importData(ImportDataCommand command) {
        command.data.eachWithIndex { Map params, Integer index ->
            importInventoryLevel(params, index)
        }
    }

    def validateInventoryLevel(Map params) {
        params.each { key, value ->
            def expectedType = InventoryLevelExcelImporter.propertyMap.get(key).expectedType
            switch (expectedType) {
                case ExpectedPropertyType.IntType:
                    assert !value || value instanceof Number || value instanceof Boolean, "Value [${value}] for column [${key}] must be a Number or Boolean but was ${value?.class?.name} (" + row + ")."
                    break
                case ExpectedPropertyType.DateType:
                    assert !value || value instanceof Date, "Value [${value}] for column [${key}] must be a Date but was ${value?.class?.name} (" + params + ")."
                    break
                default:
                    break
            }
        }
        return true
    }

    /**
     * Import a single row from the XLS file
     *
     * @param location
     * @param fileName
     * @return
     */
    def importInventoryLevel(Map params, Integer index) {
        log.info "Import inventory levels(${index}): " + params

        def product = Product.findByProductCode(params.productCode as String)
        if (!product) {
            throw new IllegalArgumentException("Product with product code ${params.productCode} was not found")
        }

        // Validate that the facility exists
        Location facility = Location.findByName(params.inventory as String)
        if (!facility || !facility.inventory) {
            throw new IllegalArgumentException("Facility with name ${params.inventory} was not found or is not valid")
        }

        // TODO Any record with a non-empty internal location is a bin replenishment rule rather than an facility-based
        //  inventory level rule. In order to protect the bin replenishment rules from being overwritten with this
        //  data import feature, we're going to skip these records for now. The best way to deal with these will be to
        //  create a separate importer/exporter to handle the bin replenishment rules separately. Or we can remove
        //  this check and ensure that the following code handles these rules properly (i.e. most of the time the
        //  bin replenishment only needs internal location, min quantity, reorder quantity, max quantity). Therefore,
        //  we should create separate command classes to validate the two types of inventory levels.
        if (params.internalLocation) {
            log.info "Skipping update to bin replenishment rule ${params}"
            return
        }

        // Resolve any objects that require a database lookup
        params.internalLocation = params.internalLocation ? resolveInternalLocation(facility, params.internalLocation as String) : null
        params.preferredBinLocation = params.preferredBinLocation ? resolveInternalLocation(facility, params.preferredBinLocation as String) : null
        params.replenishmentLocation = params.replenishmentLocation ? resolveInternalLocation(facility, params.replenishmentLocation as String) : null

        // Update the inventory level
        InventoryLevel inventoryLevel = updateOrCreateInventoryLevel(facility, product, params)

        // Delete the inventory level if the status has been set to inactive,
        if (inventoryLevel.status == InventoryStatus.INACTIVE) {
            product.removeFromInventoryLevels(inventoryLevel)
            inventoryLevel.delete()
            product.save()
        } else {
            // Save inventory level OR we could return the inventory level and save them all at the end
            product.addToInventoryLevels(inventoryLevel)
            inventoryLevel.save()
            product.save()
        }
    }

    Location resolveInternalLocation(Location facility, String internalLocationName) {
        Location internalLocation = facility.getInternalLocation(internalLocationName)
        if (!internalLocation) {
            throw new IllegalArgumentException("Unable to locate internal location ${internalLocationName} within facility ${facility?.name}")
        }
        return internalLocation
    }

    /**
     * Create or update inventory level on product
     *
     * @param product
     * @param inventory
     * @param preferredBinLocation
     * @param minQuantity
     * @param reorderQuantity
     * @param maxQuantity
     * @return
     */
    InventoryLevel updateOrCreateInventoryLevel(Location facility, Product product, Map params) {
        InventoryLevel inventoryLevel = findOrCreateInventoryLevel(facility, product)

        inventoryLevel.status = params.status ? params.status as InventoryStatus : InventoryStatus.SUPPORTED
        inventoryLevel.abcClass = params.abcClass
        inventoryLevel.minQuantity = params.minQuantity as Integer
        inventoryLevel.reorderQuantity = params.reorderQuantity as Integer
        inventoryLevel.maxQuantity = params.maxQuantity as Integer
        inventoryLevel.expectedLeadTimeDays = params.expectedLeadTimeDays as Integer
        inventoryLevel.replenishmentPeriodDays = params.replenishmentPeriodDays as Integer

        // Don't clear locations if columns in import are null
        if (params.internalLocation) {
            inventoryLevel.internalLocation = params.internalLocation as Location
        }
        if (params.preferredBinLocation) {
            inventoryLevel.preferredBinLocation = params.preferredBinLocation as Location
        }
        if (params.replenishmentLocation) {
            inventoryLevel.replenishmentLocation = params.replenishmentLocation as Location
        }

        return inventoryLevel
    }

    /**
     * Find or create an inventory level with the given parameters.
     *
     * @param product
     * @param inventory
     * @param preferredBinLocation
     * @param minQuantity
     * @param reorderQuantity
     * @param maxQuantity
     * @return
     */
    def findOrCreateInventoryLevel(Location facility, Product product) {
        log.info "Product ${product.productCode} facility ${facility}"
        def inventoryLevel = InventoryLevel.findByInventoryAndProductAndBinLocationIsNull(facility.inventory, product)
        if (!inventoryLevel) {
            inventoryLevel = new InventoryLevel()
            inventoryLevel.product = product
            inventoryLevel.inventory = facility.inventory
        }
        return inventoryLevel
    }

    /**
     * Export the given inventory levels
     *
     * @param inventoryLevels
     * @return
     */
    String exportInventoryLevels(Collection inventoryLevels) {
        def sw = new StringWriter()
        def csv = new CSVWriter(sw, {
            "Product Code" { it?.productCode }
            "Product Name" { it?.productName }
            "Inventory" { it?.inventory }
            "Status" { it?.status }
            "Bin Location" { it?.binLocation }
            "Preferred" { it?.preferred }
            "Target Bin Location" { it?.internalLocation }
            "Preferred Putaway Location" { it?.preferredBinLocation }
            "Default Replenishment Source" { it?.replenishmentLocation }
            "ABC Class" { it?.abcClass }
            "Min Quantity" { it?.minQuantity }
            "Reorder Quantity" { it?.reorderQuantity }
            "Max Quantity" { it?.maxQuantity }
            "Forecast Quantity" { it?.forecastQuantity }
            "Forecast Period" { it?.forecastPeriodDays }
            "UOM" { it?.unitOfMeasure }
        })
        inventoryLevels.each { inventoryLevel ->
            csv << [
                    productCode             : inventoryLevel?.product?.productCode,
                    productName             : inventoryLevel?.product?.displayNameWithLocaleCode,
                    inventory               : inventoryLevel?.inventory?.warehouse?.name,
                    status                  : inventoryLevel?.status,
                    binLocation             : inventoryLevel?.binLocation ?: "",
                    preferred               : inventoryLevel?.preferred ?: "",
                    internalLocation        : inventoryLevel?.internalLocation ?: "",
                    preferredBinLocation    : inventoryLevel?.preferredBinLocation ?: "",
                    replenishmentLocation   : inventoryLevel?.replenishmentLocation ?: "",
                    abcClass                : inventoryLevel?.abcClass ?: "",
                    minQuantity             : inventoryLevel?.minQuantity ?: "",
                    reorderQuantity         : inventoryLevel?.reorderQuantity ?: "",
                    maxQuantity             : inventoryLevel?.maxQuantity ?: "",
                    forecastQuantity        : inventoryLevel?.forecastQuantity ?: "",
                    forecastPeriodDays      : inventoryLevel?.forecastPeriodDays ?: "",
                    unitOfMeasure           : inventoryLevel?.product?.unitOfMeasure ?: "EA"
            ]
        }
        return CSVUtils.prependBomToCsvString(csv.writer.toString())
    }
}
