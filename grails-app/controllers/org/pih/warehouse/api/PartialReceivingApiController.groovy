/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.api

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

import java.text.DateFormat
import java.text.SimpleDateFormat

class PartialReceivingApiController {

    def receiptService
    def shipmentService


    def list = {
        render ([data: []] as JSON)
    }

    def read = {
        PartialReceipt partialReceipt = receiptService.getPartialReceipt(params.id)
        render([data:partialReceipt] as JSON)
    }

    def update = {

        JSONObject jsonObject = request.JSON

        PartialReceipt partialReceipt = receiptService.getPartialReceipt(params.id)

        bindPartialReceiptData(partialReceipt, jsonObject)

        if (partialReceipt.receiptStatus == PartialReceiptStatus.COMPLETE) {
            log.info "Save partial receipt"
            receiptService.createPartialReceipt(partialReceipt)
            partialReceipt = receiptService.getPartialReceipt(params.id)
        }
        else if (partialReceipt.receiptStatus == PartialReceiptStatus.ROLLBACK) {
            receiptService.rollbackPartialReceipts(partialReceipt.shipment)
            partialReceipt = receiptService.getPartialReceipt(params.id)
        }

        render([data:partialReceipt] as JSON)
    }


    void bindPartialReceiptData(PartialReceipt partialReceipt, JSONObject jsonObject) {

        log.info "Binding data " + jsonObject

        // Bind the partial receipt
        bindData(partialReceipt, jsonObject)

        log.info "status " + partialReceipt.receiptStatus

        // Bind the partial receipt items
        jsonObject.partialReceiptItems.each { partialReceiptItemMap ->

            PartialReceiptItem partialReceiptItem
            if (!partialReceiptItem) {
                partialReceiptItem = new PartialReceiptItem()
            }
            bindData(partialReceiptItem, partialReceiptItemMap)
            partialReceipt.partialReceiptItems.add(partialReceiptItem)
        }
    }



}


