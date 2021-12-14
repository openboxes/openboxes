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
import org.pih.warehouse.importer.CSVUtils
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.product.Category

class ProductsConfigurationApiController {
    def dataService
    def productService
    def userService

    def getCategoriesCount = {
        render([data: Category.count()] as JSON)
    }

    def downloadCategories = {
        def objects = Category.list()
        def data = dataService.transformObjects(objects, Category.CSV_PROPERTIES)
        response.setHeader('Content-disposition', 'attachment; filename="Categories.csv"')
        render(contentType: 'text/csv', text: CSVUtils.dumpMaps(data))
    }

    def importCategories = {
        productService.importCategories(params.categoryOption)

        render status: 200
    }

    def importCategoryCsv = { ImportDataCommand command ->
        try {
            productService.importCategoryCsv(command.importFile)
        } catch (Exception e) {
            response.status = 500
            render([errorCode: 500, errorMessage: e?.message ?: "An unknown error occurred during import"] as JSON)
            return
        }

        render status: 200
    }

    def downloadCategoryTemplate = {
        def csv = 'Category Name,Parent Category Name\n'
        response.setHeader('Content-disposition', 'attachment; filename="Category_template.csv"')
        render(contentType: 'text/csv', text: csv.toString())
    }

    def importProducts = {
        if (!userService.isSuperuser(session?.user)) {
            throw new Exception("You are not authorized to perform this action")
        }

        productService.importProductsFromConfig(params.productOption)

        render status: 200
    }

    def categoryOptions = {
        def categoryOptions = grailsApplication.config.openboxes.configurationWizard.categoryOptions

        render([data: categoryOptions] as JSON)
    }

    def productOptions = {
        def productOptions = grailsApplication.config.openboxes.configurationWizard.productOptions

        render([data: productOptions] as JSON)
    }
}
