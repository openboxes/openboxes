package org.pih.warehouse.inventory

import grails.gorm.services.Query
import grails.gorm.services.Service

@Service(InventoryItem)
interface InventoryItemDataService {

    InventoryItem save(InventoryItem inventoryItem);

    @Query("select item from InventoryItem item join fetch item.product where item.id = $id")
    InventoryItem getWithProduct(String id);

    void delete(String id);
}
