package org.pih.warehouse.api.receiving.v2

import grails.converters.JSON

class ReceiptApiController {

    ReceiptV2Service receiptV2Service

    def start() {
        ReceiptDto receipt = receiptV2Service.startReceipt(params.shipmentId)

        response.status = 201
        render([data: receipt] as JSON)
    }
}
