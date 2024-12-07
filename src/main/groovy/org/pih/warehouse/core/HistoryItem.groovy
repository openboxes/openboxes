package org.pih.warehouse.core

class HistoryItem<T> implements Comparable {
    Date date

    Location location

    ReferenceDocument referenceDocument

    EventCode eventCode

    String eventTypeName

    Comment comment

    User createdBy

    @Override
    int compareTo(Object o) {
        // Events with event code created should be placed
        // at the top of the lists as a first created event
        if (o?.eventCode == EventCode.CREATED) {
            return 1
        }

        if (eventCode == EventCode.CREATED) {
            return -1
        }

        // falling back to the reference document id, to avoid disappearing items
        // in case of the same dates and event codes
        return date <=> o?.date ?:
               eventTypeName <=> o?.eventTypeName ?:
               referenceDocument?.id <=> o?.referenceDocument?.id
    }
}
