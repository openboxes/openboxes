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

import grails.core.GrailsApplication
import org.apache.poi.poifs.filesystem.OfficeXmlFileException
import org.pih.warehouse.core.Location
import org.pih.warehouse.importer.CategoryExcelImporter
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.importer.InventoryExcelImporter
import org.pih.warehouse.importer.InventoryLevelExcelImporter
import org.pih.warehouse.importer.LocationExcelImporter
import org.pih.warehouse.importer.PersonExcelImporter
import org.pih.warehouse.importer.ProductAttributeExcelImporter
import org.pih.warehouse.importer.ProductCatalogExcelImporter
import org.pih.warehouse.importer.ProductCatalogItemExcelImporter
import org.pih.warehouse.importer.ProductExcelImporter
import org.pih.warehouse.importer.ProductPackageExcelImporter
import org.pih.warehouse.importer.ProductSupplierExcelImporter
import org.pih.warehouse.importer.TagExcelImporter
import org.pih.warehouse.importer.UserExcelImporter
import org.pih.warehouse.importer.UserLocationExcelImporter
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest

class BatchController {

    def dataService
    def documentService
    def inventoryService
    GrailsApplication grailsApplication
    def genericApiService
    def uploadService

    def index() {}


    def uploadData(ImportDataCommand command) {
        if (request instanceof DefaultMultipartHttpServletRequest) {
            def uploadFile = request.getFile('xlsFile')
            if (!uploadFile.empty) {
                def localFile = uploadService.createLocalFile(uploadFile.originalFilename)
                uploadFile.transferTo(localFile)
            }
        }
    }


    def downloadExcel() {
        println "Download XLS template " + params

        def objects = genericApiService.getList(params.type, [:])
        def domainClass = genericApiService.getDomainClass(params.type)
        def data = dataService.transformObjects(objects, domainClass.PROPERTIES)

        response.contentType = "application/vnd.ms-excel"
        response.setHeader 'Content-disposition', "attachment; filename=\"${params.type}.xls\""
        documentService.generateExcel(response.outputStream, data)
        response.outputStream.flush()
    }

    def downloadTemplate() {
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

    def downloadCsvTemplate() {
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


    def importData(ImportDataCommand command) {

        if ("POST".equals(request.getMethod())) {
            def localFile = session.localFile
            if (request instanceof StandardMultipartHttpServletRequest) {
                def uploadFile = command.importFile
                if (!uploadFile?.empty) {
                    try {
                        localFile = uploadService.createLocalFile(uploadFile.originalFilename)
                        uploadFile.transferTo(localFile)
                        session.localFile = localFile

                    } catch (Exception e) {
                        log.error("Error uploading file" + e.message, e)
                        flash.message = "Unable to upload file due to exception: " + e.message
                        return
                    }
                } else {
                    flash.message = "${warehouse.message(code: 'inventoryItem.emptyFile.message')}"
                }
            }

            def dataImporter
            if (localFile) {
                log.info "Local xls file " + localFile.getAbsolutePath()
                command.filename = localFile.getAbsolutePath()
                command.location = Location.get(session.warehouse.id)
                try {
                    // Need to choose the right importer
                    switch (command.importType) {
                        case "category":
                            dataImporter = new CategoryExcelImporter(command?.filename)
                            break
                        case "inventory":
                            dataImporter = new InventoryExcelImporter(command?.filename)
                            break
                        case "inventoryLevel":
                            dataImporter = new InventoryLevelExcelImporter(command?.filename)
                            break
                        case "location":
                            dataImporter = new LocationExcelImporter(command?.filename)
                            break
                        case "person":
                            dataImporter = new PersonExcelImporter(command?.filename)
                            break
                        case "product":
                            dataImporter = new ProductExcelImporter(command?.filename)
                            break
                        case "productAttribute":
                            dataImporter = new ProductAttributeExcelImporter(command?.filename)
                            break
                        case "productCatalog":
                            dataImporter = new ProductCatalogExcelImporter(command?.filename)
                            break
                        case "productCatalogItem":
                            dataImporter = new ProductCatalogItemExcelImporter(command?.filename)
                            break
                        case "productSupplier":
                            dataImporter = new ProductSupplierExcelImporter(command?.filename)
                            break
                        case "productPackage":
                            dataImporter = new ProductPackageExcelImporter(command?.filename)
                            break
                        case "tag":
                            dataImporter = new TagExcelImporter(command?.filename)
                            break
                        case "user":
                            dataImporter = new UserExcelImporter(command?.filename)
                            break
                        case "userLocation":
                            dataImporter = new UserLocationExcelImporter(command?.filename)
                            break
                        default:
                            command.errors.reject("importType", "${warehouse.message(code: 'import.invalidType.message', default: 'Please choose a valid import type')}")
                    }
                }
                catch (OfficeXmlFileException e) {
                    log.error("Error with import file " + e.message, e)
                    command.errors.reject("importFile", e.message)
                }

                if (dataImporter) {
                    // Get data from importer (should be done as a separate step 'processData' or within 'validateData')
                    command.data = dataImporter.data

                    // Validate data using importer (might change data)
                    dataImporter.validateData(command)

                    command.columnMap = dataImporter.columnMap
                }


                if (command?.data?.isEmpty()) {
                    command.errors.reject("importFile", "${warehouse.message(code: 'inventoryItem.pleaseEnsureDate.message', args: [dataImporter.columnMap?.sheet?:'Sheet1', localFile.getAbsolutePath()])}")
                }

                if (command.importType == 'inventory' && !command.date) {
                    command.errors.reject("date", "${warehouse.message(code: 'import.inventoryImportMustHaveDate.message', default: "Inventory import must specify the date of the stock count")}")
                }

                // If there are no errors and the user requests to import the data, we should execute the import
                if (command.importNow) {
                    try {
                        dataImporter.importData(command)
                    } catch(Exception e) {
                        log.error("Unable to import data: " + e.message, e)
                        command.errors.reject(e.message)
                    }
                    if (!command.hasErrors()) {
                        flash.message = "${warehouse.message(code: 'inventoryItem.importSuccess.message', args: [localFile.getAbsolutePath()])}"
                        // Remove once import has been completed
                        session.removeAttribute("localFile")
                        redirect(action: "importData")
                        return
                    }
                } else if (!command.hasErrors()) {
                    flash.message = "${warehouse.message(code: 'inventoryItem.dataReadyToBeImported.message')}"
                }
            } else {
                flash.message = "${warehouse.message(code: 'inventoryItem.notValidXLSFile.message')}"
            }
            // Render data for user to review before proceeding
            render(view: "importData", model: [commandInstance:command])
        }
    }

}
