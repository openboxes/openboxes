package org.pih.warehouse.core.history

/**
 * Constructs a history of actions for some historizable entity.
 */
abstract class HistoryProvider<T extends Historizable> {

    /**
     * Contains the feature-specific logic for building the history fir a given historizable object.
     */
    abstract List<HistoryItem> doGetHistory(T source, HistoryContext context)

    /**
     * Returns a List of HistoryItem representing some historical data on the object being historized. Typically this
     * will be some kind of event/audit log built from a List of {@link EventLog} on the entity, but it is up to the
     * implementing class to determine what qualifies as a history item.
     *
     * Returns a list to allow for a single historizable object to produce multiple history records if needed.
     */
    List<HistoryItem> getHistory(T source, HistoryContext context) {
        List<HistoryItem> historyItems = doGetHistory(source, context).sort()
        return limitHistory(historyItems, context)
    }

    private List<HistoryItem> limitHistory(List<HistoryItem> historyItems, HistoryContext context) {
        // Assumes the history items have been sorted oldest to newest.
        return context.limit ? historyItems.takeRight(context.limit) : historyItems
    }
}
