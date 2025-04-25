/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.shipping

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.plugins.csv.CSVMapReader
import grails.util.Holders
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.data.PersonService
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.Product

import javax.xml.bind.ValidationException
import java.text.SimpleDateFormat

@Transactional
class CombinedShipmentService {

    def stockMovementService
    GrailsApplication grailsApplication
    def inventoryService
    def messageService
    def localizationService
    PersonService personService

    /**
     * Parse the given text into a list of maps.
     *
     * @param inputStream
     * @return
     */
    List parseOrderItemsFromTemplateImport(String text) {
        List orderItems = []
        try {
            def settings = [skipLines: 1]
            def csvMapReader = new CSVMapReader(new StringReader(text), settings)
            csvMapReader.fieldKeys = [
                    'orderNumber',
                    'id',
                    'productCode',
                    'productName',
                    'lotNumber',
                    'expiry',
                    'quantityToShip',
                    'unitOfMeasure',
                    'palletName', // pack level 1
                    'boxName', // pack level 2]
                    'recipient',
                    'budgetCode',
            ]
            orderItems = csvMapReader.toList()

        } catch (Exception e) {
            throw new RuntimeException("Error parsing order item CSV: " + e.message, e)
        }

        return orderItems
    }

    boolean validateItemsFromTemplateImport(Shipment shipment, List lineItems) {
        def valid = true
        lineItems.each { line ->
            line.errors = []
            if (!line.orderNumber) {
                line.errors << "Order number is required"
                valid = false
            }
            Order order = Order.findByOrderNumber(line.orderNumber)
            if (!order) {
                line.errors << "There is no order with number: ${line.orderNumber}"
                valid = false
            }

            if (order && (order.origin != shipment.origin || order.destination != shipment.destination)) {
                line.errors << "Order must be from the same origin and destination as shipment"
                valid = false
            }

            OrderItem orderItem = null
            if (!line.productCode) {
                line.errors << "Product code is required"
                valid = false
            }
            Product product = Product.findByProductCode(line.productCode)
            if (!product) {
                line.errors << "Product does not exit"
                valid = false
            }

            if (product && product.lotAndExpiryControl && (!line.expiry || !line.lotNumber)) {
                line.errors << "Both lot number and expiry date are required for the '${line.productCode} ${line.productName}' product."
                valid = false
            }

            if (!line.id) {
                def codes = lineItems.findAll { it.productCode == line.productCode }
                if (codes.size() > 1) {
                    line.errors << "Product code ${line.productCode} appears more than once on PO. Must specify order item id"
                    valid = false
                }
                orderItem = OrderItem.findByOrderAndProduct(order, product)
            } else {
                orderItem = OrderItem.get(line.id)
            }
            if (!orderItem) {
                line.errors << "Order item does not exit"
                valid = false
            }

            if (orderItem && product && orderItem.product != product) {
                line.errors << "Product code ${line.productCode} does not match product on the order item with given id ${line.id}"
                valid = false
            }

            if (line.expiry) {
                def dateFormat = new SimpleDateFormat("MM/dd/yyyy")
                def expiry = null
                try {
                    expiry = dateFormat.parse(line.expiry)
                } catch (Exception e) {
                    line.errors << "Unable to parse expiry date: ${line.expiry}"
                    valid = false
                }
                Date date = Constants.EXPIRATION_DATE_FORMATTER.parse(
                    Holders.grailsApplication.config.openboxes.expirationDate.minValue
                )
                def today = new Date()
                today.clearTime()
                if (expiry) {
                    if (expiry < date) {
                        line.errors << "Expiry date is invalid. Please enter a date after ${date.getYear()+1900}."
                        valid = false
                    }
                    if (expiry < today) {
                        line.errors << messageService.getMessage("purchaseOrder.dateError.label", [line.orderNumber, line.productCode] as Object [], "Expiry date for PO ${line.orderNumber}, Product ${line.productCode} cannot be in the past.", localizationService.getCurrentLocale())
                        valid = false
                    }
                }
            }

            if (!line.quantityToShip) {
                line.errors << "Quantity To Ship is empty"
                valid = false
            } else {
                def qtyParsed = null
                try {
                    qtyParsed = Integer.parseInt(line.quantityToShip.toString())
                } catch (Exception e) {
                    line.errors << "Quantity To Ship value: ${line.quantityToShip} can't be parsed properly"
                    valid = false
                }
                if (qtyParsed) {
                    def linesWithSameOrderItem = lineItems.findAll { it.id && it.id == line.id ||
                            (!it.id && it.productCode == line.productCode && it.orderId == line.orderId) }
                    def qtyToShipTotal
                    if (linesWithSameOrderItem.size() > 1) {
                        linesWithSameOrderItem.each { it ->
                            try {
                                it.quantityToShip = Integer.parseInt(it.quantityToShip.toString())
                            } catch (Exception e) {
                                valid = false
                            }
                        }
                        qtyToShipTotal = linesWithSameOrderItem.quantityToShip.sum()
                    } else {
                        qtyToShipTotal = qtyParsed
                    }
                    if (orderItem && qtyToShipTotal > orderItem.getQuantityRemainingToShip()) {
                        line.errors << "Qty to ship for product ${line.productCode}, order ${line.orderNumber} is greater than qty available to ship(${orderItem.getQuantityRemainingToShip()})."
                        valid = false
                    }
                }
            }

            if (line.unitOfMeasure) {
                String[] uomParts = line.unitOfMeasure.split("/")
                UnitOfMeasure uom = UnitOfMeasure.findByCode(uomParts[0])
                def quantityPerUom = uomParts[1]
                if (uomParts.length <= 1 || !uom) {
                    line.errors << "Could not find provided Unit of Measure: ${line.unitOfMeasure}."
                    valid = false
                }
                if (uom && orderItem && (orderItem.quantityUom?.code != uomParts[0] || orderItem.quantityPerUom.intValue().toString() != quantityPerUom)) {
                    line.errors << messageService.getMessage("errors.differentUOM.label", [line.productCode] as Object [], "UOM for product code ${line.productCode} does not match UOM on PO.", localizationService.getCurrentLocale())
                    valid = false
                }
            }
        }
        return valid
    }

    def addItemsToShipment(Shipment shipment, List importedLines) {
        importedLines.each { line ->
            if ((line.quantityToShip as int) > 0) {
                Order order = Order.findByOrderNumber(line.orderNumber)
                Product product = Product.findByProductCode(line.productCode)
                OrderItem orderItem = null
                if (!line.id) {
                    orderItem = OrderItem.findByOrderAndProduct(order, product)
                } else {
                    orderItem = OrderItem.get(line.id)
                }

                InventoryItem inventoryItem = null
                def expiry = null
                if (line.lotNumber) {
                    if (line.expiry) {
                        def dateFormat = new SimpleDateFormat("MM/dd/yyyy")
                        expiry = dateFormat.parse(line.expiry)
                    }
                    inventoryItem = inventoryService.findOrCreateInventoryItem(product, line.lotNumber, expiry)
                }

                Person recipient = line.recipient ? Person.get(line.recipient) : null
                if (!recipient && line.recipient) {
                    recipient = line.recipient.contains(" ") ?
                            personService.getActivePersonByName(line.recipient) :
                            personService.getActivePersonByEmail(line.recipient)
                    if (!recipient) {
                        throw new IllegalArgumentException("Unable to locate person")
                    }
                }

                ShipmentItem shipmentItem = new ShipmentItem()
                shipmentItem.product = product
                shipmentItem.quantity = (Integer.parseInt(line.quantityToShip.toString())) * orderItem.quantityPerUom
                shipmentItem.recipient = recipient
                if (line.palletName) {
                    shipmentItem.container = stockMovementService
                            .createOrUpdateContainer(shipment, line.palletName, line.boxName)
                }
                if (inventoryItem) {
                    shipmentItem.lotNumber = line.lotNumber
                    shipmentItem.expirationDate = expiry ?: inventoryItem.expirationDate
                    shipmentItem.inventoryItem = inventoryItem
                }
                shipment.addToShipmentItems(shipmentItem)
                orderItem.addToShipmentItems(shipmentItem)

                shipmentItem.save()
            }
        }

        if (shipment.hasErrors() || !shipment.save(flush: true)) {
            throw new ValidationException("Invalid shipment", shipment.errors)
        }
    }

    void importTemplate(ImportDataCommand command, String shipmentId) {
        Shipment shipment = Shipment.get(shipmentId)
        def importFile = command.importFile
        if (importFile.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty")
        }

        if (importFile.contentType != "text/csv") {
            throw new IllegalArgumentException("File must be in CSV format")
        }
        String csv = new String(importFile.bytes)
        List importedLines = parseOrderItemsFromTemplateImport(csv)
        if (validateItemsFromTemplateImport(shipment, importedLines)) {
            addItemsToShipment(shipment, importedLines)
        } else {
            String message = "Failed to import template due to validation errors:"
            importedLines.eachWithIndex { line, idx ->
                if (line.errors) {
                    message += "<br>Row ${idx + 1}: ${line.errors.join("; ")}"
                }
            }
            throw new ValidationException(message)
        }
    }

    void saveItemsToShipment(Shipment shipment, List itemsToAdd) {
        if (itemsToAdd) {
            itemsToAdd.sort { it.sortOrder }.each {
                OrderItem orderItem = OrderItem.get(it.orderItemId)
                ShipmentItem shipmentItem = new ShipmentItem()
                shipmentItem.product = orderItem.product
                shipmentItem.inventoryItem = orderItem.inventoryItem
                shipmentItem.product = orderItem.product
                shipmentItem.quantity = orderItem.quantity
                shipmentItem.recipient = orderItem.recipient
                shipmentItem.quantity = it.quantityToShip * orderItem.quantityPerUom
                shipmentItem.sortOrder = shipment.shipmentItems ? shipment.shipmentItems.size() * 100 : 0
                orderItem.addToShipmentItems(shipmentItem)
                shipment.addToShipmentItems(shipmentItem)
            }
            shipment.save()
        }
    }
}
