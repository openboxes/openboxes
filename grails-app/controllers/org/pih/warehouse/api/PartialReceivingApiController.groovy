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
import org.pih.warehouse.core.Constants
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.ShipmentItem

class PartialReceivingApiController {

    def receiptService
    def shipmentService
    def dataService

    def list = {
        render ([data: []] as JSON)
    }

    def read = {
        PartialReceipt partialReceipt = receiptService.getPartialReceipt(params.id, params.stepNumber)
        render([data:partialReceipt] as JSON)
    }

    def update = {

        JSONObject jsonObject = request.JSON

        log.info "JSON " + jsonObject.toString(4)

        PartialReceipt partialReceipt = receiptService.getPartialReceipt(params.id, params.stepNumber)

        bindPartialReceiptData(partialReceipt, jsonObject)

        if (partialReceipt.receiptStatus == PartialReceiptStatus.COMPLETED) {
            log.info "Save partial receipt"
            receiptService.saveAndCompletePartialReceipt(partialReceipt)
            receiptService.saveInboundTransaction(partialReceipt)
        }
        else if (partialReceipt.receiptStatus == PartialReceiptStatus.PENDING || partialReceipt.receiptStatus == PartialReceiptStatus.CHECKING) {
            receiptService.savePartialReceipt(partialReceipt, false)
        }
        else if (partialReceipt.receiptStatus == PartialReceiptStatus.ROLLBACK) {
            receiptService.rollbackPartialReceipts(partialReceipt.shipment)
            partialReceipt = receiptService.getPartialReceipt(params.id, params.stepNumber)
        }


        render([data:partialReceipt] as JSON)
    }

    def exportCsv = {
        PartialReceipt partialReceipt = receiptService.getPartialReceipt(params.id,  params.stepNumber)

        // We need to create at least one row to ensure an empty template
        if (partialReceipt?.partialReceiptContainers?.partialReceiptItems?.empty) {
            partialReceipt?.partialReceiptContainers?.partialReceiptItems?.add(new PartialReceiptItem())
        }

        def lineItems = partialReceipt.partialReceiptItems.collect {
            [
            receiptItemId: it?.receiptItem?.id ?: "",
            Code: it?.receiptItem?.product?.productCode ?: "",
            Name: it?.receiptItem?.product?.name ?: "",
            "Lot/Serial No.": it?.lotNumber ?: "",
            "Expration date": it?.expirationDate?.format("MM/dd/yyyy") ?: "",
            "Bin Location": it?.binLocation ?: "",
            Recipient: it?.recipient ?: "",
            Shipped: it?.quantityShipped ?: "",
            Receied: it?.quantityReceived ?: "",
            "To receive": it?.quantityRemaining ?: "",
            "Receiving now": it?.quantityReceiving ?: "",
            Comment: it?.comment ?: ""
            ]
        }

        String csv = dataService.generateCsv(lineItems)
        response.setHeader("Content-disposition", "attachment; filename=\"StockMovementItems-${params.id}.csv\"")
        render(contentType:"text/csv", text: csv.toString(), encoding:"UTF-8")
    }

    Date parseDate(String date) {
        return date ? Constants.DELIVERY_DATE_FORMATTER.parse(date) : null
    }

    void bindPartialReceiptData(PartialReceipt partialReceipt, JSONObject jsonObject) {

        // Date is not bound properly using default JSON binding
        if (jsonObject.containsKey("dateDelivered")) {
            partialReceipt.dateDelivered = parseDate(jsonObject.remove("dateDelivered"))
        }

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
                boolean originalLine = Boolean.valueOf(shipmentItemMap.originalLine ?: "false")
                PartialReceiptItem partialReceiptItem = partialReceiptContainer.partialReceiptItems.find {
                    receiptItemId ? it?.receiptItem?.id == receiptItemId : it?.shipmentItem?.id == shipmentItemId
                }
                // Create new item if not exists
                if (!partialReceiptItem || newLine) {
                    partialReceiptItem = new PartialReceiptItem()
                    partialReceiptItem.shipmentItem = ShipmentItem.get(shipmentItemId)
                    partialReceiptItem.product = shipmentItemMap.get("product.id") ? Product.load(shipmentItemMap.get("product.id")) : null
                    partialReceiptItem.isSplitItem = newLine
                    partialReceiptContainer.partialReceiptItems.add(partialReceiptItem)
                }
                bindData(partialReceiptItem, shipmentItemMap)

                partialReceiptItem.shouldSave = newLine || originalLine || partialReceiptItem.quantityReceiving != null || partialReceiptItem.receiptItem
            }
        }
    }
}


