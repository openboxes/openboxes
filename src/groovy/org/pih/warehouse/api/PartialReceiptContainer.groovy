package org.pih.warehouse.api

import org.pih.warehouse.shipping.Container

class PartialReceiptContainer {

    Container container
    List<PartialReceiptItem> partialReceiptItems = []

    Boolean isDefault() {
        return container == null
    }


    Map toJson() {
        return [
                "container.id"        : container?.id,
                "container.name"      : container?.name,
                "parentContainer.id"  : container?.parentContainer?.id,
                "parentContainer.name": container?.parentContainer?.name,
                "container.type"      : container?.containerType?.name,
                shipmentItems         : partialReceiptItems.sort { a, b ->
                    a.shipmentItem?.requisitionItem?.orderIndex <=> b.shipmentItem?.requisitionItem?.orderIndex ?:
                            a.shipmentItem?.sortOrder <=> b.shipmentItem?.sortOrder ?:
                                    a.receiptItem?.sortOrder <=> b.receiptItem?.sortOrder
                }
        ]
    }

}
