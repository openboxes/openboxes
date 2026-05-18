package org.pih.warehouse.inventory

class StockHistoryOrderDto {

    String id
    String orderNumber
    String name

    String orderTypeCode
    String orderTypeName

    Boolean isTransferOrder = false
    Boolean isPutawayOrder = false
}
