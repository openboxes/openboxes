package org.pih.warehouse.api

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.ReasonCode
import org.pih.warehouse.inventory.InventoryItem

class StockAdjustment {

    InventoryItem inventoryItem
    Location binLocation
    Integer quantityAdjusted
    Integer quantityAvailable
    ReasonCode reasonCode
    String comments
    Person adjustedBy
    Date dateAdjusted

    Map toJson() {
        return [
                "inventoryItem.id"            : inventoryItem?.id,
                "inventoryItem.lotNumber"     : inventoryItem?.lotNumber,
                "inventoryItem.expirationDate": inventoryItem?.expirationDate?.format("MM/dd/yyyy"),
                "product.id"                  : inventoryItem?.product?.id,
                "product.productCode"         : inventoryItem?.product?.productCode,
                "product.name"                : inventoryItem?.product?.name,
                "binLocation.id"              : binLocation?.id,
                "binLocation.name"            : binLocation?.name,
                quantityAvailable             : quantityAvailable,
                quantityAdjusted              : quantityAdjusted,
                dateAdjusted                  : dateAdjusted,
                adjustedBy                    : adjustedBy,
                reasonCode                    : reasonCode?.name(),
                comments                      : comments
        ]
    }
}
