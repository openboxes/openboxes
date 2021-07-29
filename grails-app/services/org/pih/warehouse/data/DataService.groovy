/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.data

import groovy.sql.Sql
import org.apache.commons.lang.StringEscapeUtils
import org.grails.plugins.csv.CSVWriter
import org.grails.plugins.excelimport.ExcelImportUtils
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.ProductPrice
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.UnitOfMeasureClass
import org.pih.warehouse.core.UnitOfMeasureType
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.importer.InventoryLevelExcelImporter
import org.pih.warehouse.inventory.Inventory
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.inventory.InventoryStatus
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductPackage

import java.text.SimpleDateFormat

class DataService {

    def dataSource
    def sessionFactory
    def userService

    def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP


    private void cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
        propertyInstanceMap.get().clear()
    }

    static transactional = true

    List executeQuery(String query) {
        return new Sql(dataSource).rows(query)
    }

    List executeQuery(String query, Map params) {
        return new Sql(dataSource).rows(query, params)
    }

    void executeStatement(String statement) {
        Sql sql = new Sql(dataSource)
        sql.withTransaction {
            try {
                def startTime = System.currentTimeMillis()
                log.info "Executing statement ${statement}"
                sql.execute(statement)
                log.info "Updated ${sql.updateCount} rows in " +  (System.currentTimeMillis() - startTime) + " ms"
                sql.commit()
            } catch (Exception e) {
                sql.rollback()
                log.error("Rollback due to error while executing statements: " + e.message, e)
            }
        }
    }

    void executeStatements(List statementList) {
        statementList.each { String statement ->
            executeStatement(statement)
        }
    }

    /**
     * Validate inventory levels
     */
    def validateInventoryLevels(ImportDataCommand command) {
        println "validate inventory levels " + command.filename
        validateInventoryLevels(command.location, command.filename)
    }

    /**
     *
     * @param location
     * @param fileName
     * @return
     */
    def validateInventoryLevels(location, fileName) {
        InventoryLevelExcelImporter importer = new InventoryLevelExcelImporter(fileName)
        def inventoryLevelList = importer.getData()
        inventoryLevelList.each { row ->
            if (!validateInventoryLevel(row)) {
                return false
            }
        }
        return true
    }

    def validateInventoryLevel(row) {
        row.each { key, value ->
            def expectedType = InventoryLevelExcelImporter.propertyMap.get(key).expectedType
            switch (expectedType) {
                case ExcelImportUtils.PROPERTY_TYPE_INT:
                    assert !value || value instanceof Number || value instanceof Boolean, "Value [${value}] for column [${key}] must be a Number or Boolean but was ${value?.class?.name} (" + row + ")."
                    break
                case ExcelImportUtils.PROPERTY_TYPE_DATE:
                    assert !value || value instanceof Date, "Value [${value}] for column [${key}] must be a Date but was ${value?.class?.name} (" + row + ")."
                    break
                default:
                    break
            }
        }
        return true
    }

    def importInventoryLevels(ImportDataCommand command) {
        println "Import inventory levels " + command.filename
        importInventoryLevels(command.location, command.filename)
    }


    /**
     * Import all inventory levels found in the file with the given fileName
     *
     * @param location
     * @param fileName
     * @return
     */
    def importInventoryLevels(location, filename) {
        println "Import inventory levels " + location + " filename " + filename
        InventoryLevelExcelImporter importer = new InventoryLevelExcelImporter(filename)
        def inventoryLevelList = importer.getData()
        inventoryLevelList.eachWithIndex { row, index ->
            if (validateInventoryLevel(row)) {
                importInventoryLevel(location, row, index)
            }
        }
        return inventoryLevelList
    }


    /**
     * Import a single row from the XLS file
     *
     * @param location
     * @param fileName
     * @return
     */
    def importInventoryLevel(location, row, index) {
        println "Import inventory levels " + location + " row " + row + " index " + index
        Product.withNewSession {

            def product = findProduct(row)

            // Modify product attributes (name, manufacturer, manufacturerCode, vendor, vendorCode, unitOfMeasure, etc)
            updateProduct(product, row)

            // Create inventory level for current location, include bin location
            if (location.inventory) {
                Location preferredBinLocation = null

                if (row.preferredBinLocation) {
                    preferredBinLocation = location.getBinLocations().find {
                        it.name.equalsIgnoreCase(row.preferredBinLocation.trim())
                    }

                    if (!preferredBinLocation) {
                        throw new RuntimeException("Bin location ${row.preferredBinLocation} was not found in current location")
                    }
                }

                addInventoryLevelToProduct(product, location.inventory, preferredBinLocation, row)
            }

            // Create product package if UOM and quantity are provided
            if (row.packageUom && row.packageSize) {
                addProductPackageToProduct(product, row.packageUom, row.packageSize, row.pricePerPackage)
            }

            // Save product
            product.save()

            // Clean up session after 50 products
            if (index % 50 == 0) {
                cleanUpGorm()
            }

        }

    }

    /**
     * Add inventory level to product
     *
     * @param product
     * @param inventory
     * @param preferredBinLocation
     * @param minQuantity
     * @param reorderQuantity
     * @param maxQuantity
     * @return
     */
    def addInventoryLevelToProduct(Product product, Inventory inventory, Location preferredBinLocation, Map row) {
        findOrCreateInventoryLevel(product, inventory, preferredBinLocation, row)
    }

    /**
     * Add product package to product
     *
     * @param product
     * @param uomCode
     * @param quantity
     * @param price
     * @return
     */
    def addProductPackageToProduct(product, uomCode, quantity, price) {
        println "Create or modify product package: " + uomCode + " " + quantity + " " + price
        if (uomCode) {
            def productPackage = findOrCreateProductPackage(product, uomCode, quantity as Integer, price as Float)
            product.addToPackages(productPackage)
        }
    }

    /**
     * Find or create a unit of measure with the given parameters.
     *
     * @param uomCode
     * @return
     */
    def findOrCreateUnitOfMeasure(uomCode) {
        def unitOfMeasure = UnitOfMeasure.findByCode(uomCode)
        if (!unitOfMeasure) {
            unitOfMeasure = new UnitOfMeasure()
            unitOfMeasure.uomClass = findOrCreateQuantityUnitOfMeasureClass()
            unitOfMeasure.code = uomCode
            unitOfMeasure.name = uomCode
            unitOfMeasure.description = uomCode
            unitOfMeasure.save()
        }
        unitOfMeasure = unitOfMeasure.merge()
        log.info "findOrCreateUnitOfMeasure: ${unitOfMeasure}"
        return unitOfMeasure

    }

    /**
     * Find or create a unit of measure class -- we only care about Quantity.
     *
     * @return
     */
    def findOrCreateQuantityUnitOfMeasureClass() {
        def unitOfMeasureClass = UnitOfMeasureClass.findByType(UnitOfMeasureType.QUANTITY)
        if (!unitOfMeasureClass) {
            unitOfMeasureClass = new UnitOfMeasureClass()
            unitOfMeasureClass.code = "QTY"
            unitOfMeasureClass.name = "Quantity"
            unitOfMeasureClass.description = "Quantity"
            unitOfMeasureClass.active = true
            unitOfMeasureClass.type = UnitOfMeasureType.QUANTITY
            unitOfMeasureClass.save()
        }
        log.info "findOrCreateUnitOfMeasureClass: ${unitOfMeasureClass}"
        unitOfMeasureClass = unitOfMeasureClass.merge()

        return unitOfMeasureClass
    }

    /**
     * Find or create an inventory level with the given parameters.
     *
     * @param product
     * @param inventory
     * @param preferredBinLocation
     * @param minQuantity
     * @param reorderQuantity
     * @param maxQuantity
     * @return
     */
    def findOrCreateInventoryLevel(Product product, Inventory inventory, Location preferredBinLocation, Map row) {

        log.info "Product ${product.productCode} inventory ${inventory} preferred ${row.preferredForReorder}"

        def inventoryLevel = InventoryLevel.findByProductAndInventory(product, inventory)
        if (!inventoryLevel) {
            inventoryLevel = new InventoryLevel()
            inventoryLevel.inventory = inventory
            product.addToInventoryLevels(inventoryLevel)

        }

        inventoryLevel.status = InventoryStatus.SUPPORTED
        inventoryLevel.preferredBinLocation = preferredBinLocation
        inventoryLevel.minQuantity = row.minQuantity
        inventoryLevel.reorderQuantity = row.reorderQuantity
        inventoryLevel.maxQuantity = row.maxQuantity
        inventoryLevel.preferred = Boolean.valueOf(row.preferredForReorder)
        inventoryLevel.expectedLeadTimeDays = row.expectedLeadTimeDays
        inventoryLevel.replenishmentPeriodDays = row.replenishmentPeriodDays

        return inventoryLevel
    }

    /**
     * Find or create a product package with the given parameters.
     *
     * @param product
     * @param uomCode
     * @param quantity
     * @param price
     * @return
     */
    def findOrCreateProductPackage(product, uomCode, quantity, price) {
        println "findOrCreateProductPackage: ${product} ${uomCode} ${quantity} ${price}"
        def unitOfMeasure = findOrCreateUnitOfMeasure(uomCode)
        def criteria = ProductPackage.createCriteria()
        def productPackage = criteria.get {
            eq('uom', unitOfMeasure)
            eq('product', product)
            eq('quantity', quantity)
        }

        if (!productPackage) {
            productPackage = new ProductPackage()
            productPackage.lastUpdated = new Date()
            productPackage.dateCreated = new Date()
        }
        productPackage.name = unitOfMeasure.code + "/" + quantity
        productPackage.description = unitOfMeasure.code + "/" + quantity
        productPackage.product = product
        productPackage.gtin = ""
        productPackage.uom = unitOfMeasure
        if (!productPackage.productPrice && price) {
            ProductPrice productPrice = new ProductPrice()
            productPrice.price = price
            productPrice.save()
            productPackage.productPrice = productPrice
        } else if (productPackage.productPrice && price) {
            productPackage.productPrice.price = price
        }
        productPackage.quantity = quantity ?: 1
        productPackage = productPackage.merge()

        log.info "findOrCreateProductPackage: ${productPackage}"


        return productPackage
    }

    /**
     * Find or create a product with the given parameters.
     *
     * @param product
     * @param uomCode
     * @param quantity
     * @param price
     * @return
     */
    def findOrCreateProduct(row) {
        def category = findOrCreateCategory(row.category)
        def product = Product.findByProductCode(row.productCode)
        if (!product) {
            println("Could not find product with product code " + row.productCode)
            product = new Product()
            product.category = category
            product.productCode = row.productCode
            product.name = row.productName
            product.manufacturer = row.manufacturer
            product.manufacturerCode = row.manufacturerCode
            product.vendor = row.vendor
            product.vendorCode = row.vendorCode
            product.unitOfMeasure = row.unitOfMeasure
            product.save()
        }
        product = product.merge()
        log.info "findOrCreateProduct: ${product}"
        return product
    }

    def findProduct(row) {
        def product = Product.findByProductCode(row.productCode)
        if (!product) {
            throw new RuntimeException("Product ${row.productCode} was not found")
        }
        return product
    }

    /**
     * Find or create a category with the given categoryName.
     *
     * @param product
     * @param uomCode
     * @param quantity
     * @param price
     * @return
     */
    def findOrCreateCategory(categoryName) {
        def category = Category.findByName(categoryName)
        if (!category) {
            println("Could not find category " + categoryName)
            category = new Category(name: categoryName, rootCategory: Category.getRootCategory())
            category.save()
        }
        log.info "findOrCreateCategory: ${category}"
        category = category.merge()
        return category
    }


    /**
     * Update an existing product with the data in the given row.
     *
     * @param product
     * @param uomCode
     * @param quantity
     * @param price
     * @return
     */
    def updateProduct(product, row) {
        // Change attributes other than name and category if they exist
        if (row.manufacturer) {
            product.manufacturer = row.manufacturer
        }
        if (row.manufacturerCode) {
            product.manufacturerCode = row.manufacturerCode
        }
        if (row.vendor) {
            product.vendor = row.vendor
        }
        if (row.vendorCode) {
            product.vendorCode = row.vendorCode
        }
        // If the user-entered unit price is different from the current unit price validate the user is allowed to make the change
        if (row.pricePerUnit) {
            Float pricePerUnit = getFloat(row.pricePerUnit)
            if (pricePerUnit != product.pricePerUnit) {
                userService.assertCurrentUserHasRoleFinance()
                product.pricePerUnit = pricePerUnit
            }
        }
    }

    /**
     * Should use the apache library to handle this.
     * @param str
     * @return
     */
    def getFloat(str) {
        try {
            return str.toFloat()
        } catch (NumberFormatException e) {
            log.error("Error converting string ${str} to float.")

            throw e
        }
        return 0.0
    }

    /**
     * Testing CSV data import functionality using Sql
     *
     * @return
     */
    def importData() {
        def sql = Sql.newInstance("jdbc:mysql://localhost:3306/mydb", "user", "pswd", "com.mysql.jdbc.Driver")
        def people = sql.dataSet("PERSON")
        new File("users.csv").splitEachLine(",") { fields ->
            people.add(
                    first_name: fields[0],
                    last_name: fields[1],
                    email: fields[2]
            )
        }
    }

    String exportInventoryLevels(Collection inventoryLevels) {
        def sw = new StringWriter()
        def csv = new CSVWriter(sw, {
            "Product Code" { it.productCode }
            "Product Name" { it.productName }
            "Inventory" { it.inventory }
            "Status" { it.status }
            "Bin Location" { it.binLocation }
            "Preferred" { it.preferred }
            "ABC Class" { it.abcClass }
            "Min Quantity" { it.minQuantity }
            "Reorder Quantity" { it.reorderQuantity }
            "Max Quantity" { it.maxQuantity }
            "Forecast Quantity" { it.forecastQuantity }
            "Forecast Period" { it.forecastPeriodDays }
            "UOM" { it.unitOfMeasure }
        })
        inventoryLevels.each { inventoryLevel ->
            csv << [
                    productCode       : inventoryLevel.product.productCode,
                    productName       : inventoryLevel.product.name,
                    inventory         : inventoryLevel.inventory.warehouse.name,
                    status            : inventoryLevel.status,
                    binLocation       : inventoryLevel.binLocation ?: "",
                    preferred         : inventoryLevel.preferred ?: "",
                    abcClass          : inventoryLevel.abcClass ?: "",
                    minQuantity       : inventoryLevel.minQuantity ?: "",
                    reorderQuantity   : inventoryLevel.reorderQuantity ?: "",
                    maxQuantity       : inventoryLevel.maxQuantity ?: "",
                    forecastQuantity  : inventoryLevel.forecastQuantity ?: "",
                    forecastPeriodDays: inventoryLevel.forecastPeriodDays ?: "",
                    unitOfMeasure     : inventoryLevel?.product?.unitOfMeasure ?: "EA"
            ]
        }
        return csv.writer.toString()
    }


    /**
     * Export the given requisitions to CSV.
     *
     * @param requisitions
     * @return
     */
    String exportRequisitions(requisitions) {
        def formatDate = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss")
        def sw = new StringWriter()

        def csvWriter = new CSVWriter(sw, {
            "Requisition Number" { it.requisitionNumber }
            "Status" { it.status }
            "Type" { it.type }
            "Class" { it.commodityClass }
            "Name" { it.name }
            "Origin" { it.origin }
            "Destination" { it.destination }

            "Requested by" { it?.requestedBy?.name ?: "" }
            "Date Requested" { it.dateRequested }

            "Verified" { it?.verifiedBy?.name ?: "" }
            "Date Verified" { it.dateVerified }

            "Picked" { it?.pickedBy?.name ?: "" }
            "Date Picked" { it.datePicked }

            "Checked" { it?.checkedBy?.name ?: "" }
            "Date Checked" { it.dateChecked }

            "Issued" { it?.issuedBy?.name ?: "" }
            "Date Issued" { it.dateIssued }

            "Created" { it?.createdBy?.name ?: "" }
            "Date Created" { it.dateCreated }

            "Updated" { it?.updatedBy?.name ?: "" }
            "Date Updated" { it.lastUpdated }
        })

        requisitions.each { requisition ->
            def row = [
                    requisitionNumber: requisition.requestNumber,
                    type             : requisition?.type,
                    commodityClass   : requisition?.commodityClass,
                    status           : requisition.status,
                    name             : requisition.name,
                    origin           : requisition.origin,
                    destination      : requisition.destination,

                    requestedBy      : requisition.requestedBy,
                    dateRequested    : requisition.dateRequested ? "${formatDate.format(requisition.dateRequested)}" : "",

                    reviewedBy       : requisition.reviewedBy,
                    dateReviewed     : requisition.dateReviewed ? "${formatDate.format(requisition.dateReviewed)}" : "",

                    verifiedBy       : requisition.verifiedBy,
                    dateVerified     : requisition.dateVerified ? "${formatDate.format(requisition.dateVerified)}" : "",

                    checkedBy        : requisition.checkedBy,
                    dateChecked      : requisition.dateChecked ? "${formatDate.format(requisition.dateChecked)}" : "",

                    deliveredBy      : requisition.deliveredBy,
                    dateDelivered    : requisition.dateDelivered ? "${formatDate.format(requisition.dateDelivered)}" : "",

                    pickedBy         : requisition?.picklist?.picker,
                    datePicked       : requisition?.picklist?.datePicked ? "${formatDate.format(requisition?.picklist?.datePicked)}" : "",

                    issuedBy         : requisition.issuedBy,
                    dateIssued       : requisition.dateIssued ? "${formatDate.format(requisition.dateIssued)}" : "",

                    receivedBy       : requisition.receivedBy,
                    dateReceived     : requisition.dateReceived ? "${formatDate.format(requisition.dateReceived)}" : "",

                    createdBy        : requisition.createdBy,
                    dateCreated      : requisition.dateCreated ? "${formatDate.format(requisition.dateCreated)}" : "",

                    updatedBy        : requisition.updatedBy,
                    lastUpdated      : requisition.lastUpdated ? "${formatDate.format(requisition.lastUpdated)}" : "",
            ]
            csvWriter << row
        }
        return sw.toString()
    }

    String exportRequisitionItems(requisitions) {
        def formatDate = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ss")
        def sw = new StringWriter()

        def csvWriter = new CSVWriter(sw, {
            "Requisition Number" { it.requisitionNumber }
            "Status" { it.status }
            "Type" { it.type }
            "Class" { it.commodityClass }
            "Name" { it.name }
            "Origin" { it.origin }
            "Destination" { it.destination }
            "Requested by" { it?.requestedBy?.name }
            "Date Requested" { it.dateRequested }
            "Product code" { it.productCode }
            "Product name" { it.productName }
            "Status" { it.itemStatus ?: "" }
            "Requested" { it.quantity ?: "" }
            "Approved" { it.quantityApproved ?: "" }
            "Picked" { it.quantityPicked ?: "" }
            "Canceled" { it.quantityCanceled ?: "" }
            "Reason Code" { it.reasonCode ?: "" }
            "Comments" { it.comments ?: "" }

        })

        requisitions.each { requisition ->
            requisition.requisitionItems.each { requisitionItem ->
                def row = [
                        requisitionNumber: requisition.requestNumber,
                        type             : requisition?.type,
                        commodityClass   : requisition?.commodityClass,
                        status           : requisition.status,
                        name             : requisition.name,
                        requestedBy      : requisition.requestedBy ?: "",
                        dateRequested    : requisition.dateRequested ? "${formatDate.format(requisition.dateRequested)}" : "",
                        origin           : requisition.origin,
                        destination      : requisition.destination,
                        productCode      : requisitionItem.product.productCode,
                        productName      : requisitionItem.product.name,
                        itemStatus       : requisitionItem.status,
                        quantity         : requisitionItem.quantity,
                        quantityCanceled : requisitionItem.quantityCanceled,
                        quantityApproved : requisitionItem.quantityApproved,
                        quantityPicked   : requisitionItem.calculateQuantityPicked(),
                        reasonCode       : requisitionItem.cancelReasonCode,
                        comments         : requisitionItem.cancelComments,


                ]
                csvWriter << row
            }
        }
        return sw.toString()
    }

    def transformObjects(List objects, List includeFields) {
        Map includeFieldsMap = includeFields.inject([:]) { result, includeField ->
            result[includeField] = includeField
            return result
        }

        transformObjects(objects, includeFieldsMap)
    }

    def transformObjects(List objects, Map includeFields) {
        objects.collect { object ->
            return transformObject(object, includeFields)
        }
    }

    def transformObject(Object object, Map includeFields) {
        Map properties = [:]
        includeFields.each { fieldName, element ->
            def value = null
            if (element instanceof LinkedHashMap) {
                value = element.property.tokenize('.').inject(object) { v, k -> v?."$k" }
                if (element.defaultValue && element.dateFormat && !value) {
                    value = element.defaultValue.format(element.dateFormat)
                } else if (element.dateFormat && value) {
                    value = value.format(element.dateFormat)
                } else if (element.defaultValue && !value) {
                    value = element.defaultValue
                }
                properties[fieldName] = value ?: ""
            } else {
                value = element.tokenize('.').inject(object) { v, k -> v?."$k" }
                properties[fieldName] = value ?: ""
            }
        }
        return properties
    }

    /**
     * Generic method to generate CSV string based on given csvrows map.
     * @param csvrows
     * @return
     */
    String generateCsv(csvrows) {
        def sw = new StringWriter()
        if (csvrows) {
            def columnHeaders = csvrows[0].keySet().collect { value -> StringEscapeUtils.escapeCsv(value) }
            sw.append(columnHeaders.join(",")).append("\n")
            csvrows.each { row ->
                def values = row.values().collect { value ->
                    if (value?.toString()?.isNumber()) {
                        value
                    } else {
                        StringEscapeUtils.escapeCsv(value.toString())
                    }
                }
                sw.append(values.join(","))
                sw.append("\n")
            }
        }
        return sw.toString()
    }

}
