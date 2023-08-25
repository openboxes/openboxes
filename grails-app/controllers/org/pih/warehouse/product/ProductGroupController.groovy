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

import grails.validation.ValidationException

// import grails.plugin.springcache.annotations.CacheFlush

class ProductGroupController {

    ProductService productService
    ProductGroupService productGroupService
    ProductGroupDataService productGroupDataService

    def index() {
        redirect(action: "list", params: params)
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)

        def productGroupTotal
        def productGroups = []

        if (params.q) {
            productGroups = ProductGroup.findAllByNameLike("%" + params.q + "%", params)
            productGroupTotal = ProductGroup.countByNameLike("%" + params.q + "%")
        } else {
            productGroups = ProductGroup.list(params)
            productGroupTotal = ProductGroup.count()
        }

        [productGroupInstanceList: productGroups, productGroupInstanceTotal: productGroupTotal]
    }

    def create() {
        def productGroupInstance = new ProductGroup()
        productGroupInstance.properties = params
        return [productGroupInstance: productGroupInstance]
    }

    // @CacheFlush("selectProductFamilyCache")
    def save() {
        println "Save " + params
        ProductGroup productGroupInstance = productGroupDataService.get(params.id)
        if (!productGroupInstance) {
            productGroupInstance = new ProductGroup(params)
        }
        List<Product> products = productService.getProducts(params['product.id'])
        products.each { product ->
            productGroupInstance.addToProducts(product)
        }

        try {
            productGroupDataService.save(productGroupInstance)
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), productGroupInstance.id])}"
            redirect(action: "edit", id: productGroupInstance.id)
        } catch (ValidationException e) {
            render(view: "create", model: [productGroupInstance: productGroupInstance])
        }
    }

    def show() {
        ProductGroup productGroupInstance = productGroupDataService.get(params.id)
        if (!productGroupInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), params.id])}"
            redirect(action: "list")
        } else {
            [productGroupInstance: productGroupInstance]
        }
    }

    def edit() {
        log.info "Edit product group: " + params

        ProductGroup productGroupInstance = productGroupDataService.get(params.id)
        if (!productGroupInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), params.id])}"
            redirect(action: "list")
        } else {
            productGroupInstance.properties = params
            log.info "category: " + productGroupInstance?.category?.name
            return [productGroupInstance: productGroupInstance]
        }
    }

    def addProducts() {

        ProductGroup productGroupInstance = productGroupDataService.get(params.id)
        if (productGroupInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (productGroupInstance.version > version) {

                    productGroupInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'productGroup.label', default: 'ProductGroup')] as Object[], "Another user has updated this ProductGroup while you were editing")
                    render(view: "edit", model: [productGroupInstance: productGroupInstance])
                    return
                }
            }
            productGroupInstance.properties = params

            log.info("Products to add " + params['product.id'])

            log.info("Products before " + productGroupInstance.products)

            List<Product> products = productService.getProducts(params['product.id'])
            println "Products: " + products
            products.each { product ->
                productGroupInstance.addToProducts(product)
            }

            log.info("Products after " + productGroupInstance.products)

            if (!productGroupInstance.hasErrors() && productGroupDataService.save(productGroupInstance)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), productGroupInstance.id])}"
                redirect(action: "edit", id: productGroupInstance.id)
            } else {
                render(view: "edit", model: [productGroupInstance: productGroupInstance])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), params.id])}"
            redirect(action: "list")
        }

    }

    // @CacheFlush("selectProductFamilyCache")
    def update() {
        log.info "Update product group " + params

        ProductGroup productGroupInstance = productGroupDataService.get(params.id)
        if (!productGroupInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), params.id])}"
            redirect(controller: "productGroup", action: "list")
            return
        }

        if (params.version) {
            def version = params.version.toLong()
            if (productGroupInstance.version > version) {

                productGroupInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'productGroup.label', default: 'ProductGroup')] as Object[], "Another user has updated this ProductGroup while you were editing")
                render(view: "edit", model: [productGroupInstance: productGroupInstance])
                return
            }
        }
        productGroupInstance.properties = params

        try {
            productGroupDataService.save(productGroupInstance)
            flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), productGroupInstance.id])}"
            redirect(controller: "productGroup", action: "list")
        } catch (ValidationException e) {
            println productGroupInstance.errors
            // Refresh the instance from db, to avoid returning to the view productGroupInstance
            // that is persisted in Hibernate session with the binded properties that didn't pass the validation
            productGroupInstance.refresh()
            render(view: "edit", model: [productGroupInstance: productGroupInstance])
        }
    }

    // @CacheFlush("selectProductFamilyCache")
    def delete() {
        ProductGroup productGroupInstance = productGroupDataService.get(params.id)
        if (productGroupInstance) {
            try {
                // Remove all products from the product group before deleting the product group
                def productIds = productGroupInstance?.products?.collect { it.id }
                productIds.each { productId ->
                    def product = Product.get(productId)
                    productGroupInstance.removeFromProducts(product)

                }
                productGroupDataService.delete(productGroupInstance.id)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productGroup.label', default: 'ProductGroup'), params.id])}"
            redirect(action: "list")
        }
    }

    /**
     * From the inventory browser.
     */
    def addToProductGroup() {
        ProductGroup productGroupInstance = new ProductGroup()
        productGroupInstance.properties = params
        productGroupInstance.products = productService.getProducts(params['product.id'])


        List<Category> categories = productGroupInstance.products.collect {
            it.category
        }

        categories = categories.unique()

        if (categories.size() > 1) {
            productGroupInstance.errors.rejectValue("category", "Product group must contain products from a single category")
            flash.message = "Please return to the <a href='javascript:history.go(-1)'>Inventory Browser</a> to choose products from a single category."
        }
        productGroupInstance.category = categories.get(0)

        List<ProductGroup> productGroups = ProductGroup.findAllByCategory(productGroupInstance.category)

        render(view: "create", model: [productGroupInstance: productGroupInstance, productGroups: productGroups])
    }

    /**
     * From the edit produt group page.
     */

    def removeProductsFromProductGroup() {
        ProductGroup productGroupInstance = productGroupDataService.get(params.id)
        List<Product> products = productService.getProducts(params['delete-product.id'])
        products.each { product ->
            productGroupInstance.removeFromProducts(product)
        }
        render(view: "edit", model: [productGroupInstance: productGroupInstance])
    }

    def addProductsToProductGroup() {
        ProductGroup productGroupInstance = productGroupDataService.get(params.id)
        List<Product> products = productService.getProducts(params['add-product.id'])
        products.each { product ->
            productGroupInstance.addToProducts(product)
        }
        render(view: "edit", model: [productGroupInstance: productGroupInstance])
    }


    /**
     * Add a product group to existing product
     *
     * @return
     */
    def addProductToProductGroup() {
        Boolean isProductFamily = params.boolean("isProductFamily") ?: false
        ProductGroup productGroup = null
        try {
            productGroup = productGroupService.addProductToProductGroup(params.id, params.product?.id, isProductFamily)
        } catch (IllegalArgumentException e) {
            productGroup = ProductGroup.read(params.id)
            flash.error = e.message
        }
        render(template: 'products', model: [productGroup: productGroup, products: isProductFamily ? productGroup?.siblings : productGroup?.products])
    }

    /**
     * Delete product group from database
     */
    def deleteProductFromProductGroup() {
        Boolean isProductFamily = params.boolean("isProductFamily") ?: false
        ProductGroup productGroup = productGroupDataService.get(params.id)
        Product product = Product.get(params?.product?.id)
        if (product && productGroup) {
            if (isProductFamily) {
                product.productFamily = null
            }
            else {
                product.removeFromProductGroups(productGroup)
                productGroup.removeFromProducts(product)
            }
            productService.saveProduct(product)
        } else {
            response.status = 404
        }

        render(template: 'products', model: [productGroup: productGroup, products: isProductFamily ? productGroup?.siblings : productGroup.products])
    }

}
