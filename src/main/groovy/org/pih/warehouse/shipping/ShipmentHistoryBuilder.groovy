package org.pih.warehouse.shipping

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.EventTypeDto
import org.pih.warehouse.core.HistoryItem
import org.pih.warehouse.core.ReferenceDocument
import org.pih.warehouse.core.date.InstantParser
import org.pih.warehouse.core.history.EventLog
import org.pih.warehouse.core.history.EventLogHistoryBuilder
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptHistoryBuilder

@Component
class ShipmentHistoryBuilder extends EventLogHistoryBuilder<Shipment> {

    @Autowired
    ReceiptHistoryBuilder receiptHistoryBuilder

    /**
     * Use {@link #getReferenceDocument(Shipment, EventLog)} instead as it is more accurate.
     */
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

    /**
     * For shipments, because we store both Shipment event logs and Receipt event logs together under Shipment,
     * the ReferenceDocument that we should use depends on the event log type.
     *
     * A better solution would be to have the Receipt-specific event logs be managed via a Receipt.eventLogs field.
     * Then we could simply get the history items of the shipment and each of its receipts and merge them,
     *
     * TODO: we probably want to be able to reference both the Shipment AND the Receipt, so this should really return
     *       a List<ReferenceDocument>, but we don't have a specific need to do so yet.
     */
    ReferenceDocument getReferenceDocument(Shipment source, EventLog eventLog) {
        // If the log is for a receiving event, find the matching Receipt and use that as the reference
        if (eventLog?.eventCode?.isReceiptEvent()) {
            Receipt receipt = source.receipts.find {
                (it as Receipt).actualDeliveryDate == eventLog?.event?.eventDate
            }
            if (receipt) {
                return receiptHistoryBuilder.getReferenceDocument(receipt)
            }
        }

        // Otherwise, reference the Shipment
        return getReferenceDocument(source)
    }

    /**
     * Use {@link #getReferenceDocument(Shipment, EventLog)} instead as it is more accurate.
     */
    ReferenceDocument getReferenceDocument(Shipment source, Event event) {
        // If this is a receiving event, find the matching Receipt and use that as the reference
        if (event?.eventType?.eventCode?.isReceiptEvent()) {
            Receipt receipt = source.receipts.find { (it as Receipt).actualDeliveryDate == event?.eventDate }
            if (receipt) {
                return receiptHistoryBuilder.getReferenceDocument(receipt)
            }
        }

        // Otherwise, reference the Shipment
        return getReferenceDocument(source)
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

        historyItems.addAll(super.getHistory(source))

        // Unfortunately, because both Shipment and Receipt event logs are referenced together (in Shipment.eventLogs),
        // we need to rebuild the ReferenceDocuments so that receiving history items properly reference the Receipt.
        // This is the only reason why we need to override getHistory here.
        for (HistoryItem historyItem in historyItems) {
            EventLog eventLog = source.eventLogs.find { it?.eventDate == InstantParser.asInstant(historyItem.date) }
            if (eventLog) {
                historyItem.referenceDocument = getReferenceDocument(source, eventLog)
            }
        }
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
        for (Event event in (source.events as SortedSet<Event>)) {
            HistoryItem historyItem = getHistoryItemFromEvent(source, event)

            // Unfortunately, because both Shipment and Receipt event are referenced together (in Shipment.events),
            // we need to rebuild the ReferenceDocuments so that receiving history items properly reference the Receipt.
            historyItem.referenceDocument = getReferenceDocument(source, event)

            histories.add(historyItem)
        }
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
