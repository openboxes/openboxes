package org.pih.warehouse.core.history

import org.pih.warehouse.core.ReferenceDocument
import org.pih.warehouse.core.Referenceable

/**
 * Constructs a history of actions for some Referenceable (and possibly Historizable) entity.
 */
interface HistoryProvider<T extends Referenceable> {

    /**
     * @return A reference (in a standardized format) to the object being historized.
     */
    ReferenceDocument getReferenceDocument(T source)

    /**
     * Returns a List of HistoryItem representing some historical data on the object being historized. Typically this
     * will be some kind of event/audit log built from a List of {@link EventLog} on the entity, but it is up to the
     * implementing class to determine what qualifies as a history item.
     *
     * Returns a list to allow for a single historizable object to produce multiple history records if needed.
     */
    List<HistoryItem> getHistory(T source)
}
