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
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.ShipmentItem

class PartialReceivingApiController {

    def receiptService
    def shipmentService
    def dataService

    def list = {
        render([data: []] as JSON)
    }

    def read = {
        PartialReceipt partialReceipt = receiptService.getPartialReceipt(params.id, params.stepNumber)
        render([data: partialReceipt] as JSON)
    }

    def update = {

        JSONObject jsonObject = request.JSON

        log.debug "JSON " + jsonObject.toString(4)

        PartialReceipt partialReceipt = receiptService.getPartialReceipt(params.id, params.stepNumber)

        bindPartialReceiptData(partialReceipt, jsonObject)

        if (partialReceipt.receiptStatus == PartialReceiptStatus.COMPLETED) {
            log.debug "Save partial receipt"
            receiptService.saveAndCompletePartialReceipt(partialReceipt)

        } else if (partialReceipt.receiptStatus == PartialReceiptStatus.PENDING || partialReceipt.receiptStatus == PartialReceiptStatus.CHECKING) {
            receiptService.savePartialReceipt(partialReceipt, false)
        } else if (partialReceipt.receiptStatus == PartialReceiptStatus.ROLLBACK) {
            receiptService.rollbackPartialReceipts(partialReceipt.shipment)
            partialReceipt = receiptService.getPartialReceipt(params.id, params.stepNumber)
        }


        render([data: partialReceipt] as JSON)
    }

    def exportCsv = {
        JSONObject jsonObject = request.JSON

        PartialReceipt partialReceipt = receiptService.getPartialReceipt(params.id, params.stepNumber)

        bindPartialReceiptData(partialReceipt, jsonObject)

        // We need to create at least one row to ensure an empty template
        if (partialReceipt?.partialReceiptContainers?.partialReceiptItems?.empty) {
            partialReceipt?.partialReceiptContainers?.partialReceiptItems?.add(new PartialReceiptItem())
        }

        def lineItems = partialReceipt.partialReceiptItems.sort { a, b ->
            a.shipmentItem?.requisitionItem?.orderIndex <=> b.shipmentItem?.requisitionItem?.orderIndex ?:
                    a.shipmentItem?.sortOrder <=> b.shipmentItem?.sortOrder ?:
                            a.receiptItem?.sortOrder <=> b.receiptItem?.sortOrder
        }.collect {
            [
                    "Receipt item id" : it?.receiptItem?.id ?: "",
                    "Shipment item id": it?.shipmentItem?.id ?: "",
                    Code              : it?.shipmentItem?.product?.productCode ?: "",
                    Name              : it?.shipmentItem?.product?.name ?: "",
                    "Lot/Serial No."  : it?.lotNumber ?: "",
                    "Expiration date" : it?.expirationDate?.format("MM/dd/yyyy") ?: "",
                    "Bin Location"    : it?.binLocation ?: "",
                    Recipient         : it?.recipient?.id ?: "",
                    Shipped           : it?.quantityShipped ?: "",
                    Received          : it?.quantityReceived ?: "",
                    "To receive"      : it?.quantityRemaining ?: "",
                    "Receiving now"   : it?.quantityReceiving ?: "",
                    Comment           : it?.comment ?: ""
            ]
        }

        String csv = dataService.generateCsv(lineItems)
        response.setHeader("Content-disposition", "attachment; filename=\"PartialReceiving-${params.id}.csv\"")
        render(contentType: "text/csv", text: csv.toString(), encoding: "UTF-8")
    }

    def importCsv = { ImportDataCommand command ->

        try {
            PartialReceipt partialReceipt = receiptService.getPartialReceipt(params.id, "1")

            def importFile = command.importFile
            if (importFile.isEmpty()) {
                throw new IllegalArgumentException("File cannot be empty")
            }

            if (importFile.fileItem.contentType != "text/csv") {
                throw new IllegalArgumentException("File must be in CSV format")
            }

            String csv = new String(importFile.bytes)
            def settings = [separatorChar: ',', skipLines: 1]
            csv.toCsvReader(settings).eachLine { tokens ->
                String receiptItemId = tokens[0] ?: null
                String shipmentItemId = tokens[1] ?: null
                String code = tokens[2] ?: null
                String lotNumber = tokens[4] ?: null
                String expirationDate = tokens[5] ?: null
                String binLocation = tokens[6] ?: null
                String recipientId = tokens[7] ?: null
                Integer quantityReceiving = tokens[11] ? tokens[11].toInteger() : null
                String comment = tokens[12] ? tokens[12] : null

                List<PartialReceiptItem> partialReceiptItems = []
                partialReceipt.partialReceiptItems.each {
                    partialReceiptItems.addAll(it)
                }

                PartialReceiptItem partialReceiptItem = partialReceiptItems.find {
                    receiptItemId ? it?.receiptItem?.id == receiptItemId : it?.shipmentItem?.id == shipmentItemId
                }

                if ((expirationDate && Constants.EXPIRATION_DATE_FORMATTER.parse(expirationDate).format(Constants.EXPIRATION_DATE_FORMAT) != partialReceiptItem.expirationDate.format(Constants.EXPIRATION_DATE_FORMAT))
                        || ((recipientId && recipientId != partialReceiptItem?.recipient?.id) || (!recipientId && partialReceiptItem.recipient))
                        || ((lotNumber && lotNumber != partialReceiptItem.lotNumber) || (!lotNumber && partialReceiptItem.lotNumber))
                        || ((binLocation && binLocation != partialReceiptItem.binLocation.name) || (!binLocation && partialReceiptItem.binLocation))
                        || ((code && code != partialReceiptItem.product.productCode) || (!code && partialReceiptItem.product.productCode))) {
                    throw new IllegalArgumentException("You can only import the Receiving Now and the Comment fields. To make other changes, please use the edit line feature. You can then export and import the template again.")
                }

                if (!partialReceiptItem) {
                    throw new IllegalArgumentException("Receipt item id: ${receiptItemId} not found")
                }

                partialReceiptItem.quantityReceiving = quantityReceiving
                partialReceiptItem.comment = comment
                partialReceiptItem.shouldSave = quantityReceiving != null
            }

            receiptService.savePartialReceipt(partialReceipt, false)

        } catch (Exception e) {
            log.warn("Error occurred while importing CSV: " + e.message, e)
            response.status = 500
            render([errorCode: 500, errorMessage: e?.message ?: "An unknown error occurred during import"] as JSON)
            return
        }

        render([data: "Data was imported successfully"] as JSON)
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


