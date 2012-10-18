package org.pih.warehouse.modules

import geb.Module

class AddSuitcaseToShipmentModule extends Module {
    static content = {
        packingUnit {$("input", id: "name")}
        weight {$("input", id: "weight")}
        caseHeight {$("input", id: "height")}
        caseWidth {$("input", id: "width")}
        caseLength {$("input", id: "length")}
        recipient {$("input", id: "recipient-suggest")}
        saveButton {$("input", id: "_eventId_saveContainer")}
        addItemButton {$("input", id: "_eventId_addItemToContainer")}
    }
}
