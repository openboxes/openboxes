package org.pih.warehouse.inventory

import grails.gorm.services.Join
import grails.gorm.services.Service

@Service(InventoryItem)
interface InventoryItemDataService {

    InventoryItem save(InventoryItem inventoryItem)

    @Join("product")
    InventoryItem getWithProduct(String id)

    void delete(String id)
}
