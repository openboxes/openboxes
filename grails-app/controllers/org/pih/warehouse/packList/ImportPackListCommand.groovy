package org.pih.warehouse.packList

import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.importer.ImportDataCommand

class ImportPackListCommand extends ImportDataCommand {

    StockMovement stockMovement
    List<ImportPackListItemCommand> packImportItems

    ImportPackListCommand() {
        location = AuthService.currentLocation
        importType = "packListItems"
    }

    static constraints = {
        stockMovement nullable: true
        packImportItems nullable: true
    }
}
