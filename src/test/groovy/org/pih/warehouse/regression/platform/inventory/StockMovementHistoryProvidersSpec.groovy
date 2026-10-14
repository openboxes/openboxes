package org.pih.warehouse.regression.platform.inventory

import org.pih.warehouse.inventory.*
import spock.lang.Specification

import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.core.history.HistoryContext
import org.pih.warehouse.core.history.HistoryItem
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderHistoryProvider
import org.pih.warehouse.outbound.OutboundHistoryProvider
import org.pih.warehouse.putaway.PutawayService
import org.pih.warehouse.requisition.RequisitionHistoryProvider
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentHistoryProvider

/**
 * Stock movement history is the history of its shipment followed by the history of the putaways of that shipment,
 * sorted and limited like any history.
 */
class StockMovementHistoryProvidersSpec extends Specification {

    OrderHistoryProvider orderHistoryProvider = Mock(OrderHistoryProvider)
    ShipmentHistoryProvider shipmentHistoryProvider = Mock(ShipmentHistoryProvider)
    PutawayService putawayService = Mock(PutawayService)
    // OBAF: StockMovementHistoryProvider also takes a RequisitionHistoryProvider (OBLS-905, requisition events in the history)
    RequisitionHistoryProvider requisitionHistoryProvider = Mock(RequisitionHistoryProvider)

    Shipment shipment = new Shipment(name: 'S-1')
    Order putawayA = new Order(orderNumber: 'PUT-A')
    Order putawayB = new Order(orderNumber: 'PUT-B')
    HistoryItem shipped = new HistoryItem(dateLogged: new Date(3000))
    HistoryItem created = new HistoryItem(dateLogged: new Date(1000))
    HistoryItem putawayADone = new HistoryItem(dateLogged: new Date(4000))
    HistoryItem putawayBDone = new HistoryItem(dateLogged: new Date(2000))

    private void stubHistories(HistoryContext context) {
        1 * shipmentHistoryProvider.getHistory(shipment, context) >> [shipped, created]
        1 * putawayService.getPutawayOrders(shipment) >> [putawayA, putawayB]
        1 * orderHistoryProvider.getHistory(putawayA, context) >> [putawayADone]
        1 * orderHistoryProvider.getHistory(putawayB, context) >> [putawayBDone]
    }

    void 'stock movement history combines shipment and putaway history, oldest first'() {
        given:
        HistoryContext context = new HistoryContext()
        StockMovementHistoryProvider provider =
                new StockMovementHistoryProvider(orderHistoryProvider, shipmentHistoryProvider, requisitionHistoryProvider, putawayService)

        when:
        List<HistoryItem> history = provider.getHistory(new StockMovement(shipment: shipment), context)

        then:
        interaction { stubHistories(context) }
        history == [created, putawayBDone, shipped, putawayADone]
    }

    void 'outbound history is limited to the most recent items'() {
        given:
        HistoryContext context = new HistoryContext(limit: 2)
        OutboundHistoryProvider provider =
                new OutboundHistoryProvider(orderHistoryProvider, shipmentHistoryProvider, putawayService)

        when:
        List<HistoryItem> history = provider.getHistory(new OutboundStockMovement(shipment: shipment), context)

        then:
        interaction { stubHistories(context) }
        history == [shipped, putawayADone]
    }

    void 'movements without a shipment have no history'() {
        given:
        HistoryContext context = new HistoryContext()

        expect:
        new StockMovementHistoryProvider(orderHistoryProvider, shipmentHistoryProvider, requisitionHistoryProvider, putawayService)
                .getHistory(new StockMovement(), context) == []
        new OutboundHistoryProvider(orderHistoryProvider, shipmentHistoryProvider, putawayService)
                .getHistory(new OutboundStockMovement(), context) == []
    }
}
