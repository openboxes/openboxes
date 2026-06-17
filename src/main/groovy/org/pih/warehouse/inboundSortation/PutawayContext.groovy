package org.pih.warehouse.inboundSortation

import org.pih.warehouse.core.DeliveryTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.RequisitionItem

class PutawayContext {
    Location facility
    Product product
    InventoryItem inventoryItem
    String lotNumber
    Date expirationDate
    Location currentBinLocation
    Location preferredBin
    Location internalLocation
    Integer quantity
    String backorderReference
    RequisitionItem backorderItem
    DeliveryTypeCode deliveryTypeCode
}
