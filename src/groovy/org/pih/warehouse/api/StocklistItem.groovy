package org.pih.warehouse.api

import org.pih.warehouse.core.Person
import org.pih.warehouse.requisition.RequisitionItem

class StocklistItem {

    RequisitionItem requisitionItem

    Person manager
    Integer maxQuantity
    Integer replenishmentPeriod

    String stocklistId

    static StocklistItem createFromRequisitionItem(RequisitionItem requisitionItem) {
        return new StocklistItem(requisitionItem: requisitionItem)
    }

    Map toJson() {
        return [
                "requisitionItem.id": requisitionItem?.id,
                stocklistId: requisitionItem?.requisition?.id,
                name: requisitionItem?.requisition?.name,
                "location.id": requisitionItem?.requisition?.destination?.id,
                "location.name": requisitionItem?.requisition?.destination?.name,
                "locationGroup.id": requisitionItem?.requisition?.destination?.locationGroup?.id,
                "locationGroup.name": requisitionItem?.requisition?.destination?.locationGroup?.name,
                "manager.id": requisitionItem?.requestedBy?.id,
                "manager.name": requisitionItem?.requestedBy?.name,
                uom: requisitionItem?.product?.unitOfMeasure,
                maxQuantity: requisitionItem?.quantity,
                replenishmentPeriod: replenishmentPeriod,
        ]
    }
}
