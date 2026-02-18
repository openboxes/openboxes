package org.pih.warehouse.shipping

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.EventTypeDto
import org.pih.warehouse.core.HistoryItem
import org.pih.warehouse.core.ReferenceDocument
import org.pih.warehouse.core.history.EventLog
import org.pih.warehouse.core.history.EventLogHistoryBuilder
import org.pih.warehouse.receiving.ReceiptHistoryBuilder

@Component
class ShipmentHistoryBuilder extends EventLogHistoryBuilder<Shipment> {

    @Autowired
    ReceiptHistoryBuilder receiptHistoryBuilder

    @Override
    ReferenceDocument getReferenceDocument(Shipment source) {
        return new ReferenceDocument(
                label: source.shipmentNumber,
                url: "/stockMovement/show/${source.requisition?.id ?: source.id}",
                id: source.id,
                identifier: source.shipmentNumber,
                description: source.description,
                name: source.name,
        )
    }

    @Override
    Collection<EventLog> getEventLogs(Shipment source) {
        return source.eventLogs
    }

    @Override
    List<HistoryItem> getHistory(Shipment source) {
        if (source.eventLogs) {
            return getHistoryFromEventLogs(source)
        }
        // Gracefully handle fetching history for shipments from before we introduced event logs.
        return getHistoryPreEventLog(source)
    }

    /**
     * Build the history for the shipment using its EventLogs.
     */
    private List<HistoryItem> getHistoryFromEventLogs(Shipment source) {
        List<HistoryItem> historyItems = []

        // The entry for the initial CREATED event must be added manually. See the docstring for details.
        historyItems.add(getCreatedHistoryItem(source))

        // Then we can simply collect history of each of the EventLogs
        historyItems.addAll(super.getHistory(source))

        return historyItems.sort()
    }

    /**
     * Build the history for shipments that were started before we introduced event logs.
     */
    private List<HistoryItem> getHistoryPreEventLog(Shipment source) {
        List<HistoryItem> histories = []

        // The entry for the initial CREATED event must be added manually. See the docstring for details.
        histories.add(getCreatedHistoryItem(source))

        // Then we can simply collect history of each of the Events
        histories.addAll(getHistoryItemsFromEvents(source, source.events))

        return histories.sort()
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
                referenceDocument: getReferenceDocument(source),
                createdBy: source.createdBy,
        )
    }
}
