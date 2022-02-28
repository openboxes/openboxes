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

class ProductsConfigurationApiController {
    def productService
    def userService
    def grailsApplication

    def importCategories = {
        if (!userService.isSuperuser(session?.user)) {
            throw new Exception("You are not authorized to perform this action")
        }

        productService.importCategories(params.categoryOption)
        return 200
    }

    def importProducts = {
        if (!userService.isSuperuser(session?.user)) {
            throw new Exception("You are not authorized to perform this action")
        }

        productService.importProductsFromConfig(params.productOption)
        return 200
    }

    def categoryOptions = {
        def categoryOptions = grailsApplication.config.openboxes.configurationWizard.categoryOptions

        render([data: categoryOptions] as JSON)
    }
}
