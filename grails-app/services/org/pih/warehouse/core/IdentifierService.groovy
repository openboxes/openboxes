/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.apache.commons.lang.RandomStringUtils
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.WordUtils
import org.apache.commons.lang.text.StrSubstitutor
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.order.Order
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.product.ProductType
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.shipping.Shipment

import java.sql.SQLIntegrityConstraintViolationException
import java.util.regex.Matcher
import java.util.regex.Pattern

@Transactional
class IdentifierService {

    GrailsApplication grailsApplication
    def dataService
    def productTypeService

    /**
     * A: alphabetic
     * L: letter
     * N: numeric
     * D: digit
     * 0-9: digit
     *
     * @param format
     * @return
     */
    def generateIdentifier(String format) {
        if (!format || format.isEmpty()) {
            println "format must be specified"
            throw new IllegalArgumentException("Format pattern string must be specified")
        }

        String identifier = ""
        for (int i = 0; i < format.length(); i++) {
            switch (format[i]) {
                case 'N':
                    identifier += RandomStringUtils.random(1, grailsApplication.config.openboxes.identifier.numeric)
                    break
                case 'D':
                    identifier += RandomStringUtils.random(1, grailsApplication.config.openboxes.identifier.numeric)
                    break
                case 'L':
                    identifier += RandomStringUtils.random(1, grailsApplication.config.openboxes.identifier.alphabetic)
                    break
                case 'A':
                    identifier += RandomStringUtils.random(1, grailsApplication.config.openboxes.identifier.alphanumeric)
                    break
                default:
                    identifier += format[i]
            }
        }

        return identifier
    }

    /**
     * Generate a random identifier of given length using alphanumeric characters.
     *
     * @param length
     */
    def generateIdentifier(int length) {
        return RandomStringUtils.random(length, grailsApplication.config.openboxes.identifier.alphanumeric)
    }

    String generateInvoiceIdentifier() {
        return generateIdentifier(grailsApplication.config.openboxes.identifier.invoice.format)
    }

    def generateOrderIdentifier() {
        return generateIdentifier(grailsApplication.config.openboxes.identifier.order.format)
    }

    def generatePurchaseOrderIdentifier() {
        return generateIdentifier(grailsApplication.config.openboxes.identifier.purchaseOrder.format)
    }

    def generateProductIdentifier(String format) {
        if (StringUtils.isNotBlank(format)) {
            return generateIdentifier(format)
        }

        return generateIdentifier(grailsApplication.config.openboxes.identifier.product.format)
    }

    def generateProductIdentifier() {
        return generateIdentifier(grailsApplication.config.openboxes.identifier.product.format)
    }

    def generateProductSupplierIdentifier() {
        return generateProductSupplierIdentifier(null)
    }

    def generateProductSupplierIdentifier(String prefix) {
        String identifier = generateIdentifier(grailsApplication.config.openboxes.identifier.productSupplier.format)
        Boolean prefixEnabled = grailsApplication.config.openboxes.identifier.productSupplier.prefix.enabled
        if (prefixEnabled && prefix) {
            identifier = "${prefix}${Constants.DEFAULT_NAME_SEPARATOR}${identifier}"
        }
        return identifier
    }

    def generateProductSupplierIdentifier(String prefix, String suffix) {
        if (prefix && suffix) {
            if (ProductSupplier.findByCode("${prefix}${Constants.DEFAULT_NAME_SEPARATOR}${suffix}")) {
                return generateProductSupplierIdentifier("${prefix}${Constants.DEFAULT_NAME_SEPARATOR}${suffix}")
            }
            return "${prefix}${Constants.DEFAULT_NAME_SEPARATOR}${suffix}"
        } else if (prefix) {
            return generateProductSupplierIdentifier(prefix)
        } else {
            return generateIdentifier(grailsApplication.config.openboxes.identifier.productSupplier.format)
        }
    }

    def generateRequisitionIdentifier() {
        return generateIdentifier(grailsApplication.config.openboxes.identifier.requisition.format)
    }

    def generateShipmentIdentifier() {
        return generateIdentifier(grailsApplication.config.openboxes.identifier.shipment.format)
    }

    def generateReceiptIdentifier() {
        return generateIdentifier(grailsApplication.config.openboxes.identifier.receipt.format)
    }

    def generateTransactionIdentifier() {
        return generateIdentifier(grailsApplication.config.openboxes.identifier.transaction.format)
    }

    def generateLocationIdentifier() {
        return generateIdentifier(grailsApplication.config.openboxes.identifier.location.format)
    }

    def generateOrganizationIdentifier() {
        return generateIdentifier(grailsApplication.config.openboxes.identifier.organization.format)
    }

    // TODO: refactor this to sequence or sth else, because this way it will fail when more than 10 duplicates
    def generateOrganizationIdentifier(String name) {
        Integer minSize = grailsApplication.config.openboxes.identifier.organization.minSize
        Integer maxSize = grailsApplication.config.openboxes.identifier.organization.maxSize

        // Clean up string by removing everything after command
        name = name.split(",")[0].capitalize()
        String identifier = WordUtils.initials(name)?.replaceAll("[^a-zA-Z0-9]", "")
        if (identifier.length() < minSize) {
            identifier = WordUtils.abbreviate(name, minSize, maxSize, null)
        }
        else if (identifier.length() > maxSize) {
            identifier = identifier.substring(0, maxSize)
        }

        // Checking for a duplicates
        if (validateOrganizationIdentifier(identifier.toUpperCase())) {
            return identifier.toUpperCase()
        }

        String identifierWithHighestNumber = getOrganizationIdentifierWithHighestSuffix(identifier.substring(0, identifier.size() - 1))
        if (identifierWithHighestNumber) {
            char suffix = identifierWithHighestNumber.charAt(identifierWithHighestNumber.size() - 1)
            suffix++

            return identifier.toUpperCase().substring(0, identifier.size() -1) + suffix
        }
        return identifier.length() < maxSize ? identifier.toUpperCase() + '0': identifier.toUpperCase().substring(0, maxSize - 1) + '0'
    }

    def validateOrganizationIdentifier(String identifier) {
        println "Validating organization identifier " + identifier
        def count = Organization.executeQuery( "select count(o.code) from Organization o where code = :identifier", [identifier: identifier] )

        return count ? (count[0] == 0) : false
    }

    def getOrganizationIdentifierWithHighestSuffix(String identifier) {
        def organizations = Organization.executeQuery( "select o.code from Organization o where code like :identifier", [identifier: identifier + '%'] )
        return organizations.findAll { Character.isDigit(it.charAt(it.size() - 1)) } ? organizations.findAll { Character.isDigit(it.charAt(it.size() - 1)) }.sort()?.last() : null
    }

    def extractSequenceFormat(String productIdentifierFormat, String sequentialPatternChar, Integer allowedSequences) {
        Pattern pattern = Pattern.compile("${sequentialPatternChar}+")
        Matcher matcher = pattern.matcher(productIdentifierFormat)
        int count = 0
        def sequenceFormat = ""

        while (matcher.find()) {
            sequenceFormat = matcher.group()
            count++
        }

        // If custom identifier contains more than one sequential part, then throw exception
        if (count > allowedSequences) {
            throw new IllegalArgumentException("Cannot have more sequence numbers than ${allowedSequences} in the same identifier")
        }

        return sequenceFormat
    }

    def generateSequentialIdentifier(String identifierFormat, String sequenceFormat, String sequenceNumber) {
        List identifierFormatComponents
        List identifierComponents

        // Split custom identifier by sequence format (with keeping sequence inside identifier components)
        String sequenceDelimiter = "((?<=${sequenceFormat})|(?=${sequenceFormat}))"
        identifierFormatComponents = identifierFormat.split(sequenceDelimiter)
        identifierComponents = identifierFormatComponents.collect { String identifierFormatComponent ->
            if (identifierFormatComponent.contains("0")) {
                return generateSequenceNumber(sequenceNumber.toString(), identifierFormatComponent)
            }
            else {
                return generateIdentifier(identifierFormatComponent)
            }
        }

        return identifierComponents.join("")
    }

    def generateSequentialProductIdentifier(ProductType productType) {
        // If the product type does not have a custom identifier or does not contain character "0" which indicates the sequential part,
        // then generate sequential product code from product type code
        if (!productType.productIdentifierFormat || !productType.productIdentifierFormat.contains("0")) {
            return generateSequentialProductIdentifierFromCode(productType)
        }

        def sequenceFormat = extractSequenceFormat(productType.productIdentifierFormat, Constants.DEFAULT_SEQUENCE_NUMBER_FORMAT_CHAR, 1)
        def sequenceNumber = productTypeService.getAndSetNextSequenceNumber(productType)

        return generateSequentialIdentifier(productType.productIdentifierFormat, sequenceFormat, sequenceNumber.toString())
    }

    String generateSequentialProductIdentifierFromCode(ProductType productType) {
        if (!productType) {
            throw new IllegalArgumentException("Missing product type")
        }

        def defaultProductTypeId = Holders.config.openboxes.identifier.defaultProductType.id
        if (!productType.code && productType.id != defaultProductTypeId) {
            throw new IllegalArgumentException("Can only generate sequential product code without specified product type code " +
                "for default product type with id: ${defaultProductTypeId}")
        }

        Integer sequenceNumber = productTypeService.getAndSetNextSequenceNumber(productType)
        String sequenceNumberStr = generateSequenceNumber(sequenceNumber.toString())

        String template = Holders.config.openboxes.identifier.productCode.format
        String delimiter = Holders.config.openboxes.identifier.productCode.delimiter
        Map properties = Holders.config.openboxes.identifier.productCode.properties
        Map model = dataService.transformObject(productType, properties)
        // If no value present in the map (all are empty or null), then do not include delimiter
        if (model?.any { key, value -> value }) {
            model.put("delimiter", delimiter)
        } else {
            model.put("delimiter", "")
        }
        model.put("sequenceNumber", sequenceNumberStr)
        return renderTemplate(template, model)
    }

    /**
     * Generate random product identifier for specific productIdentifierFormat if it is set on the given product type,
     * or for configured config.openboxes.identifier.product.format
     * */
    String generateRandomProductIdentifier(ProductType productType) {
        // If the product type is null or the product type does not have a custom identifier,
        // then generate product code from DEFAULT_PRODUCT_NUMBER_FORMAT
        if (!productType || !productType.productIdentifierFormat) {
            return generateProductIdentifier()
        }

        // if the product type does not contain sequential part, then generate identifier basing on custom format
        return generateIdentifier(productType.productIdentifierFormat)
    }

    /**
     * Generates product identifier for product with given product type.
     *
     * Options of generated product identifiers (and what needs to be configured to achieve it):
     *  1. If product type is the systems' default (id == openboxes.identifier.defaultProductType.id):
     *      a) SEQUENTIAL - requires:
     *          - setting openboxes.identifier.productCode.generatorType as IdentifierGeneratorTypeCode.SEQUENCE,
     *          - one of:
     *              - productIdentifierFormat (needs sequential part to be present in this field),
     *              - code in combination with with openboxes.identifier.productCode.format template,
     *              - non of both above will result 'number' only sequence, the identifier will be based on
     *                the openboxes.identifier.productCode.delimiter, openboxes.identifier.productCode.format,
     *                openboxes.identifier.productCode.properties and identifier.sequenceNumber.format config
     *                (available only for the systems' default product type)
     *      b) RANDOM - requires:
     *          - setting openboxes.identifier.productCode.generatorType as IdentifierGeneratorTypeCode.RANDOM,
     *          - one of:
     *              - filled productIdentifierFormat
     *              - empty productIdentifierFormat (will be generated basing on the openboxes.identifier.product.format)
     *  2. If product type is NOT the systems' default:
     *      a) SEQUENTIAL - requires one of:+
     *          - product identifier format containng sequential part
     *          - code
     *      b) RANDOM - requires one of:
     *          - filled productIdentifierFormat
     *          - empty productIdentifierFormat (will be generated basing on the openboxes.identifier.product.format)
     * */
    String generateProductIdentifier(ProductType productType) {
        IdentifierGeneratorTypeCode generatorTypeCode = Holders.config.openboxes.identifier.productCode.generatorType as IdentifierGeneratorTypeCode
        def defaultProductType = Holders.config.openboxes.identifier.defaultProductType

        if (productType?.id == defaultProductType.id) {
            // If product type is systems default then generate basing on the generator type
            switch (generatorTypeCode) {
                case IdentifierGeneratorTypeCode.SEQUENCE:
                    return generateSequentialProductIdentifier(productType)
                case IdentifierGeneratorTypeCode.RANDOM:
                    return generateRandomProductIdentifier(productType)
                default:
                    return generateRandomProductIdentifier(productType)
            }
        }

        // If product type is not default
        // and has sequential identifier format containing "0" character which indicates the sequential part
        // or identifier format is not specified when generatorTypeCode is SEQUENCE but has a code
        // then generate sequential, otherwise random
        Boolean shouldGenerateSequential = generatorTypeCode == IdentifierGeneratorTypeCode.SEQUENCE

        if (productType?.hasSequentialFormat() || (productType?.code && shouldGenerateSequential)) {
            return generateSequentialProductIdentifier(productType)
        }

        return generateRandomProductIdentifier(productType)
    }

    def generateSequenceNumber(String sequenceNumber, String sequenceNumberFormat) {
        return StringUtils.leftPad(sequenceNumber, sequenceNumberFormat.length(), sequenceNumberFormat.substring(0, 1))
    }

    def generateSequenceNumber(String sequenceNumber) {
        String sequenceNumberFormat = grailsApplication.config.openboxes.identifier.sequenceNumber.format
        return generateSequenceNumber(sequenceNumber, sequenceNumberFormat)
    }

    def renderTemplate(String template, Map model) {
        return StrSubstitutor.replace(template, model)
    }

    void assignTransactionIdentifiers() {
        def transactions = Transaction.findAll("from Transaction as t where transactionNumber is null or transactionNumber = ''")
        transactions.each { transaction ->
            try {
                println "Assigning identifier to transaction " + transaction.id + " " + transaction.dateCreated + " " + transaction.lastUpdated
                Transaction.withTransaction {
                    transaction.transactionNumber = generateTransactionIdentifier()
                    if (!transaction.merge(flush: true, validate: false)) {
                        println transaction.errors
                    }
                }
                println "Assigned identifier to transaction " + transaction.id + " " + transaction.dateCreated + " " + transaction.lastUpdated
            } catch (ObjectNotFoundException e) {
                println("Unable to assign identifier to transaction with ID " + transaction?.id + ": " + e.message)

            } catch (Exception e) {
                println("Unable to assign identifier to transaction with ID " + transaction?.id + ": " + e.message)
            }
        }
    }


    void assignProductIdentifiers() {
        def products = Product.findAll("from Product as p where p.active = true and (p.productCode is null or p.productCode = '')")
        products.each { product ->
            try {
                def productCode = product.productType ? generateProductIdentifier(product.productType) : generateProductIdentifier()
                println "Assigning identifier ${productCode} to product " + product.id + " " + product.name

                // Check to see if there's already a product with that product code
                if (!Product.findByProductCode(productCode)) {
                    product.productCode = productCode
                    if (!product.merge(flush: true, validate: false)) {
                        println product.errors
                    }
                }
            } catch (SQLIntegrityConstraintViolationException e) {
                log.warn("Unable to assign identifier due to constraint violation: " + e.message, e)
            } catch (Exception e) {
                log.warn("Unable to assign identifier to product with ID " + product?.id + ": " + e.message, e)
            }
        }
    }

    void assignShipmentIdentifiers() {
        def shipments = Shipment.findAll("from Shipment as s where shipmentNumber is null or shipmentNumber = ''")
        shipments.each { shipment ->
            println "Assigning identifier to shipment " + shipment.id + " " + shipment.name
            try {
                shipment.shipmentNumber = generateShipmentIdentifier()
                if (!shipment.merge(flush: true, validate: false)) {
                    println shipment.errors
                }
            } catch (Exception e) {
                println("Unable to assign identifier to shipment with ID " + shipment?.id + ": " + e.message)
            }
        }
    }

    void assignReceiptIdentifiers() {
        def receipts = Receipt.findAll("from Receipt as s where receiptNumber is null or receiptNumber = ''")
        receipts.each { Receipt receipt ->
            println "Assigning identifier to receipt " + receipt.id
            try {
                receipt.receiptNumber = generateReceiptIdentifier()
                if (!receipt.merge(flush: true, validate: false)) {
                    println receipt.errors
                }
            } catch (Exception e) {
                println("Unable to assign identifier to receipt with ID " + receipt?.id + ": " + e.message)
            }
        }
    }


    void assignRequisitionIdentifiers() {
        def requisitions = Requisition.findAll("from Requisition as r where (requestNumber is null or requestNumber = '') and (isTemplate is null or isTemplate = false)")
        requisitions.each { requisition ->
            try {
                println "Assigning identifier to requisition " + requisition.id + " " + requisition.name
                requisition.requestNumber = generateRequisitionIdentifier()
                if (!requisition.merge(flush: true, validate: false)) {
                    println requisition.errors
                }
            } catch (Exception e) {
                println("Unable to assign identifier to requisition with ID " + requisition?.id + ": " + e.message)
            }
        }
    }

    void assignOrderIdentifiers() {
        def orders = Order.findAll("from Order as o where orderNumber is null or orderNumber = ''")
        orders.each { order ->
            try {
                println "Assigning identifier to order " + order.id + " " + order.name
                order.orderNumber = generateOrderIdentifier()
                if (!order.merge(flush: true, validate: false)) {
                    println order.errors
                }
            } catch (Exception e) {
                println("Unable to assign identifier to order with ID " + order?.id + ": " + e.message)
            }
        }
    }


}
