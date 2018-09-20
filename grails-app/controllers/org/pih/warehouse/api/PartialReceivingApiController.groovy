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

        log.info "JSON " + jsonObject.toString(4)

        PartialReceipt partialReceipt = receiptService.getPartialReceipt(params.id)

        bindPartialReceiptData(partialReceipt, jsonObject)

        if (partialReceipt.receiptStatus == PartialReceiptStatus.COMPLETED) {
            log.info "Save partial receipt"
            receiptService.savePartialReceipt(partialReceipt)
            receiptService.saveInboundTransaction(partialReceipt)
            partialReceipt = receiptService.getPartialReceipt(params.id)
        }
        else if (partialReceipt.receiptStatus == PartialReceiptStatus.CHECKING) {
            // do nothing for now
        }
        else if (partialReceipt.receiptStatus == PartialReceiptStatus.ROLLBACK) {
            receiptService.rollbackPartialReceipts(partialReceipt.shipment)
            partialReceipt = receiptService.getPartialReceipt(params.id)
        }


        render([data:partialReceipt] as JSON)
    }


    void bindPartialReceiptData(PartialReceipt partialReceipt, JSONObject jsonObject) {

        // Bind the partial receipt
        bindData(partialReceipt, jsonObject)

        jsonObject.containers.each { containerMap ->

            // Bind the container
            PartialReceiptContainer partialReceiptContainer =
                    partialReceipt.findPartialReceiptContainer(containerMap["container.id"])

            if (!partialReceiptContainer) {
                partialReceiptContainer = new PartialReceiptContainer()
                partialReceipt.partialReceiptContainers.add(partialReceiptContainer)
            }
            bindData(partialReceiptContainer, containerMap)

            // Bind the shipment items
            containerMap.shipmentItems.each { shipmentItemMap ->

                // Find item if it exists
                String shipmentItemId = shipmentItemMap.get("shipmentItem.id")
                PartialReceiptItem partialReceiptItem = partialReceiptContainer.partialReceiptItems.find {
                    it?.shipmentItem?.id == shipmentItemId
                }
                // Create new item if not exists
                if (!partialReceiptItem) {
                    partialReceiptItem = new PartialReceiptItem()
                    partialReceiptContainer.partialReceiptItems.add(partialReceiptItem)
                }
                bindData(partialReceiptItem, shipmentItemMap)
            }
        }
    }
}


