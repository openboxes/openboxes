package org.pih.warehouse.requisition

import java.time.Instant
import spock.lang.Specification

import org.pih.warehouse.core.ReferenceDocument
import org.pih.warehouse.core.history.EventLog
import org.pih.warehouse.core.history.EventLogCode
import org.pih.warehouse.core.history.HistoryContext
import org.pih.warehouse.core.history.HistoryItem
import org.pih.warehouse.core.localization.MessageLocalizer

class RequisitionHistoryProviderSpec extends Specification {

    RequisitionHistoryProvider requisitionHistoryProvider

    void setup() {
        requisitionHistoryProvider = new RequisitionHistoryProvider()
        requisitionHistoryProvider.messageLocalizer = Stub(MessageLocalizer) {
            // Stub all localization of enums to simply return the enum name
            localizeEnumValue(_ as Enum) >> { Enum enumVal -> return enumVal.name() }
        }
    }

    void "getHistory should build a history item from an ERROR_OCCURRED event log"() {
        given: "A Requisition"
        Requisition requisition = new Requisition(
                requestNumber: "REQ123",
                name: "Name",
                description: "Description",
        )
        requisition.id = "0"

        and: "An expected reference document representing the requisition"
        ReferenceDocument expectedReferenceDocument = new ReferenceDocument(
                label: "REQ123",
                url: "/openboxes/stockMovement/show/0",
                id: "0",
                identifier: "REQ123",
                description: "Description",
                name: "Name",
        )

        and: "An ERROR_OCCURRED event log recording an allocation failure"
        Instant errorDate = Instant.now()
        EventLog errorEventLog = new EventLog(
                event: null,
                eventLogCode: EventLogCode.ERROR_OCCURRED,
                eventDate: errorDate,
                message: "Allocation failed: no available inventory",
        )
        errorEventLog.dateCreated = errorDate

        requisition.eventLogs = [errorEventLog]

        when:
        List<HistoryItem> historyItems = requisitionHistoryProvider.getHistory(requisition, new HistoryContext())

        then:
        historyItems.size() == 1
        HistoryItem historyItem = historyItems[0]
        historyItem.eventType.name == EventLogCode.ERROR_OCCURRED.name()
        historyItem.comment?.comment == "Allocation failed: no available inventory"
        historyItem.referenceDocument.label == expectedReferenceDocument.label
        historyItem.referenceDocument.url == expectedReferenceDocument.url
        historyItem.referenceDocument.id == expectedReferenceDocument.id
        historyItem.referenceDocument.identifier == expectedReferenceDocument.identifier
    }

    void "getHistory should return no history items when there are no event logs"() {
        given:
        Requisition requisition = new Requisition(requestNumber: "REQ123")
        requisition.id = "0"
        requisition.eventLogs = []

        expect:
        requisitionHistoryProvider.getHistory(requisition, new HistoryContext()) == []
    }
}
