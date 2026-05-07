package org.pih.warehouse.inventory

/**
 * Pre-resolved projections of the Shipment / Requisition / Order entities
 * referenced from the rows of the stock history page. Bundled together so the
 * controller makes one orchestrating call and the GSP looks each one up by id
 * without ever materialising the entity (which would trigger the eager
 * one-to-one inverse load on Picklist)
 */
class StockHistoryDisplayContext {

    Map<String, StockHistoryShipmentDto> shipmentDtoById = [:]
    Map<String, StockHistoryRequisitionDto> requisitionDtoById = [:]
    Map<String, StockHistoryOrderDto> orderDtoById = [:]
}
