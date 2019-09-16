package org.pih.warehouse.modules

import geb.Module

class AddContainerToShipmentModule extends Module {
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

    def addContainer(container){
        packingUnit.value(container.unit)
        weight.value(container.weight)
        caseHeight.value(container.height)
        caseWidth.value(container.width)
        caseLength.value(container.length)
    }
}
