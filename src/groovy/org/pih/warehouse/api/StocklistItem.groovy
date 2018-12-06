package org.pih.warehouse.api

import org.pih.warehouse.requisition.RequisitionItem

class StocklistItem {

    RequisitionItem requisitionItem

    String stocklistId
    Integer maxQuantity

    static StocklistItem createFromRequisitionItem(RequisitionItem requisitionItem) {
        return new StocklistItem(requisitionItem: requisitionItem)
    }

    Map toJson() {
        return [
                "requisitionItem.id": requisitionItem?.id,
                stocklistId: requisitionItem?.requisition?.id,
                name: requisitionItem?.requisition?.name,
                "location.id": requisitionItem?.requisition?.origin?.id,
                "location.name": requisitionItem?.requisition?.origin?.name,
                "locationGroup.id": requisitionItem?.requisition?.origin?.locationGroup?.id,
                "locationGroup.name": requisitionItem?.requisition?.origin?.locationGroup?.name,
                "manager.id": requisitionItem?.requisition?.requestedBy?.id,
                "manager.name": requisitionItem?.requisition?.requestedBy?.name,
                uom: requisitionItem?.product?.unitOfMeasure,
                maxQuantity: requisitionItem?.quantity,
                replenishmentPeriod: null,
        ]
    }
}
