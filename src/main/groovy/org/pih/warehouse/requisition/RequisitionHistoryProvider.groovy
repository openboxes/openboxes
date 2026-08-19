package org.pih.warehouse.requisition

import org.springframework.stereotype.Component

import org.pih.warehouse.core.history.EventLog
import org.pih.warehouse.core.history.EventLogHistoryProvider

@Component
class RequisitionHistoryProvider extends EventLogHistoryProvider<Requisition> {

    @Override
    Collection<EventLog> getEventLogs(Requisition source) {
        return source.eventLogs
    }
}
