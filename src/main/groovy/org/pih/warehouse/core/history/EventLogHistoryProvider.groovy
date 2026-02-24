package org.pih.warehouse.core.history

import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired

import org.pih.warehouse.core.Comment
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventType
import org.pih.warehouse.core.EventTypeDto
import org.pih.warehouse.core.Referenceable
import org.pih.warehouse.core.date.JavaUtilDateParser
import org.pih.warehouse.core.localization.MessageLocalizer

/**
 * A convenience base class for any HistoryProvider that operates on entities whose history is based off
 * of a collection of {@link EventLog}.
 */
abstract class EventLogHistoryProvider<T extends Referenceable> implements HistoryProvider<T> {

    @Autowired
    MessageLocalizer messageLocalizer

    /**
     * @return the EventLogs associated with the document that we're generating the history for.
     */
    abstract Collection<EventLog> getEventLogs(T source)

    @Override
    List<HistoryItem> getHistory(T source) {
        // The default behaviour assumes the event logs contain all the information that we need to build the history.
        Collection<EventLog> eventLogs = getEventLogs(source)
        List<HistoryItem> historyItems = []
        for (EventLog eventLog in eventLogs) {
            HistoryItem historyItem = getHistoryItem(source, eventLog)
            historyItems.add(historyItem)
        }
        return historyItems.sort()
    }

    /**
     * Creates a List of HistoryItem from a given Event collection and source object.
     */
    protected List<HistoryItem> getHistoryItemsFromEvents(T source, Collection<Event> events) {
        List<HistoryItem> historyItems = []
        for (Event event in events) {
            HistoryItem historyItem = getHistoryItemFromEvent(source, event)
            historyItems.add(historyItem)
        }
        return historyItems.findAll()
    }

    /**
     * Creates a HistoryItem from a given Event and source object.
     */
    protected HistoryItem getHistoryItemFromEvent(T source, Event event) {
        if (!event) {
            return null
        }

        EventType eventType = event.eventType
        return new HistoryItem(
                dateLogged: event.dateCreated,
                date: event.eventDate,
                location: event.eventLocation,
                eventType: new EventTypeDto(
                        name: eventType ? messageLocalizer.localizeEnumValue(eventType.eventCode) : null,
                        eventCode: eventType?.eventCode,
                ),
                comment: event.comment,
                createdBy: event.createdBy,
                referenceDocument: getReferenceDocument(source),
        )
    }

    private HistoryItem getHistoryItem(T source, EventLog eventLog) {
        // For event-based logs, we let the Event determine the contents of the history item.
        // We then enhance the history by adding extra data from the EventLog
        if (eventLog.event) {
            HistoryItem historyItem = getHistoryItemFromEvent(source, eventLog.event)
            if (StringUtils.isNotBlank(eventLog.message)) {
                historyItem.comment = new Comment(comment: eventLog.message)
            }
            return historyItem
        }

        // For non-event-based logs and Event rollbacks, we build the history item ourselves.
        Enum eventName = eventLog.eventCode ?: eventLog.eventLogCode
        boolean isRollback = eventLog.eventLogCode == EventLogCode.EVENT_ROLLBACK_OCCURRED
        String eventNameString = "${messageLocalizer.localizeEnumValue(eventName)}${isRollback ? " - Rollback" : ""}"

        return new HistoryItem(
                dateLogged: JavaUtilDateParser.asDate(eventLog.dateCreated),
                date: JavaUtilDateParser.asDate(eventLog.dateCreated),
                location: eventLog.location,
                eventType: new EventTypeDto(
                        name: eventNameString,
                        eventCode: eventLog.eventCode,
                ),
                comment: StringUtils.isBlank(eventLog.message) ? null : new Comment(comment: eventLog.message),
                createdBy: eventLog.createdBy,
                referenceDocument: getReferenceDocument(source),
        )
    }
}
