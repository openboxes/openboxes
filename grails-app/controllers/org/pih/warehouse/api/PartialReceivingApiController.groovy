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
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.ShipmentItem

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
            receiptService.saveAndCompletePartialReceipt(partialReceipt)
            receiptService.saveInboundTransaction(partialReceipt)
            partialReceipt = receiptService.getPartialReceipt(params.id)
        }
        else if (partialReceipt.receiptStatus == PartialReceiptStatus.PENDING || partialReceipt.receiptStatus == PartialReceiptStatus.CHECKING) {
            receiptService.savePartialReceipt(partialReceipt)
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
                String shipmentItemId = shipmentItemMap.get("shipmentItemId")
                String receiptItemId = shipmentItemMap.get("receiptItemId")
                boolean newLine = Boolean.valueOf(shipmentItemMap.newLine ?: "false")
                PartialReceiptItem partialReceiptItem = partialReceiptContainer.partialReceiptItems.find {
                    receiptItemId ? it?.receiptItem?.id == receiptItemId : it?.shipmentItem?.id == shipmentItemId
                }
                // Create new item if not exists
                if (!partialReceiptItem || newLine) {
                    partialReceiptItem = new PartialReceiptItem()
                    partialReceiptItem.shipmentItem = ShipmentItem.get(shipmentItemId)
                    partialReceiptItem.product = shipmentItemMap.get("product.id") ? Product.load(shipmentItemMap.get("product.id")) : null
                    partialReceiptContainer.partialReceiptItems.add(partialReceiptItem)
                }
                bindData(partialReceiptItem, shipmentItemMap)
            }
        }
    }
}


