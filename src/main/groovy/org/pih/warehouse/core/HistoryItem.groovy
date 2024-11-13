package org.pih.warehouse.core

class HistoryItem<T> {
    String identifier

    Date date

    // FIXME: I didn't know what name suits the best for the location
    Location associatedLocation

    T parentObject

    // FIXME: Introduced this property to be able to determine which history item is pointing to e.g. received event, which one to shipped event etc.
    EventCode eventCode
}
