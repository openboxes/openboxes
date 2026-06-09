package org.pih.warehouse.core.history

import org.pih.warehouse.core.Comment
import org.pih.warehouse.core.EventTypeDto
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.ReferenceDocument
import org.pih.warehouse.core.User

/**
 * Represents a standardized format to represent the history items of some historizable object.
 *
 * HistoryItem is generic, so it is up to the feature-specific usages of this object to determine what being
 * historizable means. Most commonly, this will be defined via a {@link HistoryProvider}.
 */
class HistoryItem implements Comparable<HistoryItem> {

    /**
     * The system datetime that the history item was logged at.
     * This is typically mapped to the "dateCreated" audit field of the entity being historized.
     */
    Date dateLogged

    /**
     * The real world datetime that the action being historized occurred at.
     * This can differ from dateLogged in cases where we're backdating or logging upcoming events.
     */
    Date date

    Location location

    ReferenceDocument referenceDocument

    EventTypeDto eventType

    Comment comment

    User createdBy

    @Override
    int compareTo(HistoryItem historyItem) {
        return dateLogged <=> historyItem?.dateLogged ?:
               date <=> historyItem?.date ?:
               eventType?.name <=> historyItem?.eventType?.name ?:
               // falling back to the reference document id, to avoid disappearing items
               // in case of the same dates and event codes
               referenceDocument?.id <=> historyItem?.referenceDocument?.id
    }
}
