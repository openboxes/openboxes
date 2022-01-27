/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.batch


import org.apache.poi.poifs.filesystem.OfficeXmlFileException
import org.pih.warehouse.core.Location
import org.pih.warehouse.importer.CategoryExcelImporter
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.importer.InventoryExcelImporter
import org.pih.warehouse.importer.InventoryLevelExcelImporter
import org.pih.warehouse.importer.LocationExcelImporter
import org.pih.warehouse.importer.OutboundStockMovementExcelImporter
import org.pih.warehouse.importer.PersonExcelImporter
import org.pih.warehouse.importer.ProductAttributeExcelImporter
import org.pih.warehouse.importer.ProductCatalogExcelImporter
import org.pih.warehouse.importer.ProductCatalogItemExcelImporter
import org.pih.warehouse.importer.ProductExcelImporter
import org.pih.warehouse.importer.ProductPackageExcelImporter
import org.pih.warehouse.importer.ProductSupplierAttributeImporter
import org.pih.warehouse.importer.ProductSupplierExcelImporter
import org.pih.warehouse.importer.ProductSupplierPreferenceImporter
import org.pih.warehouse.importer.TagExcelImporter
import org.pih.warehouse.importer.UserExcelImporter
import org.pih.warehouse.importer.UserLocationExcelImporter
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest


class BatchController {

    def dataService
    def documentService
    def inventoryService
    def grailsApplication
    def genericApiService
    def uploadService

    def index = {}

    def downloadExcel = {
        println "Download XLS template " + params

        def objects = genericApiService.getList(params.type, [:])
        def domainClass = genericApiService.getDomainClass(params.type)
        def data = dataService.transformObjects(objects, domainClass.PROPERTIES)

        response.contentType = "application/vnd.ms-excel"
        response.setHeader 'Content-disposition', "attachment; filename=\"${params.type}.xls\""
        documentService.generateExcel(response.outputStream, data)
        response.outputStream.flush()
    }

    def downloadTemplate = {
        println "Download XLS template " + params
        def filename = params.template
        try {
            def file = documentService.findFile("templates/" + filename)
            response.contentType = "application/vnd.ms-excel"
            response.setHeader 'Content-disposition', "attachment; filename=\"${filename}\""
            response.outputStream << file.bytes
            response.outputStream.flush()
        }
        catch (FileNotFoundException e) {
            response.status = 404
        }
    }

    def downloadCsvTemplate = {
        println "Download csv template " + params
        def filename = params.template
        try {
            def file = documentService.findFile("templates/" + filename)
            response.contentType = "text/csv"
            response.setHeader 'Content-disposition', "attachment; filename=\"${filename}\""
            response.outputStream << file.bytes
            response.outputStream.flush()
        }
        catch (FileNotFoundException e) {
            response.status = 404
        }
    }


    def importData = { ImportDataCommand command ->

        def xlsFile = session.xlsFile
        if ("POST".equals(request.getMethod())) {

            if (request instanceof DefaultMultipartHttpServletRequest) {
                xlsFile = request.getFile('xlsFile')
                if (!xlsFile?.empty) {
                    try {
                        session.xlsFile = xlsFile
                    } catch (Exception e) {
                        //throw new RuntimeException(e);
                        flash.message = "Unable to upload file due to exception: " + e.message
                        return
                    }
                } else {
                    flash.message = "${warehouse.message(code: 'inventoryItem.emptyFile.message')}"
                }
            }

            def dataImporter
            if (xlsFile) {
                log.info "Local xls file " + xlsFile
                command.importFile = xlsFile
                command.filename = xlsFile.name
                command.location = Location.get(session.warehouse.id)
                try {
                    // Need to choose the right importer
                    switch (command.type) {
                        case "category":
                            dataImporter = new CategoryExcelImporter(command?.filename, xlsFile.inputStream)
                            break
                        case "inventory":
                            dataImporter = new InventoryExcelImporter(command?.filename, xlsFile.inputStream)
                            break
                        case "inventoryLevel":
                            dataImporter = new InventoryLevelExcelImporter(command?.filename, xlsFile.inputStream)
                            break
                        case "location":
                            dataImporter = new LocationExcelImporter(command?.filename, xlsFile.inputStream)
                            break
                        case "person":
                            dataImporter = new PersonExcelImporter(command?.filename, xlsFile.inputStream)
                            break
                        case "product":
                            dataImporter = new ProductExcelImporter(command?.filename, xlsFile.inputStream)
                            break
                        case "productAttribute":
                            dataImporter = new ProductAttributeExcelImporter(command?.filename, xlsFile.inputStream)
                            break
                        case "productCatalog":
                            dataImporter = new ProductCatalogExcelImporter(command?.filename, xlsFile.inputStream)
                            break
                        case "productCatalogItem":
                            dataImporter = new ProductCatalogItemExcelImporter(command?.filename, xlsFile.inputStream)
                            break
                        case "productSupplier":
                            dataImporter = new ProductSupplierExcelImporter(command?.filename, xlsFile.inputStream)
                            break
                        case "productSupplierPreference":
                            dataImporter = new ProductSupplierPreferenceImporter(command?.filename, xlsFile.inputStream)
                            break
                        case "productSupplierAttribute":
                            dataImporter = new ProductSupplierAttributeImporter(command?.filename, xlsFile.inputStream)
                            break
                        case "productPackage":
                            dataImporter = new ProductPackageExcelImporter(command?.filename, xlsFile.inputStream)
                            break
                        case "outboundStockMovement":
                            dataImporter = new OutboundStockMovementExcelImporter(command?.filename, xlsFile.inputStream)
                            break
                        case "tag":
                            dataImporter = new TagExcelImporter(command?.filename, xlsFile.inputStream)
                            break
                        case "user":
                            dataImporter = new UserExcelImporter(command?.filename, xlsFile.inputStream)
                            break
                        case "userLocation":
                            dataImporter = new UserLocationExcelImporter(command?.filename, xlsFile.inputStream)
                            break
                        default:
                            command.errors.reject("type", "${warehouse.message(code: 'import.invalidType.message', default: 'Please choose a valid import type')}")
                    }
                }
                catch (OfficeXmlFileException e) {
                    log.error("Error with import file " + e.message, e)
                    command.errors.reject("importFile", e.message)
                }

                if (dataImporter) {

                    log.info "Using data importer ${dataImporter.class.name}"

                    // Get data from importer (should be done as a separate step 'processData' or within 'validateData')
                    command.data = dataImporter.data

                    // Validate data using importer (might change data)
                    dataImporter.validateData(command)

                    //command.data = dataImporter.data
                    command.columnMap = dataImporter.columnMap

                }


                if (command?.data?.isEmpty()) {
                    command.errors.reject("importFile", "${warehouse.message(code: 'inventoryItem.pleaseEnsureDate.message', args: [dataImporter.columnMap?.sheet?:'Sheet1', xlsFile.name])}")
                }

                if (command.type == 'inventory' && !command.date) {
                    command.errors.reject("date", "${warehouse.message(code: 'import.inventoryImportMustHaveDate.message', default: "Inventory import must specify the date of the stock count")}")
                }

                // If there are no errors and the user requests to import the data, we should execute the import
                if (!command.hasErrors() && params.import) {
                    log.info "Data is about to be imported ..."
                    try {

                        dataImporter.importData(command)

                        if (!command.errors.hasErrors()) {
                            session.removeAttribute("xlsFile")
                            flash.message = "${warehouse.message(code: 'inventoryItem.importSuccess.message', args: [xlsFile.name])}"
                            redirect(action: "importData")
                            return
                        }
                    } catch (Exception e) {
                        command.errors.reject(e.message)
                    }

                } else if (!command.hasErrors()) {
                    flash.message = "${warehouse.message(code: 'inventoryItem.dataReadyToBeImported.message')}"
                }
                render(view: "importData", model: [commandInstance: command])
            } else {
                flash.message = "${warehouse.message(code: 'inventoryItem.notValidXLSFile.message')}"
            }

        }
    }

}
