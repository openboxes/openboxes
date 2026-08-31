package org.pih.warehouse.requisition

import grails.validation.ValidationException
import org.springframework.stereotype.Component

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.EventType
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.event.EventTypeManager

/**
 * Manages the lifecycle of a Requisition related {@link Event}.
 */
@Component
class RequisitionEventManager {

    final RequisitionEventLogger requisitionEventLogger
    final EventTypeManager eventTypeManager

    RequisitionEventManager(final RequisitionEventLogger requisitionEventLogger,
                            final EventTypeManager eventTypeManager) {
        this.requisitionEventLogger = requisitionEventLogger
        this.eventTypeManager = eventTypeManager
    }

    /**
     * Records a requisition status transition. Determines whether this is a new, forward transition (e.g.
     * CREATED -> PICKING) or a rollback of the transition that led to the status being left (e.g. ISSUED ->
     * CHECKING via rollbackRequisition(), CANCELED -> PENDING via undoCancelRequisition(), APPROVED/REJECTED ->
     * PENDING_APPROVAL via rollbackApproval()), and creates the proper Event object for each case - a new Event
     * for a forward transition, or a rollback (delete + EventLog) of the old status's Event for a backward one.
     * This avoids ending up with a confusing mix of both the original and "undone" events for the same status.
     */
    void recordStatusChange(Requisition requisition, RequisitionStatus oldStatus, RequisitionStatus newStatus, Location location) {
        if (isRollback(oldStatus, newStatus)) {
            rollbackEvent(requisition, oldStatus)
            return
        }

        EventCode eventCode = toEventCode(newStatus)
        if (eventCode) {
            createEvent(requisition, eventCode, location)
        }
    }

    /**
     * A transition counts as a rollback when it moves to an earlier point in the requisition lifecycle than
     * where it currently is, rather than progressing forward as usual. There's no earlier point to move back
     * from on the very first status assignment (oldStatus is null), so that's always a forward transition.
     */
    private boolean isRollback(RequisitionStatus oldStatus, RequisitionStatus newStatus) {
        return oldStatus != null && newStatus.sortOrder < oldStatus.sortOrder
    }

    /**
     * The EventCode a requisition status is represented by (a requisition status is represented directly by
     * the EventCode of the same name, the same way shipment statuses are). Returns null for statuses that
     * aren't part of the tracked transition timeline (no matching EventCode, e.g. the legacy/unused RECEIVED,
     * DELETED, ERROR, DISPATCHED, REQUESTED, OPEN, FULFILLED, REVIEWING, CONFIRMING).
     */
    private EventCode toEventCode(RequisitionStatus status) {
        // The only status that doesn't have a one-to-one mapping to an EventCode is CANCELED (singe vs double L)
        if (status == RequisitionStatus.CANCELED) {
            return EventCode.CANCELLED
        }

        return EventCode.values().find { it.name() == status.name() }
    }

    /**
     * The inverse of toEventCode(RequisitionStatus) - the requisition status an Event's EventType represents,
     * so callers can compare two requisition lifecycle positions directly instead of comparing an EventType's
     * sortOrder against a RequisitionStatus's sortOrder (two different things that only happen to share the
     * same numbering by convention). Returns null for an EventType with no matching RequisitionStatus.
     */
    RequisitionStatus toRequisitionStatus(EventType eventType) {
        if (eventType?.eventCode == EventCode.CANCELLED) {
            return RequisitionStatus.CANCELED
        }

        return RequisitionStatus.values().find { it.name() == eventType?.eventCode?.name() }
    }

    /**
     * Create a new Requisition Event representing a status transition, then logs the action.
     */
    Event createEvent(Requisition requisition, EventCode eventCode, Location location) {
        EventType eventType = eventTypeManager.getOrCreateEventType(eventCode)
        Event event = new Event(
                eventDate: new Date(),
                eventType: eventType,
                eventLocation: location,
                createdBy: AuthService.currentUser)

        if (!event.save()) {
            throw new ValidationException("Unable to create requisition event", event.errors)
        }
        requisition.addToEvents(event)

        requisitionEventLogger.logEvent(requisition, event)

        return event
    }

    /**
     * Deletes the Event representing the transition into oldStatus (if one was ever recorded for it), then
     * logs the rollback action.
     */
    private void rollbackEvent(Requisition requisition, RequisitionStatus oldStatus) {
        EventCode eventCode = toEventCode(oldStatus)
        if (!eventCode) {
            return
        }

        Event event = requisition.events?.findAll { it.eventType?.eventCode == eventCode }?.max { it.eventDate }
        if (!event) {
            return
        }

        requisitionEventLogger.logEventRollback(requisition, event)

        requisition.removeFromEvents(event)
        event.delete()
    }
}
