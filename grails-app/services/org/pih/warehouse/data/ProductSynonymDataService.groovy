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

import org.pih.warehouse.core.Synonym
import org.pih.warehouse.core.SynonymTypeCode
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.product.Product

class ProductSynonymDataService {

    def grailsApplication

    def importData(ImportDataCommand command) {
        def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
        log.info "Validate data " + command.filename

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

            // If none validation error occured yet, try to add a synonym
            if (!command.errors.allErrors) {
                Synonym synonym = new Synonym(name: params['name'], locale: new Locale(params['locale']), synonymTypeCode: params['synonymTypeCode'])
                product.addToSynonyms(synonym)
                if (!synonym.validate() || !product.save(flush: true, failOnError: true)) {
                    command.errors.reject("Row ${index + 1}: Validation error occured. Most probably you are trying to add multiple DISPLAY_NAME synonym for one locale")
                }
            }

        }
    }

}
