package org.pih.warehouse.core

interface Historizable {
    List<HistoryItem> getHistory()

    ReferenceDocument getReferenceDocument()
}
