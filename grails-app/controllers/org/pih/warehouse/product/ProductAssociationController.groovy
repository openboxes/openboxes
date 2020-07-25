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

import grails.gorm.transactions.Transactional

@Transactional
class ProductAssociationController {

    def dataService
    def documentService
    def productService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)

        def terms = params.q ? params?.q?.split(" ") : null
        def products = terms ? productService.searchProducts(terms, null) : []
        def selectedTypes = params.list("code").collect { it as ProductAssociationTypeCode }

        // Remove paging parameters if user is downloading CSV export
        if (params.format) {
            params.remove("max")
            params.remove("offset")
        }

        def productAssociations = ProductAssociation.createCriteria().list(params) {
            if (selectedTypes) {
                'in'("code", selectedTypes)
            }
            or {
                if (params.q) {
                    ilike("id", params.q + "%")
                }
                if (products) {
                    or {
                        'in'("product", products)
                        'in'("associatedProduct", products)
                    }
                }
            }
        }

        if (params.format && productAssociations) {
            String filename = "productAssociations"
            def data = dataService.transformObjects(productAssociations, ProductAssociation.PROPERTIES)

            if (params.format == 'csv') {
                filename = "${filename}.csv"
                def text = dataService.generateCsv(data)
                response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
                render(contentType: "text/csv", text: text)
                return
            } else if (params.format == 'xls') {
                filename = "${filename}.xls"
                response.contentType = "application/vnd.ms-excel"
                response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
                documentService.generateExcel(response.outputStream, data)
                response.outputStream.flush()
            }

        }

        [productAssociationInstanceList: productAssociations, productAssociationInstanceTotal: productAssociations.totalCount, selectedTypes: selectedTypes]
    }

    def create() {
        def productAssociationInstance = new ProductAssociation()
        productAssociationInstance.properties = params
        return [productAssociationInstance: productAssociationInstance]
    }

    def save() {
        def productAssociationInstance = new ProductAssociation(params)
        validateAssociation(productAssociationInstance)
        if (!productAssociationInstance.hasErrors() && productAssociationInstance.save(flush: true)) {
            if (params.hasMutualAssociation) {
                def mutualAssociationInstance = new ProductAssociation()
                bindMutualAssociationData(mutualAssociationInstance, params)

                mutualAssociationInstance.mutualAssociation = productAssociationInstance
                productAssociationInstance.mutualAssociation = mutualAssociationInstance

                mutualAssociationInstance.save(flush: true, failOnError: true)
            }
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

    def show() {
        def productAssociationInstance = ProductAssociation.get(params.id)
        if (!productAssociationInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productAssociation.label', default: 'ProductAssociation'), params.id])}"
            redirect(action: "list")
        } else {
            [productAssociationInstance: productAssociationInstance]
        }
    }

    def edit() {
        def productAssociationInstance = ProductAssociation.get(params.id)
        if (!productAssociationInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productAssociation.label', default: 'ProductAssociation'), params.id])}"
            redirect(action: "list")
        } else {
            return [productAssociationInstance: productAssociationInstance]
        }
    }

    def update() {
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

            def mutualAssociationInstance
            if (params.hasMutualAssociation) {
                if (productAssociationInstance.mutualAssociation) {
                    mutualAssociationInstance = productAssociationInstance.mutualAssociation
                } else {
                    mutualAssociationInstance = new ProductAssociation()

                    mutualAssociationInstance.mutualAssociation = productAssociationInstance
                    productAssociationInstance.mutualAssociation = mutualAssociationInstance
                }

                bindMutualAssociationData(mutualAssociationInstance, params)
                mutualAssociationInstance.save(flush: true, failOnError: true)
            } else if (productAssociationInstance.mutualAssociation && !params.hasMutualAssociation) {
                mutualAssociationInstance = productAssociationInstance.mutualAssociation
                productAssociationInstance.mutualAssociation = null
                mutualAssociationInstance.delete()
            }

            productAssociationInstance.properties = params
            validateAssociation(productAssociationInstance)
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

    def delete() {
        def productAssociationInstance = ProductAssociation.get(params.id)
        if (productAssociationInstance) {
            try {
                if (productAssociationInstance.mutualAssociation) {
                    ProductAssociation mutualAssociation = ProductAssociation.get(productAssociationInstance.mutualAssociation.id)
                    mutualAssociation.mutualAssociation = null
                    productAssociationInstance.mutualAssociation = null
                    if (Boolean.valueOf(params.mutualDelete)) {
                        mutualAssociation.delete()
                    } else {
                        mutualAssociation.save()
                    }
                }
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


    def dialog() {
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


    def export() {
        def productAssociations = ProductAssociation.list()
        def data = productAssociations ? dataService.transformObjects(productAssociations, ProductAssociation.PROPERTIES) : [[:]]
        response.setHeader("Content-disposition",
                "attachment; filename=\"productAssociations.csv\"")
        response.contentType = "text/csv"
        render dataService.generateCsv(data)
    }

    void bindMutualAssociationData(ProductAssociation mutualAssociation, Map params) {
        mutualAssociation.product = Product.get(params.associatedProduct.id)
        mutualAssociation.associatedProduct = Product.get(params.product.id)
        def quantity = params.quantity as Integer
        mutualAssociation.quantity = quantity != 0 ? (1 / quantity) : 0 as BigDecimal
        mutualAssociation.code = ProductAssociationTypeCode.valueOf(ProductAssociationTypeCode, params.code)
        mutualAssociation.comments = params.comments
    }

    void validateAssociation(ProductAssociation productAssociation) {
        // associate to itself
        if (productAssociation.product?.id == productAssociation.associatedProduct?.id) {
            productAssociation.errors.reject("Cannot associate a product with itself")
        }
        // duplicates
        List<ProductAssociation> foundProductAssociations = ProductAssociation.findAllWhere([
                product             : productAssociation.product,
                associatedProduct   : productAssociation.associatedProduct,
                code                : productAssociation.code,
        ])
        if (foundProductAssociations && foundProductAssociations.size() > 0) {
            productAssociation.errors.reject("Association with given parameters already exists")
        }
    }
}
