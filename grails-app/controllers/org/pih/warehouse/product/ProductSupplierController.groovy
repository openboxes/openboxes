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

class ProductSupplierController {

    def dataService
    def identifierService

    static allowedMethods = [save: "POST", update: "POST", delete: ["GET", "POST"]]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [productSupplierInstanceList: ProductSupplier.list(params), productSupplierInstanceTotal: ProductSupplier.count()]
    }

    def create = {
        def productSupplierInstance = new ProductSupplier()
        productSupplierInstance.properties = params
        return [productSupplierInstance: productSupplierInstance]
    }

    def save = {
        def productSupplierInstance = new ProductSupplier(params)

        if (!productSupplierInstance.code) {
            String prefix = productSupplierInstance?.product?.productCode
            productSupplierInstance.code = identifierService.generateProductSupplierIdentifier(prefix)
        }
        if (productSupplierInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'productSupplier.label', default: 'ProductSupplier'), productSupplierInstance.id])}"

            if (params.dialog) {
                redirect(controller: "product", action: "edit", id: productSupplierInstance?.product?.id)
            } else {
                redirect(action: "list", id: productSupplierInstance.id)
            }
        } else {
            render(view: "create", model: [productSupplierInstance: productSupplierInstance])
        }
    }

    def show = {
        def productSupplierInstance = ProductSupplier.get(params.id)
        if (!productSupplierInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productSupplier.label', default: 'ProductSupplier'), params.id])}"
            redirect(action: "list")
        } else {
            [productSupplierInstance: productSupplierInstance]
        }
    }

    def edit = {
        def productSupplierInstance = ProductSupplier.get(params.id)
        if (!productSupplierInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productSupplier.label', default: 'ProductSupplier'), params.id])}"
            redirect(action: "list")
        } else {
            return [productSupplierInstance: productSupplierInstance]
        }
    }

    def update = {
        def productSupplierInstance = ProductSupplier.get(params.id)
        if (productSupplierInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (productSupplierInstance.version > version) {

                    productSupplierInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'productSupplier.label', default: 'ProductSupplier')] as Object[], "Another user has updated this ProductSupplier while you were editing")
                    render(view: "edit", model: [productSupplierInstance: productSupplierInstance])
                    return
                }
            }
            productSupplierInstance.properties = params

            if (!productSupplierInstance.code) {
                String prefix = productSupplierInstance?.product?.productCode
                productSupplierInstance.code = identifierService.generateProductSupplierIdentifier(prefix)
            }

            if (!productSupplierInstance.hasErrors() && productSupplierInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'productSupplier.label', default: 'ProductSupplier'), productSupplierInstance.id])}"

                if (params.dialog) {
                    redirect(controller: "product", action: "edit", id: productSupplierInstance?.product?.id)
                } else {
                    redirect(action: "list", id: productSupplierInstance.id)
                }

            } else {
                render(view: "edit", model: [productSupplierInstance: productSupplierInstance])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productSupplier.label', default: 'ProductSupplier'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def productSupplierInstance = ProductSupplier.get(params.id)
        if (productSupplierInstance) {
            try {
                productSupplierInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'productSupplier.label', default: 'ProductSupplier'), params.id])}"
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                log.error("Unable to delete product supplier: " + e.message, e)
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'productSupplier.label', default: 'ProductSupplier'), params.id])}"
            }

            if (params.dialog) {
                redirect(controller: "product", action: "edit", id: productSupplierInstance?.product?.id)
            } else {
                redirect(action: "list", id: productSupplierInstance.id)
            }

        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productSupplier.label', default: 'ProductSupplier'), params.id])}"
            redirect(action: "list")
        }
    }

    def dialog = {
        log.info "Display dialog " + params
        def product = Product.get(params.product.id)
        def productSupplier = ProductSupplier.get(params.id)

        // If not found, initialize a new product supplier
        if (!productSupplier) {
            productSupplier = new ProductSupplier()
            productSupplier.product = product
        }
        render(template: "dialog", model: [productSupplier: productSupplier])
    }

    def export = {
        def productSuppliers = params.list("productSupplier.id") ?
                ProductSupplier.findAllByIdInList(params.list("productSupplier.id")) : ProductSupplier.list()
        def data = productSuppliers ? dataService.transformObjects(productSuppliers, ProductSupplier.PROPERTIES) : [[:]]
        response.setHeader("Content-disposition",
                "attachment; filename=\"ProductSuppliers-${new Date().format("yyyyMMdd-hhmmss")}.csv\"")
        response.contentType = "text/csv"
        render dataService.generateCsv(data)
    }
}
