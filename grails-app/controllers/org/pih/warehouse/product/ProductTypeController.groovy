/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.product

import org.springframework.dao.DataIntegrityViolationException

class ProductTypeController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [productTypeInstanceList: ProductType.list(params), productTypeInstanceTotal: ProductType.count()]
    }

    def create = {
        def productTypeInstance = new ProductType(productTypeCode: ProductTypeCode.GOOD,
                requiredFields: [ProductField.PRODUCT_CODE, ProductField.NAME, ProductField.CATEGORY, ProductField.GL_ACCOUNT])
        productTypeInstance.properties = params
        return [productTypeInstance: productTypeInstance]
    }

    def save = {
        if (params.supportedActivities) {
            params.supportedActivities = params.list("supportedActivities") as ProductActivityCode[]
        }
        if (params.requiredFields) {
            params.requiredFields = params.list("requiredFields") as ProductField[]
        }
        if (params.displayedFields) {
            params.displayedFields = params.list("displayedFields") as ProductField[]
        }

        def productTypeInstance = new ProductType(params)
        productTypeInstance.productTypeCode = ProductTypeCode.GOOD
        productTypeInstance.requiredFields = [ProductField.PRODUCT_CODE, ProductField.NAME, ProductField.CATEGORY, ProductField.GL_ACCOUNT]
        if (productTypeInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'productType.label', default: 'ProductType'), productTypeInstance.id])}"
            redirect(action: "list", id: productTypeInstance.id)
        }
        else {
            render(view: "create", model: [productTypeInstance: productTypeInstance])
        }
    }

    def show = {
        def productTypeInstance = ProductType.get(params.id)
        if (!productTypeInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productType.label', default: 'ProductType'), params.id])}"
            redirect(action: "list")
        }
        else {
            [productTypeInstance: productTypeInstance]
        }
    }

    def edit = {
        def productTypeInstance = ProductType.get(params.id)
        if (!productTypeInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productType.label', default: 'ProductType'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [productTypeInstance: productTypeInstance]
        }
    }

    def update = {
        def productTypeInstance = ProductType.get(params.id)
        if (productTypeInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (productTypeInstance.version > version) {

                    productTypeInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'productType.label', default: 'ProductType')] as Object[], "Another user has updated this ProductType while you were editing")
                    render(view: "edit", model: [productTypeInstance: productTypeInstance])
                    return
                }
            }
            if (params.supportedActivities) {
                params.supportedActivities = params.list("supportedActivities") as ProductActivityCode[]
            }
            if (params.requiredFields) {
                params.requiredFields = params.list("requiredFields") as ProductField[]
            }
            if (params.displayedFields) {
                params.displayedFields = params.list("displayedFields") as ProductField[]
            }
            productTypeInstance.properties = params
            if (!productTypeInstance.hasErrors() && productTypeInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'productType.label', default: 'ProductType'), productTypeInstance.id])}"
                redirect(action: "list", id: productTypeInstance.id)
            }
            else {
                render(view: "edit", model: [productTypeInstance: productTypeInstance])
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productType.label', default: 'ProductType'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def productTypeInstance = ProductType.get(params.id)
        if (productTypeInstance) {
            def existingProducts = Product.countByProductType(productTypeInstance)
            if (existingProducts) {
                flash.message = "${warehouse.message(code: 'productType.deleteWithExistingProducts.message')}"
                redirect(action: "list", id: params.id)
            }

            try {
                productTypeInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'productType.label', default: 'ProductType'), params.id])}"
                redirect(action: "list")
            }
            catch (DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'productType.label', default: 'ProductType'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productType.label', default: 'ProductType'), params.id])}"
            redirect(action: "list")
        }
    }
}
