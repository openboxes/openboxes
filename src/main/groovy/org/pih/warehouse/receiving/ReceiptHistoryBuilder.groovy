package org.pih.warehouse.receiving

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import org.pih.warehouse.core.HistoryItem
import org.pih.warehouse.core.ReferenceDocument
import org.pih.warehouse.core.User
import org.pih.warehouse.core.history.EventLogHistoryBuilder
import org.pih.warehouse.core.history.HistoryBuilder

@Component
class ReceiptHistoryBuilder implements HistoryBuilder<Receipt> {

    @Autowired
    EventLogHistoryBuilder eventLogHistoryBuilder

    @Override
    ReferenceDocument getReferenceDocument(Receipt source) {
        return new ReferenceDocument(
                label: source.receiptNumber,
                url: "/stockMovement/show/${source.shipment?.requisition?.id ?: source.shipment?.id}",
                id: source.id,
                identifier: source.receiptNumber,
        )
    }

    @Override
    List<HistoryItem> getHistory(Receipt source) {
        return [new HistoryItem(
                date: source.actualDeliveryDate,
                location: source.shipment.destination,
                referenceDocument: getReferenceDocument(source),
                createdBy: source.recipient as User,
        )]
    }
}
