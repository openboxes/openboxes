package org.pih.warehouse.inventory

import spock.lang.Specification

import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.core.history.HistoryContext
import org.pih.warehouse.core.history.HistoryItem
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderHistoryProvider
import org.pih.warehouse.putaway.PutawayService
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionHistoryProvider
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentHistoryProvider

class StockMovementHistoryProviderSpec extends Specification {

    OrderHistoryProvider orderHistoryProvider
    ShipmentHistoryProvider shipmentHistoryProvider
    RequisitionHistoryProvider requisitionHistoryProvider
    PutawayService putawayService
    StockMovementHistoryProvider stockMovementHistoryProvider

    void setup() {
        orderHistoryProvider = Mock(OrderHistoryProvider)
        shipmentHistoryProvider = Mock(ShipmentHistoryProvider)
        requisitionHistoryProvider = Mock(RequisitionHistoryProvider)
        putawayService = Mock(PutawayService)
        stockMovementHistoryProvider = new StockMovementHistoryProvider(
                orderHistoryProvider, shipmentHistoryProvider, requisitionHistoryProvider, putawayService)
    }

    void "doGetHistory should include requisition history even when there is no shipment yet"() {
        given:
        Requisition requisition = new Requisition()
        StockMovement stockMovement = new StockMovement(requisition: requisition, shipment: null)
        HistoryItem requisitionHistoryItem = new HistoryItem()

        when:
        List<HistoryItem> historyItems = stockMovementHistoryProvider.doGetHistory(stockMovement, new HistoryContext())

        then:
        1 * requisitionHistoryProvider.getHistory(requisition, _ as HistoryContext) >> [requisitionHistoryItem]
        0 * shipmentHistoryProvider.getHistory(*_)
        0 * putawayService.getPutawayOrders(*_)
        historyItems == [requisitionHistoryItem]
    }

    void "doGetHistory should combine requisition, shipment, and putaway order history"() {
        given:
        Requisition requisition = new Requisition()
        Shipment shipment = new Shipment()
        StockMovement stockMovement = new StockMovement(requisition: requisition, shipment: shipment)

        HistoryItem requisitionHistoryItem = new HistoryItem()
        HistoryItem shipmentHistoryItem = new HistoryItem()
        HistoryItem orderHistoryItem = new HistoryItem()
        Order putawayOrder = new Order()

        when:
        List<HistoryItem> historyItems = stockMovementHistoryProvider.doGetHistory(stockMovement, new HistoryContext())

        then:
        1 * requisitionHistoryProvider.getHistory(requisition, _ as HistoryContext) >> [requisitionHistoryItem]
        1 * shipmentHistoryProvider.getHistory(shipment, _ as HistoryContext) >> [shipmentHistoryItem]
        1 * putawayService.getPutawayOrders(shipment) >> [putawayOrder]
        1 * orderHistoryProvider.getHistory(putawayOrder, _ as HistoryContext) >> [orderHistoryItem]
        historyItems.size() == 3
        historyItems.containsAll([requisitionHistoryItem, shipmentHistoryItem, orderHistoryItem])
    }

    void "doGetHistory should return no history items when the stock movement has neither a requisition nor a shipment"() {
        given:
        StockMovement stockMovement = new StockMovement(requisition: null, shipment: null)

        when:
        List<HistoryItem> historyItems = stockMovementHistoryProvider.doGetHistory(stockMovement, new HistoryContext())

        then:
        0 * requisitionHistoryProvider.getHistory(*_)
        0 * shipmentHistoryProvider.getHistory(*_)
        historyItems == []
    }
}
