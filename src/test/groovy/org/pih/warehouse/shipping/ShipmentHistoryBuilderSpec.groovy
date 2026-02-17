package org.pih.warehouse.shipping

import spock.lang.Shared
import spock.lang.Specification

import org.pih.warehouse.core.Comment
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.EventType
import org.pih.warehouse.core.HistoryItem
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.ReferenceDocument
import org.pih.warehouse.core.User
import org.pih.warehouse.core.localization.MessageLocalizer
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptHistoryBuilder

class ShipmentHistoryBuilderSpec extends Specification {

    @Shared
    ShipmentHistoryBuilder shipmentHistoryBuilder

    void setup() {
        shipmentHistoryBuilder = new ShipmentHistoryBuilder()

        shipmentHistoryBuilder.receiptHistoryBuilder = Stub(ReceiptHistoryBuilder) {
            getReferenceDocument(_ as Receipt) >> { Receipt r -> new ReferenceDocument(identifier: r.receiptNumber) }
        }

        shipmentHistoryBuilder.messageLocalizer = Stub(MessageLocalizer) {
            localizeEnumValue(_ as Enum) >> { Enum enumVal -> return enumVal.name() }
        }
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
        shipment.events = new TreeSet<Event>([
                new Event(
                        eventType: new EventType(
                                name: EventCode.SHIPPED.name(),
                                eventCode: EventCode.SHIPPED,
                        ),
                        eventDate: shipmentCreationDate,
                        eventLocation: origin,
                        comment: new Comment(comment: "Sending shipment"),
                        createdBy: shipmentCreator,
                ),
                new Event(
                        eventType: new EventType(
                                name: EventCode.PARTIALLY_RECEIVED.name(),
                                eventCode: EventCode.PARTIALLY_RECEIVED,
                        ),
                        eventDate: partialReceiptDate,
                        eventLocation: origin,
                        comment: new Comment(comment: "Partial receiving shipment"),
                        createdBy: shipmentCreator,
                ),
                new Event(
                        eventType: new EventType(
                                name: EventCode.RECEIVED.name(),
                                eventCode: EventCode.RECEIVED,
                        ),
                        eventDate: finalReceiptDate,
                        eventLocation: origin,
                        comment: new Comment(comment: "Final receiving shipment"),
                        createdBy: shipmentCreator,
                ),
        ])

        when:
        List<HistoryItem> historyItems = shipmentHistoryBuilder.getHistory(shipment)

        then:
        assert historyItems.size() == 4

        assertHistoryItem(historyItems[0],  // The created event - based on Shipment
                shipmentCreationDate,
                origin,
                null,
                shipmentCreator,
                EventCode.CREATED,
                new ReferenceDocument(
                        label: "ABC123",
                        url: "/stockMovement/show/0",
                        id: 0,
                        identifier: "ABC123",
                        description: "Description",
                        name: "Name",
                ))

        assertHistoryItem(historyItems[1],  // The shipped event - based on Shipment
                shipmentCreationDate,
                origin,
                "Sending shipment",
                shipmentCreator,
                EventCode.SHIPPED,
                new ReferenceDocument(
                        label: "ABC123",
                        url: "/stockMovement/show/0",
                        id: 0,
                        identifier: "ABC123",
                        description: "Description",
                        name: "Name",
                ))

        assertHistoryItem(historyItems[2],  // The partial receipt - based on Receipt
                partialReceiptDate,
                origin,
                "Partial receiving shipment",
                shipmentCreator,
                EventCode.PARTIALLY_RECEIVED,
                new ReferenceDocument(identifier: "R0"))  // This is a stub for receipts. No other fields are set.

        assertHistoryItem(historyItems[3],  // The final receipt - based on Receipt
                finalReceiptDate,
                origin,
                "Final receiving shipment",
                shipmentCreator,
                EventCode.RECEIVED,
                new ReferenceDocument(identifier: "R1"))  // This is a stub for receipts. No other fields are set.
    }

    private void assertHistoryItem(HistoryItem historyItem,
                                   Date expectedDate,
                                   Location expectedLocation,
                                   String expectedComment,
                                   User expectedCreatedBy,
                                   EventCode expectedEventCode,
                                   ReferenceDocument expectedReferenceDocument) {
        assert historyItem != null
        assert historyItem.date == expectedDate
        assert historyItem.location == expectedLocation
        assert historyItem.comment?.comment == expectedComment
        assert historyItem.createdBy == expectedCreatedBy

        assert historyItem.eventType.name == expectedEventCode.name()  // In setup we stub the l10nized name to be this
        assert historyItem.eventType.eventCode == expectedEventCode

        assert historyItem.referenceDocument.label == expectedReferenceDocument.label
        assert historyItem.referenceDocument.url == expectedReferenceDocument.url
        assert historyItem.referenceDocument.id == expectedReferenceDocument.id
        assert historyItem.referenceDocument.identifier == expectedReferenceDocument.identifier
    }
}
