package org.pih.warehouse.shipping

import org.springframework.stereotype.Component

import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.EventTypeDto
import org.pih.warehouse.core.history.HistoryContext
import org.pih.warehouse.core.history.HistoryItem
import org.pih.warehouse.core.history.EventLog
import org.pih.warehouse.core.history.EventLogHistoryProvider

@Component
class ShipmentHistoryProvider extends EventLogHistoryProvider<Shipment> {

    @Override
    Collection<EventLog> getEventLogs(Shipment source) {
        return source.eventLogs
    }

    @Override
    List<HistoryItem> doGetHistory(Shipment source, HistoryContext context=new HistoryContext()) {
        // A (temporary) backwards compatibility check. We don't want to generate a partial history for shipments that
        // were in progress when event logs were first introduced so we only rely on event logs if all of the shipment
        // events have been logged. We know that the SHIPPED event is the first logged event, so if the shipment has it,
        // it's safe to rely on event logs. Otherwise, we'll rely on the events.
        return source.eventLogs?.any { it.eventCode == EventCode.SHIPPED } ?
                getHistoryFromEventLogs(source, context) :
                getHistoryFromEvents(source)
    }

    /**
     * Build the history for the shipment using its EventLogs.
     */
    private List<HistoryItem> getHistoryFromEventLogs(Shipment source, HistoryContext context) {
        List<HistoryItem> historyItems = []

        // The entry for the initial CREATED event must be added manually. See the docstring for details.
        historyItems.add(getCreatedHistoryItem(source))

        // Then we can simply collect history of each of the EventLogs
        historyItems.addAll(super.doGetHistory(source, context))

        return historyItems
    }

    /**
     * Build the history for Shipments using only the shipment's Events.
     * For use on shipments that were started before we introduced event logs.
     */
    private List<HistoryItem> getHistoryFromEvents(Shipment source) {
        List<HistoryItem> historyItems = []

        // The entry for the initial CREATED event must be added manually. See the docstring for details.
        historyItems.add(getCreatedHistoryItem(source))

        // Then we can simply collect history of each of the Events. We don't need to filter for rollbacks
        // because Events are deleted when they're rolled back.
        historyItems.addAll(getHistoryItemsFromEvents(source, source.events))

        return historyItems
    }

    /**
     * Initialize a new HistoryItem for the CREATED event code.
     *
     * TODO: We need to do this because we don't persist a real CREATED event when creating a shipment. We should
     *       refactor the shipment create logic (which is spread out across multiple different services) to properly
     *       create both an Event and an EventLog.
     */
    private HistoryItem getCreatedHistoryItem(Shipment source) {
        return new HistoryItem(
                dateLogged: source.dateCreated,
                date: source.dateCreated,
                location: source.origin,
                eventType: new EventTypeDto(
                        name: messageLocalizer.localizeEnumValue(EventCode.CREATED),
                        eventCode: EventCode.CREATED,
                ),
                referenceDocument: source.getReferenceDocument(),
                createdBy: source.createdBy,
        )
    }
}
