package org.pih.warehouse.order

import org.springframework.stereotype.Component

import org.pih.warehouse.core.ReferenceDocument
import org.pih.warehouse.core.history.EventLog
import org.pih.warehouse.core.history.EventLogHistoryProvider

@Component
class OrderHistoryProvider extends EventLogHistoryProvider<Order> {

    @Override
    Collection<EventLog> getEventLogs(Order source) {
        return source.eventLogs
    }
}
