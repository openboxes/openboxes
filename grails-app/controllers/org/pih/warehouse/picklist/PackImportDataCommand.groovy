package org.pih.warehouse.picklist

import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.importer.ImportDataCommand

class PackImportDataCommand extends ImportDataCommand {

    StockMovement stockMovement
    List<PackImportItemCommand> packImportItems

    PackImportDataCommand() {
        location = AuthService.currentLocation
        importType = "packListItems"
    }

    static constraints = {
        stockMovement nullable: true
        packImportItems nullable: true
    }
}
