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
import grails.validation.ValidationException
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product

class ProductApiController {

    def productService

    def list = {
        log.info "List products " + params
		def products = productService.getProducts(null, null, params)
        products = products.collect { it.toJson() }
		render products as JSON
	}

    def read = {
        Product product = Product.findByIdOrProductCode(params.id, params.id)
        if (!product) {
            throw new ObjectNotFoundException(params.id, "Product")
        }
        render product.toJson() as JSON
    }

    def create = {
        log.info "Save category " + params
        Product product = Product.get(params.id)
        if (!product) {
            product = new Product(request.JSON)
        }
        else {
            product.properties = request.JSON;
        }

        if (!product.hasErrors() && product.save()) {
            render product.toJson() as JSON
        }
        else {
            throw new ValidationException("Unable to save product due to errors", product.errors)
        }
    }


    def delete = {

    }

}
