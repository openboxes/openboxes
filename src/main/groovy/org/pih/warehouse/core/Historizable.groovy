package org.pih.warehouse.core

/**
 * The ability for some entity to produce one or more entries of a report on historical data.
 *
 * While not strictly required, this interface will often be used by entities in conjunction with:
 * - a List of {@link EventLog} to maintain an event/audit log of all relevant changes
 * - a List of {@link Event} to maintain a history of state
 */
interface Historizable {

    /**
     * Returns a List of HistoryItem representing some historical data on the entity. Typically this will be some
     * kind of event/audit log built from a List of {@link EventLog} on the entity, but it is up to the implementing
     * entity to determine what qualifies as a history item.
     *
     * Returns a list to allow for a single historizable object to produce multiple history records if needed.
     */
    List<HistoryItem> getHistory()

    /**
     * Returns a ReferenceDocument POJO representing a reference to the source object responsible for the history.
     */
    ReferenceDocument getReferenceDocument()
}
