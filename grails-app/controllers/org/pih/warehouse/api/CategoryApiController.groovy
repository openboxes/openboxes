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

class CategoryApiController {

    def productService

    def list = {
        log.debug "List products " + params
        def categories = productService.getCategoryTree()
        categories = categories.collect { it.toJson() }
        render([data: categories] as JSON)
    }

    def read = {
        Category category = Category.get(params.id)
        if (!category) {
            throw new ObjectNotFoundException(params.id, "Category")
        }
        render category.toJson() as JSON
    }

    def save = {
        log.debug "Save category " + params
        def category = Category.get(params.id)
        if (!category) {
            category = new Category(request.JSON)
        } else {
            category.properties = params
        }

        if (!category.hasErrors() && category.save()) {
            render category.toJson() as JSON
        } else {
            throw new ValidationException("Unable to save category due to errors", category.errors)
        }
    }

    def delete = {
        def category = Category.get(params.id)
        if (!category) {
            throw new ObjectNotFoundException(params.id, "Category")
        } else {
            category.delete(flush: true)
            render status: 204
        }
    }
}
