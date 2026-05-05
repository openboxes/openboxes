package org.pih.warehouse.inventory

import org.pih.warehouse.order.OrderTypeCode

class StockHistoryShipmentDto {

    Boolean isFromPurchaseOrder = false
    Boolean isFromReturnOrder = false

    OrderTypeCode purchaseOrderTypeCode

    String returnOrderTypeCode
    String returnOrderTypeName
}
