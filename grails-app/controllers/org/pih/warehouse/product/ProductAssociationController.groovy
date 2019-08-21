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

import grails.orm.PagedResultList

class ProductAssociationController {

    def dataService
    def productService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)

        def terms = params.q ? params?.q?.split(" ") : null
        def products = terms ? productService.searchProducts(terms, null) : []
        def selectedTypes = params.list("code").collect { it as ProductAssociationTypeCode }

        // Remove paging parameters if user is downloading CSV export
        if (params.format) {
            params.remove("max")
            params.remove("offset")
        }

        PagedResultList productAssociations = ProductAssociation.createCriteria().list(params) {
            if (selectedTypes) {
                'in'("code", selectedTypes)
            }
            or {
                ilike("id", params.q + "%")
                if (products) {
                    or {
                        'in'("product", products)
                        'in'("associatedProduct", products)
                    }
                }
            }
        }

        if (params.format && productAssociations) {
            def filename = "productAssociations.csv"
            def data = productAssociations ? dataService.transformObjects(productAssociations, ProductAssociation.PROPERTIES) : [[:]]
            def text = dataService.generateCsv(data)
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: "text/csv", text: text)
            return
        }

        [productAssociationInstanceList: productAssociations, productAssociationInstanceTotal: productAssociations.totalCount, selectedTypes: selectedTypes]
    }

    def create = {
        def productAssociationInstance = new ProductAssociation()
        productAssociationInstance.properties = params
        return [productAssociationInstance: productAssociationInstance]
    }

    def save = {
        def productAssociationInstance = new ProductAssociation(params)
        if (productAssociationInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'productAssociation.label', default: 'ProductAssociation'), productAssociationInstance.id])}"

            if (params.dialog) {
                redirect(controller: "product", action: "edit", id: productAssociationInstance?.product?.id)
                return
            }

            redirect(controller: "product", action: "edit", id: productAssociationInstance?.product?.id)
        } else {
            render(view: "create", model: [productAssociationInstance: productAssociationInstance])
        }
    }

    def show = {
        def productAssociationInstance = ProductAssociation.get(params.id)
        if (!productAssociationInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productAssociation.label', default: 'ProductAssociation'), params.id])}"
            redirect(action: "list")
        } else {
            [productAssociationInstance: productAssociationInstance]
        }
    }

    def edit = {
        def productAssociationInstance = ProductAssociation.get(params.id)
        if (!productAssociationInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productAssociation.label', default: 'ProductAssociation'), params.id])}"
            redirect(action: "list")
        } else {
            return [productAssociationInstance: productAssociationInstance]
        }
    }

    def update = {
        def productAssociationInstance = ProductAssociation.get(params.id)
        if (productAssociationInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (productAssociationInstance.version > version) {

                    productAssociationInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'productAssociation.label', default: 'ProductAssociation')] as Object[], "Another user has updated this ProductAssociation while you were editing")
                    render(view: "edit", model: [productAssociationInstance: productAssociationInstance])
                    return
                }
            }
            productAssociationInstance.properties = params
            if (!productAssociationInstance.hasErrors() && productAssociationInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'productAssociation.label', default: 'ProductAssociation'), productAssociationInstance.id])}"
                redirect(controller: "product", action: "edit", id: productAssociationInstance?.product?.id)

            } else {
                render(view: "edit", model: [productAssociationInstance: productAssociationInstance])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productAssociation.label', default: 'ProductAssociation'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def productAssociationInstance = ProductAssociation.get(params.id)
        if (productAssociationInstance) {
            try {
                productAssociationInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'productAssociation.label', default: 'ProductAssociation'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'productAssociation.label', default: 'ProductAssociation'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productAssociation.label', default: 'ProductAssociation'), params.id])}"
            redirect(action: "list")
        }
    }


    def dialog = {
        log.info "Display dialog " + params
        def product = Product.get(params.product.id)
        def productAssociation = ProductAssociation.get(params.id)

        // If not found, initialize a new product supplier
        if (!productAssociation) {
            productAssociation = new ProductAssociation()
            productAssociation.code = ProductAssociationTypeCode.SUBSTITUTE
            productAssociation.product = product
        }
        render(template: "dialog", model: [productAssociation: productAssociation])
    }


    def export = {
        def productAssociations = ProductAssociation.list()
        def data = productAssociations ? dataService.transformObjects(productAssociations, ProductAssociation.PROPERTIES) : [[:]]
        response.setHeader("Content-disposition",
                "attachment; filename=\"productAssociations.csv\"")
        response.contentType = "text/csv"
        render dataService.generateCsv(data)
    }
}
