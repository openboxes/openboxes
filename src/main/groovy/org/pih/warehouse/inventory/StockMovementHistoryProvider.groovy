package org.pih.warehouse.inventory

import org.springframework.stereotype.Component

import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.core.history.HistoryContext
import org.pih.warehouse.core.history.HistoryItem
import org.pih.warehouse.core.history.HistoryProvider
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderHistoryProvider
import org.pih.warehouse.putaway.PutawayService
import org.pih.warehouse.shipping.ShipmentHistoryProvider

/**
 * Constructs a history of actions performed on a stock movement.
 *
 * Stock movement history is built by combining the history from a number of different sources, including
 * shipment and order.
 */
@Component
class StockMovementHistoryProvider extends HistoryProvider<StockMovement> {

    final OrderHistoryProvider orderHistoryProvider
    final ShipmentHistoryProvider shipmentHistoryProvider
    final PutawayService putawayService

    StockMovementHistoryProvider(final OrderHistoryProvider orderHistoryProvider,
                                 final ShipmentHistoryProvider shipmentHistoryProvider,
                                 final PutawayService putawayService) {
        this.orderHistoryProvider = orderHistoryProvider
        this.shipmentHistoryProvider = shipmentHistoryProvider
        this.putawayService = putawayService
    }

    @Override
    List<HistoryItem> doGetHistory(StockMovement source, HistoryContext context) {
        List<HistoryItem> historyItems = []
        if (!source?.shipment) {
            return historyItems
        }

        historyItems.addAll(shipmentHistoryProvider.getHistory(source.shipment, context))

        // The only orders that have event history are putaway orders so don't bother fetching anything else.
        Collection<Order> orders = putawayService.getPutawayOrders(source?.shipment)
        for (Order order in orders) {
            historyItems.addAll(orderHistoryProvider.getHistory(order, context))
        }

        return historyItems
    }
}
