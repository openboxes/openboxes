package org.pih.warehouse.picklist

import org.pih.warehouse.api.PickPageItem
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.importer.ImportDataCommand

class PicklistImportDataCommand extends ImportDataCommand {

    StockMovement stockMovement
    List<PickPageItem> pickPageItems
    List<PicklistItemCommand> picklistItems

    PicklistImportDataCommand() {
        importType = "picklistItems"
    }

    static constraints = {
        // FIXME need to figure out if we can / want to bind stock movement and therefore make this nullable:false
        stockMovement nullable: true
        pickPageItems nullable: true
        picklistItems nullable: true
    }

}
