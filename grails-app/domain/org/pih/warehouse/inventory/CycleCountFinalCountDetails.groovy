package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.product.Product

class CycleCountFinalCountDetails implements Serializable {

    Location facility
    Location location
    Product product
    InventoryItem inventoryItem

    CycleCount cycleCount
    CycleCountItem cycleCountItem
    Date dateCounted

    Integer quantityOnHand
    Integer quantityCounted
    Integer quantityVariance

    String comments

    // FIXME Should eventually be a list of enums
    String rootCause

    static constraints = {
        version false
        // FIXME - this is not the correct approach, but i needed a temporary solution to get this domain working
        id composite: ['cycleCount', 'product', 'inventoryItem', 'location']
        table "cycle_count_final_count_details"
    }

    Map toJson() {
        return [
                inventoryItem   : inventoryItem,
                location        : location.toJson(LocationTypeCode.INTERNAL),
                dateCounted     : dateCounted,
                quantityOnHand  : quantityOnHand,
                quantityCounted : quantityCounted,
                quantityVariance: quantityVariance,
                comments        : comments,
                rootCause       : rootCause,
                extra           : [
                        facility      : facility.toBaseJson(),
                        location      : location.toBaseJson(),
                        product       : product,
                        inventoryItem : inventoryItem,
                        cycleCount    : cycleCount,
                        cycleCountItem: cycleCountItem
                ]
        ]
    }

}
