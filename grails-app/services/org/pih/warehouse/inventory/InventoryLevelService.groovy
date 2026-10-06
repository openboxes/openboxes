package org.pih.warehouse.inventory

import grails.gorm.transactions.Transactional

@Transactional(readOnly = true)
class InventoryLevelService {

    PreferredBinLocationsDto getPreferredBinLocations(PreferredBinLocationsCommand command) {
        List<InventoryLevel> inventoryLevels = InventoryLevel.createCriteria().list {
            eq("inventory", command.facility.inventory)
            "in"("product", command.products)
            isNotNull("preferredBinLocation")
        }

        return PreferredBinLocationsDto.from(inventoryLevels)
    }
}
