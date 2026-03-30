package org.pih.warehouse.order

import org.springframework.stereotype.Component

import org.pih.warehouse.core.ReferenceDocument
import org.pih.warehouse.core.history.EventLog
import org.pih.warehouse.core.history.EventLogHistoryProvider

@Component
class OrderHistoryProvider extends EventLogHistoryProvider<Order> {

    @Override
    ReferenceDocument getReferenceDocument(Order source) {
        return new ReferenceDocument(
                label: source.orderNumber,
                url: "/openboxes/order/show/${source.id}",
                id: source.id,
                identifier: source.orderNumber,
                description: source.description,
                name: source.name,
        )
    }

    @Override
    Collection<EventLog> getEventLogs(Order source) {
        return source.eventLogs
    }
}
