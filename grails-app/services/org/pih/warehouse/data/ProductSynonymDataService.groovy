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

import grails.core.GrailsApplication
import org.pih.warehouse.core.Synonym
import org.pih.warehouse.core.SynonymTypeCode
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.product.Product

class ProductSynonymDataService {

    def productService
    GrailsApplication grailsApplication

    Boolean validateData(ImportDataCommand command) {
        log.info "Validate data " + command.filename

        // List to store product codes and a locale that we've already added a DISPLAY_NAME synonym for during this one import
        List<Map<String, Object>> productsWithDisplayName = []

        command.data.eachWithIndex { params, index ->
            // check for required fields
            Synonym.PROPERTIES.each {columnName, paramAccessor ->
                if(!params[paramAccessor]) {
                    command.errors.reject("Row ${index + 1}: '${columnName?.toLowerCase()}' is required")
                }
            }

            Product product = null
            SynonymTypeCode synonymTypeCode = null

            if (params['product.productCode']) {
                product = Product.findByProductCode(params['product.productCode'])
                if (!product) {
                    command.errors.reject("Row ${index + 1}: Product with code '${params['product.productCode']}' does not exist")
                }
            }

            if (params['synonymTypeCode']) {
                params['synonymTypeCode'] = params['synonymTypeCode']?.toUpperCase()
                try {
                    synonymTypeCode = SynonymTypeCode.valueOf(params['synonymTypeCode'])
                } catch (IllegalArgumentException ex) {
                    command.errors.reject("Row ${index + 1}: Synonym type code '${params['synonymTypeCode']} does not exist")
                }
            }

            if (params['locale']) {
                List<String> supportedLocales = grailsApplication.config.openboxes.locale.supportedLocales
                String foundLocale = supportedLocales.find {
                    it.toLowerCase() == params['locale']?.toLowerCase() || new Locale(it).displayName?.toLowerCase() == params['locale']?.toLowerCase()
                }
                if (foundLocale) {
                    params['locale'] = foundLocale
                } else {
                    command.errors.reject("Row ${index + 1}: Locale '${params['locale']}' is not a supported locale")
                }

            }

            // Check if none error occures to be sure, that we have correct product, locale and synonymTypeCode
            // before we check the duplicates
            if (!command.errors.allErrors && synonymTypeCode == SynonymTypeCode.DISPLAY_NAME) {
                Synonym duplicate = product?.synonyms?.find { Synonym synonym ->
                    synonym.locale == new Locale(params['locale']) && synonym.synonymTypeCode == SynonymTypeCode.DISPLAY_NAME
                }
                // If the product already has a synonym of type DISPLAY_NAME and a locale
                // or we are trying to add it in any of the line above for this single import, throw a validation error
                if (duplicate || productsWithDisplayName.find{ it?.code == product?.productCode && it?.locale == params['locale'] }) {
                    command.errors.reject("Row ${index + 1}: You cannot add multiple display names in the same language. Edit the existing synonym instead.")
                    return
                }
                productsWithDisplayName.add([code: product?.productCode, locale: params['locale']])
            }

        }
    }

    void importData(ImportDataCommand command) {
        log.info "Import data " + command.filename

        command.data.each{ params ->
            Product product = Product.findByProductCode(params['product.productCode'])
            productService.addSynonymToProduct(product.id, params['synonymTypeCode'], params['name'], params['locale'])
        }
    }

}
