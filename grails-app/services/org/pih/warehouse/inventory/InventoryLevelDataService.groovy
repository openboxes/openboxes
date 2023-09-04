package org.pih.warehouse.inventory

import grails.gorm.services.Service

@Service(InventoryLevel)
interface InventoryLevelDataService {

    InventoryLevel save(InventoryLevel inventoryLevel)
}
