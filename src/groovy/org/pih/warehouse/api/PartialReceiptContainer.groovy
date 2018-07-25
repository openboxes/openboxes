package org.pih.warehouse.api

import org.pih.warehouse.shipping.Container

class PartialReceiptContainer {

    Container container
    List<PartialReceiptItem> partialReceiptItems = []


    Map toJson() {
        return [
                "container.id": container?.id,
                "container.name": container?.name,
                "container.type": container?.containerType?.name,
                shipmentItems: partialReceiptItems
        ]
    }

}
