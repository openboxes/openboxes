package org.pih.warehouse.api

import org.pih.warehouse.shipping.Container

class PartialReceiptContainer{

    Container container
    String sortBy
    List<PartialReceiptItem> partialReceiptItems = []

    Boolean isDefault() {
        return container == null
    }
    private static int defaultOrderComparison(a, b) {
        return a.shipmentItem?.requisitionItem?.orderIndex <=> b.shipmentItem?.requisitionItem?.orderIndex ?:
                a.shipmentItem?.sortOrder <=> b.shipmentItem?.sortOrder ?:
                        a.receiptItem?.sortOrder <=> b.receiptItem?.sortOrder
    }

    Map toJson() {
        return [
                "container.id"        : container?.id,
                "container.name"      : container?.name,
                "parentContainer.id"  : container?.parentContainer?.id,
                "parentContainer.name": container?.parentContainer?.name,
                "container.type"      : container?.containerType?.name,
                shipmentItems         : partialReceiptItems.sort { a, b ->
                    switch (sortBy) {
                        case "alphabetical":
                            return a.shipmentItem?.product?.name <=> b.shipmentItem?.product?.name ?:
                                    defaultOrderComparison(a, b)
                        case "shipment":
                        default:
                            return defaultOrderComparison(a, b)
                    }
                }
        ]
    }
}
