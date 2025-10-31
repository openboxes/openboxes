/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.receiving

import grails.util.Holders
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.Historizable
import org.pih.warehouse.core.HistoryItem
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.ReferenceDocument
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.RefreshOrderSummaryEvent
import org.pih.warehouse.shipping.Shipment
import util.StringUtil

class Receipt implements Serializable, Comparable<Receipt>, Historizable {

    def publishRefreshEvent() {
        Holders.grailsApplication.mainContext.publishEvent(new RefreshOrderSummaryEvent(this))
    }

    def afterInsert() {
        publishRefreshEvent()
    }

    def afterUpdate() {
        publishRefreshEvent()
    }

    String id
    String receiptNumber
    ReceiptStatusCode receiptStatusCode = ReceiptStatusCode.PENDING
    Date expectedDeliveryDate
    Date actualDeliveryDate
    Person recipient
    Date dateCreated
    Date lastUpdated

    Boolean disableRefresh = Boolean.FALSE

    static hasOne = [transaction: Transaction]
    static hasMany = [receiptItems: ReceiptItem]
    static belongsTo = [shipment: Shipment]

    static mapping = {
        id generator: 'uuid'
        receiptItems cascade: "all-delete-orphan"
    }

    static transients = ["disableRefresh"]

    // Constraints
    static constraints = {
        receiptNumber(nullable: true, blank: false)
        receiptStatusCode(nullable: true)
        transaction(nullable: true)
        shipment(nullable: true)
        recipient(nullable: true)
        expectedDeliveryDate(nullable: true)
        actualDeliveryDate(nullable: false,
                validator: { value, obj ->
                    // can't be delivered before it is shipped!
                    Date actualShippingDate = obj?.shipment?.actualShippingDate
                    if (actualShippingDate && !(value + 1).after(actualShippingDate)) {
                        return ["invalid.mustOccurOnOrAfterActualShippingDate", value, actualShippingDate]
                    }
                }
        )
    }

    int compareTo(Receipt otherReceipt) {
        return dateCreated <=> otherReceipt?.dateCreated ?:
                lastUpdated <=> otherReceipt?.lastUpdated ?:
                        id <=> otherReceipt?.id
    }

    List<ReceiptItem> sortReceiptItemsBySortOrder() {
        def receiptItemsComparator = { a, b ->
            return a.shipmentItem?.requisitionItem?.orderIndex <=> b.shipmentItem?.requisitionItem?.orderIndex ?:
                    a.shipmentItem?.sortOrder <=> b.shipmentItem?.sortOrder ?:
                            a?.sortOrder <=> b?.sortOrder ?:
                                    a?.inventoryItem?.product?.name <=>  b?.inventoryItem?.product?.name
        }

        return receiptItems?.sort(receiptItemsComparator)
    }

    @Override
    ReferenceDocument getReferenceDocument() {
        return new ReferenceDocument(
                label: receiptNumber,
                url: "/stockMovement/show/${shipment?.requisition?.id ?: shipment?.id}",
                id: id,
                identifier: receiptNumber,
        )
    }

    @Override
    List<HistoryItem<Receipt>> getHistory() {
        HistoryItem<Receipt> historyItem = new HistoryItem<>(
                date: actualDeliveryDate,
                location: shipment.destination,
                referenceDocument: getReferenceDocument(),
                createdBy: recipient,
        )
        return [historyItem]
    }
}
