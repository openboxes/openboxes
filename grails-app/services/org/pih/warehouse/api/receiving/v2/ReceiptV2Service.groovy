package org.pih.warehouse.api.receiving.v2

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.session.SessionManager
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptDto
import org.pih.warehouse.receiving.ReceiptIdentifierService
import org.pih.warehouse.receiving.ReceiptService
import org.pih.warehouse.receiving.ReceiptStatusCode
import org.pih.warehouse.shipping.Shipment

@Transactional(readOnly = true)
class ReceiptV2Service {

    ReceiptIdentifierService receiptIdentifierService

    // Inject old receipt service to reuse bin creation logic
    ReceiptService receiptService

    @Transactional
    ReceiptDto startReceipt(String shipmentId) {
        Shipment shipment = Shipment.get(shipmentId)
        if (!shipment) {
            throw new ObjectNotFoundException(shipmentId, Shipment.class.toString())
        }

        boolean hasPendingReceipt = shipment.receipts?.any { it.receiptStatusCode == ReceiptStatusCode.PENDING }
        if (hasPendingReceipt) {
            throw new IllegalStateException("A pending receipt already exists for shipment ${shipment.shipmentNumber}")
        }

        Receipt receipt = new Receipt()
        receipt.receiptNumber = receiptIdentifierService.generate(receipt)
        receipt.receiptStatusCode = ReceiptStatusCode.PENDING
        receipt.recipient = AuthService.currentUser
        receipt.expectedDeliveryDate = shipment.expectedDeliveryDate
        receipt.actualDeliveryDate = shipment.actualDeliveryDate ?: new Date()
        receipt.disableRefresh = true

        receiptService.createTemporaryReceivingBin(shipment)
        shipment.addToReceipts(receipt)

        if (!receipt.save()) {
            throw new ValidationException("Receipt is invalid", receipt.errors)
        }

        return ReceiptDto.toDto(receipt)
    }
}
