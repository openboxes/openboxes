package org.pih.warehouse.inventory

import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.pih.warehouse.core.LocalizationService
import org.pih.warehouse.importer.ImportDataCommand

class CycleCountImportService {

    LocalizationService localizationService

    void validateCountImport(ImportDataCommand command) {
        // Firstly clear the command's errors, as it might contain some additional errors related to the file itself
        // that we do not care about while validating the data, and we don't them to be visible for a user
        command.clearErrors()
        // Store items that should be ignored for an import - for some cases we want not only to display an error, but also to ignore such row
        List<Map> itemsToRemove = []
        command.data.eachWithIndex { row, index ->
            CycleCountRequest cycleCountRequest = CycleCount.read(row.cycleCountId)?.cycleCountRequest
            // If a user provides an invalid cycle count id, skip that row and do not look for other errors, as they do not matter at this point
            if (!cycleCountRequest) {
                command.errors.reject("Row ${index + 1}: This row is skipped. Cycle count with given cycle count id ${row.cycleCountId} does not exist")
                itemsToRemove.add(row)
                // as explained above - since a cycle count is not found, there is no point to validate the row itself
                return
            }
            CycleCountItem cycleCountItem = CycleCountItem.read(row.cycleCountItemId)
            // If a cycle count item is not found, but user provided an id, do not treat such row as a custom row, but throw an error and ignore the row.
            if (!cycleCountItem && row.cycleCountItemId) {
                command.errors.reject("Row ${index + 1}: This row is skipped. Cycle count item with given cycle count item id ${row.cycleCountItemId} does not exist")
                itemsToRemove.add(row)
            }
            // Ignore the row if product code is blank
            if (!row.productCode) {
                command.errors.reject("Row ${index + 1}: This row was ignored in the import because product code was blank - you must use a product associated with this cycle count")
                itemsToRemove.add(row)
            }
            // Ignore the row is product code is different than the product code that cycle count is associated with
            if (row.productCode != cycleCountRequest.product.productCode) {
                command.errors.reject("Row ${index + 1}: This row is skipped, because you used an invalid product - you must use a product associated with this cycle count")
                itemsToRemove.add(row)
            }
            // If bin location is not null, but an object with id and name properties, it means it is not a default bin
            // but user provided an invalid location id
            if (row.binLocation != null && !row.binLocation.id) {
                command.errors.reject("Row ${index + 1}: Provided bin location does not exist in the system")
            }
            // Validation cases for an existing item (update)
            // We mustn't update bin location, lot number and expiration date via import for an existing item
            if (cycleCountItem) {
                if (row.binLocation?.id != cycleCountItem.location?.id) {
                    command.errors.reject("Row ${index + 1}: You cannot update bin location for an existing item")
                }
                if (row.lotNumber != cycleCountItem.inventoryItem?.lotNumber) {
                    command.errors.reject("Row ${index + 1}: You cannot update lot number for an existing item")
                }
                if (row.expirationDate != cycleCountItem.inventoryItem.expirationDate) {
                    command.errors.reject("Row ${index + 1}: You cannot update expiration date for an existing item")
                }
            }

        }
        // In the end remove from the list all rows that are supposed to be ignored
        command.data.removeAll(itemsToRemove)
    }

    List<String> buildErrors(ImportDataCommand command) {
        ApplicationTagLib g = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
        return command.errors.allErrors.collect {
            g.message(error: it, locale: localizationService.currentLocale)
        }
    }
}
