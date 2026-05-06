package org.pih.warehouse.core.history

class HistoryContext {
    /**
     * The number of history items to return. Setting a value of null will return the full history.
     */
    Integer limit

    /**
     * True if the history should include rollback events and events that have been rolled back.
     * False if they should be filtered out.
     */
    Boolean includeRolledBackEvents = true
}
