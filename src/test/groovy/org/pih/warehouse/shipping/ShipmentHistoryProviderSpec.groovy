package org.pih.warehouse.shipping

import java.time.Instant
import spock.lang.Shared
import spock.lang.Specification

import org.pih.warehouse.core.Comment
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.EventType
import org.pih.warehouse.core.history.HistoryContext
import org.pih.warehouse.core.history.HistoryItem
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.ReferenceDocument
import org.pih.warehouse.core.User
import org.pih.warehouse.core.date.InstantParser
import org.pih.warehouse.core.history.EventLog
import org.pih.warehouse.core.history.EventLogCode
import org.pih.warehouse.core.localization.MessageLocalizer
import org.pih.warehouse.receiving.Receipt

class ShipmentHistoryProviderSpec extends Specification {

    @Shared
    ShipmentHistoryProvider shipmentHistoryProvider

    void setup() {
        shipmentHistoryProvider = new ShipmentHistoryProvider()

        shipmentHistoryProvider.messageLocalizer = Stub(MessageLocalizer) {
            // Stub all localization of enums to simply return the enum name
            localizeEnumValue(_ as Enum) >> { Enum enumVal -> return enumVal.name() }
        }
    }

    void "getHistory should work when using event log"() {
        given: "A Shipment"
        Date shipmentCreationDate = new Date()
        Location origin = new Location()
        User shipmentCreator = new User()
        Shipment shipment = new Shipment(
                origin: origin,
                createdBy: shipmentCreator,
                shipmentNumber: "ABC123",
                name: "Name",
                description: "Description",
        )
        shipment.id = "0"
        shipment.dateCreated = shipmentCreationDate

        and: "Receipts for the Shipment"
        Date partialReceiptDate = new Date(shipmentCreationDate.getTime() + 1000)
        Receipt partialReceipt = new Receipt(
                shipment: shipment,
                receiptNumber: "R0",
                actualDeliveryDate: partialReceiptDate,
        )
        partialReceipt.id = "0"

        Date finalReceiptDate = new Date(partialReceiptDate.getTime() + 1000)
        Receipt finalReceipt = new Receipt(
                shipment: shipment,
                receiptNumber: "R1",
                actualDeliveryDate: finalReceiptDate,
        )
        finalReceipt.id = "1"

        shipment.receipts = new TreeSet<Receipt>([partialReceipt, finalReceipt])

        and: "Events for the Shipment"
        Event shippedEvent = new Event(
                eventType: new EventType(
                        name: EventCode.SHIPPED.name(),
                        eventCode: EventCode.SHIPPED,
                ),
                eventDate: shipmentCreationDate,
                eventLocation: origin,
                comment: new Comment(comment: "Sending shipment"),
                createdBy: shipmentCreator,
        )
        shippedEvent.dateCreated = shipmentCreationDate

        Event partialReceivedEvent = new Event(
                eventType: new EventType(
                        name: EventCode.PARTIALLY_RECEIVED.name(),
                        eventCode: EventCode.PARTIALLY_RECEIVED,
                ),
                eventDate: partialReceiptDate,
                eventLocation: origin,
                comment: new Comment(comment: "Partial receiving shipment"),
                createdBy: shipmentCreator,
        )
        partialReceivedEvent.dateCreated = partialReceiptDate

        Event finalReceivedEvent = new Event(
                eventType: new EventType(
                        name: EventCode.RECEIVED.name(),
                        eventCode: EventCode.RECEIVED,
                ),
                eventDate: finalReceiptDate,
                eventLocation: origin,
                comment: new Comment(comment: "Final receiving shipment"),
                createdBy: shipmentCreator,
        )
        finalReceivedEvent.dateCreated = finalReceiptDate

        shipment.events = new TreeSet<Event>([shippedEvent, partialReceivedEvent, finalReceivedEvent])

        and: "An expected reference document representing the shipment"
        ReferenceDocument expectedReferenceDocument = new ReferenceDocument(
                label: "ABC123",
                url: "/openboxes/stockMovement/show/0",
                id: 0,
                identifier: "ABC123",
                description: "Description",
                name: "Name",
        )

        and: "EventLogs for each of the Events"
        Instant shipmentCreationLogDate = InstantParser.asInstant(shipmentCreationDate)
        EventLog shippedEventLog = new EventLog(
                event: shippedEvent,
                eventCode: EventCode.SHIPPED,
                eventDate: shipmentCreationLogDate,
                eventLogCode: EventLogCode.EVENT_OCCURRED,
                message: "Event Log - Sending shipment",
                location: origin,
                createdBy: shipmentCreator,
        )
        shippedEventLog.dateCreated = shipmentCreationLogDate

        Instant partialReceiptLogDate = InstantParser.asInstant(partialReceiptDate)
        EventLog partialReceivedEventLog = new EventLog(
                event: partialReceivedEvent,
                eventCode: EventCode.PARTIALLY_RECEIVED,
                eventDate: partialReceiptLogDate,
                eventLogCode: EventLogCode.EVENT_OCCURRED,
                message: "Event Log - Partial receiving shipment",
                location: origin,
                createdBy: shipmentCreator,
        )
        partialReceivedEventLog.dateCreated = partialReceiptLogDate

        Instant finalReceiptLogDate = InstantParser.asInstant(finalReceiptDate)
        EventLog receivedEventLog = new EventLog(
                event: finalReceivedEvent,
                eventCode: EventCode.RECEIVED,
                eventDate: finalReceiptLogDate,
                eventLogCode: EventLogCode.EVENT_OCCURRED,
                message: "Event Log - Final receiving shipment",
                location: origin,
                createdBy: shipmentCreator,
        )
        receivedEventLog.dateCreated = finalReceiptLogDate

        shipment.eventLogs = new TreeSet<EventLog>([shippedEventLog, partialReceivedEventLog, receivedEventLog])

        when:
        List<HistoryItem> historyItems = shipmentHistoryProvider.getHistory(shipment, new HistoryContext())

        then:
        assert historyItems.size() == 4

        assertHistoryItem(historyItems[0],  // The created event - based on Shipment
                shipmentCreationDate,
                origin,
                null,
                shipmentCreator,
                EventCode.CREATED.name(),
                EventCode.CREATED,
                expectedReferenceDocument)

        assertHistoryItem(historyItems[1],  // The shipped event - based on Shipment
                shipmentCreationDate,
                origin,
                "Event Log - Sending shipment",
                shipmentCreator,
                EventCode.SHIPPED.name(),
                EventCode.SHIPPED,
                expectedReferenceDocument)

        assertHistoryItem(historyItems[2],  // The partial receipt - based on Receipt
                partialReceiptDate,
                origin,
                "Event Log - Partial receiving shipment",
                shipmentCreator,
                EventCode.PARTIALLY_RECEIVED.name(),
                EventCode.PARTIALLY_RECEIVED,
                expectedReferenceDocument)

        assertHistoryItem(historyItems[3],  // The final receipt - based on Receipt
                finalReceiptDate,
                origin,
                "Event Log - Final receiving shipment",
                shipmentCreator,
                EventCode.RECEIVED.name(),
                EventCode.RECEIVED,
                expectedReferenceDocument)
    }

    void "getHistory should work when using event log when rollbacks have happened"() {
        given: "A Shipment (which is rolled back)"
        Date shipmentCreationDate = new Date()
        Location origin = new Location()
        User shipmentCreator = new User()
        Shipment shipment = new Shipment(
                origin: origin,
                createdBy: shipmentCreator,
                shipmentNumber: "ABC123",
                name: "Name",
                description: "Description",
        )
        shipment.id = "0"
        shipment.dateCreated = shipmentCreationDate

        and: "An expected reference document representing the shipment"
        ReferenceDocument expectedReferenceDocument = new ReferenceDocument(
                label: "ABC123",
                url: "/openboxes/stockMovement/show/0",
                id: 0,
                identifier: "ABC123",
                description: "Description",
                name: "Name",
        )

        and: "EventLogs for each of the Events and their rollbacks"
        Instant shipmentCreationLogDate = InstantParser.asInstant(shipmentCreationDate)
        EventLog shippedEventLog = new EventLog(
                event: null,  // rolled back so no longer exists
                eventCode: EventCode.SHIPPED,
                eventDate: shipmentCreationLogDate,
                eventLogCode: EventLogCode.EVENT_OCCURRED,
                message: "Event Log - Sending shipment",
                location: origin,
                createdBy: shipmentCreator,
        )
        shippedEventLog.dateCreated = shipmentCreationLogDate

        Date partialReceiptDate = new Date(shipmentCreationDate.getTime() + 1000)
        Instant partialReceiptLogDate = InstantParser.asInstant(partialReceiptDate)
        EventLog partialReceivedEventLog = new EventLog(
                event: null,  // rolled back so no longer exists
                eventCode: EventCode.PARTIALLY_RECEIVED,
                eventDate: partialReceiptLogDate,
                eventLogCode: EventLogCode.EVENT_OCCURRED,
                message: "Event Log - Partial receiving shipment",
                location: origin,
                createdBy: shipmentCreator,
        )
        partialReceivedEventLog.dateCreated = partialReceiptLogDate

        Date finalReceiptDate = new Date(partialReceiptDate.getTime() + 1000)
        Instant finalReceiptLogDate = InstantParser.asInstant(finalReceiptDate)
        EventLog receivedEventLog = new EventLog(
                event: null,  // rolled back so no longer exists
                eventCode: EventCode.RECEIVED,
                eventDate: finalReceiptLogDate,
                eventLogCode: EventLogCode.EVENT_OCCURRED,
                message: "Event Log - Final receiving shipment",
                location: origin,
                createdBy: shipmentCreator,
        )
        receivedEventLog.dateCreated = finalReceiptLogDate

        Date finalReceiptRollbackDate = new Date(finalReceiptDate.getTime() + 1000)
        Instant finalReceiptRollbackLogDate = InstantParser.asInstant(finalReceiptRollbackDate)
        EventLog receivedRollbackEventLog = new EventLog(
                event: null,  // rolled back so no longer exists
                eventCode: EventCode.RECEIVED,
                eventDate: finalReceiptRollbackLogDate,
                eventLogCode: EventLogCode.EVENT_ROLLBACK_OCCURRED,
                message: "Event Log - Rollback - Final receiving shipment",
                location: origin,
                createdBy: shipmentCreator,
        )
        receivedRollbackEventLog.dateCreated = finalReceiptRollbackLogDate

        Date partialReceiptRollbackDate = new Date(finalReceiptRollbackDate.getTime() + 1000)
        Instant partialReceiptRollbackLogDate = InstantParser.asInstant(partialReceiptRollbackDate)
        EventLog partialReceivedRollbackEventLog = new EventLog(
                event: null,  // rolled back so no longer exists
                eventCode: EventCode.PARTIALLY_RECEIVED,
                eventDate: partialReceiptRollbackLogDate,
                eventLogCode: EventLogCode.EVENT_ROLLBACK_OCCURRED,
                message: "Event Log - Rollback - Partial receiving shipment",
                location: origin,
                createdBy: shipmentCreator,
        )
        partialReceivedRollbackEventLog.dateCreated = partialReceiptRollbackLogDate

        Date shipmentCreationRollbackDate = new Date(partialReceiptRollbackDate.getTime() + 1000)
        Instant shipmentCreationRollbackLogDate = InstantParser.asInstant(shipmentCreationRollbackDate)
        EventLog shippedRollbackEventLog = new EventLog(
                event: null,  // rolled back so no longer exists
                eventCode: EventCode.SHIPPED,
                eventDate: shipmentCreationRollbackLogDate,
                eventLogCode: EventLogCode.EVENT_ROLLBACK_OCCURRED,
                message: "Event Log - Rollback - Sending shipment",
                location: origin,
                createdBy: shipmentCreator,
        )
        shippedRollbackEventLog.dateCreated = shipmentCreationRollbackLogDate

        shipment.eventLogs = new TreeSet<EventLog>([
                shippedEventLog,
                partialReceivedEventLog,
                receivedEventLog,
                receivedRollbackEventLog,
                partialReceivedRollbackEventLog,
                shippedRollbackEventLog,
        ])

        when:
        List<HistoryItem> historyItems = shipmentHistoryProvider.getHistory(shipment, new HistoryContext())

        then:
        assert historyItems.size() == 7

        assertHistoryItem(historyItems[0],  // The created event - based on Shipment
                shipmentCreationDate,
                origin,
                null,
                shipmentCreator,
                EventCode.CREATED.name(),
                EventCode.CREATED,
                expectedReferenceDocument)

        assertHistoryItem(historyItems[1],  // The shipped event - based on Shipment
                shipmentCreationDate,
                origin,
                "Event Log - Sending shipment",
                shipmentCreator,
                EventCode.SHIPPED.name(),
                EventCode.SHIPPED,
                expectedReferenceDocument)

        assertHistoryItem(historyItems[2],  // The partial receipt - based on Receipt
                partialReceiptDate,
                origin,
                "Event Log - Partial receiving shipment",
                shipmentCreator,
                EventCode.PARTIALLY_RECEIVED.name(),
                EventCode.PARTIALLY_RECEIVED,
                expectedReferenceDocument)

        assertHistoryItem(historyItems[3],  // The final receipt - based on Receipt
                finalReceiptDate,
                origin,
                "Event Log - Final receiving shipment",
                shipmentCreator,
                EventCode.RECEIVED.name(),
                EventCode.RECEIVED,
                expectedReferenceDocument)

        assertHistoryItem(historyItems[4],  // The final receipt rollback
                finalReceiptRollbackDate,
                origin,
                "Event Log - Rollback - Final receiving shipment",
                shipmentCreator,
                "${EventCode.RECEIVED.name()} - Rollback",
                EventCode.RECEIVED,
                expectedReferenceDocument)

        assertHistoryItem(historyItems[5],  // The partial receipt rollback
                partialReceiptRollbackDate,
                origin,
                "Event Log - Rollback - Partial receiving shipment",
                shipmentCreator,
                "${EventCode.PARTIALLY_RECEIVED.name()} - Rollback",
                EventCode.PARTIALLY_RECEIVED,
                expectedReferenceDocument)

        assertHistoryItem(historyItems[6],  // The shipment rollback
                shipmentCreationRollbackDate,
                origin,
                "Event Log - Rollback - Sending shipment",
                shipmentCreator,
                "${EventCode.SHIPPED.name()} - Rollback",
                EventCode.SHIPPED,
                expectedReferenceDocument)
    }

    void "getHistory should work for data from before event log"() {
        given: "A Shipment"
        Date shipmentCreationDate = new Date()
        Location origin = new Location()
        User shipmentCreator = new User()
        Shipment shipment = new Shipment(
                origin: origin,
                createdBy: shipmentCreator,
                shipmentNumber: "ABC123",
                name: "Name",
                description: "Description",
        )
        shipment.id = "0"
        shipment.dateCreated = shipmentCreationDate

        and: "Receipts for the Shipment"
        Date partialReceiptDate = new Date(shipmentCreationDate.getTime() + 1000)
        Receipt partialReceipt = new Receipt(
                shipment: shipment,
                receiptNumber: "R0",
                actualDeliveryDate: partialReceiptDate,
        )
        partialReceipt.id = "0"

        Date finalReceiptDate = new Date(partialReceiptDate.getTime() + 1000)
        Receipt finalReceipt = new Receipt(
                shipment: shipment,
                receiptNumber: "R1",
                actualDeliveryDate: finalReceiptDate,
        )
        finalReceipt.id = "1"

        shipment.receipts = new TreeSet<Receipt>([partialReceipt, finalReceipt])

        and: "Events for the Shipment"
        Event shippedEvent = new Event(
                eventType: new EventType(
                        name: EventCode.SHIPPED.name(),
                        eventCode: EventCode.SHIPPED,
                ),
                eventDate: shipmentCreationDate,
                eventLocation: origin,
                comment: new Comment(comment: "Sending shipment"),
                createdBy: shipmentCreator,
        )
        shippedEvent.dateCreated = shipmentCreationDate

        Event partialReceivedEvent = new Event(
                eventType: new EventType(
                        name: EventCode.PARTIALLY_RECEIVED.name(),
                        eventCode: EventCode.PARTIALLY_RECEIVED,
                ),
                eventDate: partialReceiptDate,
                eventLocation: origin,
                comment: new Comment(comment: "Partial receiving shipment"),
                createdBy: shipmentCreator,
        )
        partialReceivedEvent.dateCreated = partialReceiptDate

        Event receivedEvent = new Event(
                eventType: new EventType(
                        name: EventCode.RECEIVED.name(),
                        eventCode: EventCode.RECEIVED,
                ),
                eventDate: finalReceiptDate,
                eventLocation: origin,
                comment: new Comment(comment: "Final receiving shipment"),
                createdBy: shipmentCreator,
        )
        receivedEvent.dateCreated = finalReceiptDate

        shipment.events = new TreeSet<Event>([shippedEvent, partialReceivedEvent, receivedEvent])

        and: "An expected reference document representing the shipment"
        ReferenceDocument expectedReferenceDocument = new ReferenceDocument(
                label: "ABC123",
                url: "/openboxes/stockMovement/show/0",
                id: 0,
                identifier: "ABC123",
                description: "Description",
                name: "Name",
        )

        when:
        List<HistoryItem> historyItems = shipmentHistoryProvider.getHistory(shipment, new HistoryContext())

        then:
        assert historyItems.size() == 4

        assertHistoryItem(historyItems[0],  // The created event - based on Shipment
                shipmentCreationDate,
                origin,
                null,
                shipmentCreator,
                EventCode.CREATED.name(),
                EventCode.CREATED,
                expectedReferenceDocument)

        assertHistoryItem(historyItems[1],  // The shipped event - based on Shipment
                shipmentCreationDate,
                origin,
                "Sending shipment",
                shipmentCreator,
                EventCode.SHIPPED.name(),
                EventCode.SHIPPED,
                expectedReferenceDocument)

        assertHistoryItem(historyItems[2],  // The partial receipt - based on Receipt
                partialReceiptDate,
                origin,
                "Partial receiving shipment",
                shipmentCreator,
                EventCode.PARTIALLY_RECEIVED.name(),
                EventCode.PARTIALLY_RECEIVED,
                expectedReferenceDocument)

        assertHistoryItem(historyItems[3],  // The final receipt - based on Receipt
                finalReceiptDate,
                origin,
                "Final receiving shipment",
                shipmentCreator,
                EventCode.RECEIVED.name(),
                EventCode.RECEIVED,
                expectedReferenceDocument)
    }

    void "getHistory should work when using event log and rollbacks are ignored"() {
        given: "A Shipment (which is rolled back)"
        Date shipmentCreationDate = new Date()
        Location origin = new Location()
        User shipmentCreator = new User()
        Shipment shipment = new Shipment(
                origin: origin,
                createdBy: shipmentCreator,
                shipmentNumber: "ABC123",
                name: "Name",
                description: "Description",
        )
        shipment.id = "0"
        shipment.dateCreated = shipmentCreationDate

        and: "An expected reference document representing the shipment"
        ReferenceDocument expectedReferenceDocument = new ReferenceDocument(
                label: "ABC123",
                url: "/openboxes/stockMovement/show/0",
                id: 0,
                identifier: "ABC123",
                description: "Description",
                name: "Name",
        )

        and: "EventLogs for each of the Events and their rollbacks"
        Instant shipmentCreationLogDate = InstantParser.asInstant(shipmentCreationDate)
        EventLog shippedEventLog = new EventLog(
                event: null,  // rolled back so no longer exists
                eventCode: EventCode.SHIPPED,
                eventDate: shipmentCreationLogDate,
                eventLogCode: EventLogCode.EVENT_OCCURRED,
                message: "Event Log - Sending shipment",
                location: origin,
                createdBy: shipmentCreator,
        )
        shippedEventLog.dateCreated = shipmentCreationLogDate

        Date partialReceiptDate = new Date(shipmentCreationDate.getTime() + 1000)
        Instant partialReceiptLogDate = InstantParser.asInstant(partialReceiptDate)
        EventLog partialReceivedEventLog = new EventLog(
                event: null,  // rolled back so no longer exists
                eventCode: EventCode.PARTIALLY_RECEIVED,
                eventDate: partialReceiptLogDate,
                eventLogCode: EventLogCode.EVENT_OCCURRED,
                message: "Event Log - Partial receiving shipment",
                location: origin,
                createdBy: shipmentCreator,
        )
        partialReceivedEventLog.dateCreated = partialReceiptLogDate

        Date finalReceiptDate = new Date(partialReceiptDate.getTime() + 1000)
        Instant finalReceiptLogDate = InstantParser.asInstant(finalReceiptDate)
        EventLog receivedEventLog = new EventLog(
                event: null,  // rolled back so no longer exists
                eventCode: EventCode.RECEIVED,
                eventDate: finalReceiptLogDate,
                eventLogCode: EventLogCode.EVENT_OCCURRED,
                message: "Event Log - Final receiving shipment",
                location: origin,
                createdBy: shipmentCreator,
        )
        receivedEventLog.dateCreated = finalReceiptLogDate

        Date finalReceiptRollbackDate = new Date(finalReceiptDate.getTime() + 1000)
        Instant finalReceiptRollbackLogDate = InstantParser.asInstant(finalReceiptRollbackDate)
        EventLog receivedRollbackEventLog = new EventLog(
                event: null,  // rolled back so no longer exists
                eventCode: EventCode.RECEIVED,
                eventDate: finalReceiptRollbackLogDate,
                eventLogCode: EventLogCode.EVENT_ROLLBACK_OCCURRED,
                message: "Event Log - Rollback - Final receiving shipment",
                location: origin,
                createdBy: shipmentCreator,
        )
        receivedRollbackEventLog.dateCreated = finalReceiptRollbackLogDate

        Date partialReceiptRollbackDate = new Date(finalReceiptRollbackDate.getTime() + 1000)
        Instant partialReceiptRollbackLogDate = InstantParser.asInstant(partialReceiptRollbackDate)
        EventLog partialReceivedRollbackEventLog = new EventLog(
                event: null,  // rolled back so no longer exists
                eventCode: EventCode.PARTIALLY_RECEIVED,
                eventDate: partialReceiptRollbackLogDate,
                eventLogCode: EventLogCode.EVENT_ROLLBACK_OCCURRED,
                message: "Event Log - Rollback - Partial receiving shipment",
                location: origin,
                createdBy: shipmentCreator,
        )
        partialReceivedRollbackEventLog.dateCreated = partialReceiptRollbackLogDate

        Date shipmentCreationRollbackDate = new Date(partialReceiptRollbackDate.getTime() + 1000)
        Instant shipmentCreationRollbackLogDate = InstantParser.asInstant(shipmentCreationRollbackDate)
        EventLog shippedRollbackEventLog = new EventLog(
                event: null,  // rolled back so no longer exists
                eventCode: EventCode.SHIPPED,
                eventDate: shipmentCreationRollbackLogDate,
                eventLogCode: EventLogCode.EVENT_ROLLBACK_OCCURRED,
                message: "Event Log - Rollback - Sending shipment",
                location: origin,
                createdBy: shipmentCreator,
        )
        shippedRollbackEventLog.dateCreated = shipmentCreationRollbackLogDate

        shipment.eventLogs = new TreeSet<EventLog>([
                shippedEventLog,
                partialReceivedEventLog,
                receivedEventLog,
                receivedRollbackEventLog,
                partialReceivedRollbackEventLog,
                shippedRollbackEventLog,
        ])

        when: "we fetch history, ignoring rollbacks"
        List<HistoryItem> historyItems = shipmentHistoryProvider.getHistory(shipment, new HistoryContext(
                includeRolledBackEvents: false,
        ))

        then: "all rolled back events are filtered out from the history"
        assert historyItems.size() == 1

        assertHistoryItem(historyItems[0],  // The created event - based on Shipment
                shipmentCreationDate,
                origin,
                null,
                shipmentCreator,
                EventCode.CREATED.name(),
                EventCode.CREATED,
                expectedReferenceDocument)
    }

    private void assertHistoryItem(HistoryItem historyItem,
                                   Date expectedDate,
                                   Location expectedLocation,
                                   String expectedComment,
                                   User expectedCreatedBy,
                                   String expectedEventName,
                                   EventCode expectedEventCode,
                                   ReferenceDocument expectedReferenceDocument) {
        assert historyItem != null
        assert historyItem.date == expectedDate
        assert historyItem.location == expectedLocation
        assert historyItem.comment?.comment == expectedComment
        assert historyItem.createdBy == expectedCreatedBy

        assert historyItem.eventType.name == expectedEventName
        assert historyItem.eventType.eventCode == expectedEventCode

        assert historyItem.referenceDocument.label == expectedReferenceDocument.label
        assert historyItem.referenceDocument.url == expectedReferenceDocument.url
        assert historyItem.referenceDocument.id == expectedReferenceDocument.id
        assert historyItem.referenceDocument.identifier == expectedReferenceDocument.identifier
    }
}
