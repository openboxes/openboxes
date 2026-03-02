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
        ProductAssociation productAssociationInstance = new ProductAssociation(params)
        if (params.hasMutualAssociation) {
            ProductAssociation mutualAssociationInstance = new ProductAssociation()
            bindMutualAssociationData(mutualAssociationInstance, params)

            mutualAssociationInstance.mutualAssociation = productAssociationInstance
            // Save the assocation before assigning it to the main instance, otherwise given the transaction commit that is proceeded in the controller
            // the mutual association instance might not be assigned to the productAssociationInstance after rendering the view
            mutualAssociationInstance.save(flush: true)
            productAssociationInstance.mutualAssociation = mutualAssociationInstance
        }
        productAssociationInstance.validate()
        if (!productAssociationInstance.hasErrors() && productAssociationInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'productAssociation.label', default: 'ProductAssociation'), productAssociationInstance.id])}"
            if (params.dialog) {
                redirect(controller: "product", action: "edit", id: productAssociationInstance?.product?.id)
                return
            }
            redirect(controller: "product", action: "edit", id: productAssociationInstance?.product?.id)
        } else {
            if (params.isFromProductEditPage) {
                chain(controller: "product", action: "edit", id: productAssociationInstance?.product?.id, model: [productAssociationInstance: productAssociationInstance])
                return
            }
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
            return [productAssociationInstance: productAssociationInstance, isFromProductEditPage: params.isFromProductEditPage]
        }
    }

    def update() {
        ProductAssociation productAssociationInstance = ProductAssociation.get(params.id)
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
                if (!mutualAssociationInstance.validate()) {
                    // Add mutual association errors to the main association so they can be displayed in the same view
                    mutualAssociationInstance.errors.allErrors.each { error ->
                        productAssociationInstance.errors.addError(error)
                    }
                    // Re-read product association to reset any changes made to it before rendering the view
                    // TODO: This all logic should be moved to a transactional service, as having it in the controller which is transactional
                    // causes the bug that even though we wouldn't update a productAssociationInstance, the rollback takes place after rendering the view
                    // Ideally the rollback should happen in the service so that when the instance comes back to the controller, all the dirty fields
                    // are reset to the original values and we don't have to manually refresh the instance here
                    productAssociationInstance.refresh()
                    if (params.isFromProductEditPage) {
                        chain(controller: "product", action: "edit", id: productAssociationInstance?.product?.id, model: [productAssociationInstance: productAssociationInstance])
                        return
                    }
                    render(view: "create", model: [productAssociationInstance: productAssociationInstance])
                    return
                }
                mutualAssociationInstance.save(flush: true, failOnError: true)
            } else if (productAssociationInstance.mutualAssociation && !params.hasMutualAssociation) {
                mutualAssociationInstance = productAssociationInstance.mutualAssociation
                productAssociationInstance.mutualAssociation = null
                mutualAssociationInstance.delete()
            }

            productAssociationInstance.properties = params
            if (!productAssociationInstance.hasErrors() && productAssociationInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'productAssociation.label', default: 'ProductAssociation'), productAssociationInstance.id])}"
                redirect(controller: "product", action: "edit", id: productAssociationInstance?.product?.id)

            } else {
                if (params.isFromProductEditPage) {
                    chain(controller: "product", action: "edit", id: productAssociationInstance?.product?.id, model: [productAssociationInstance: productAssociationInstance])
                    return
                }
                render(view: "create", model: [productAssociationInstance: productAssociationInstance])
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
        render(template: "dialog", model: [productAssociation: productAssociation, isFromProductEditPage: true])
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


}
