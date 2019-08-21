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

import org.pih.warehouse.core.ApiException
import org.pih.warehouse.core.MailService
import org.pih.warehouse.inventory.InventoryService


class CreateProductController {

    MailService mailService
    ProductService productService
    InventoryService inventoryService

    def index = {
        redirect(action: "create")
    }


    def createFlow = {
        start {
            action {}
            on("success").to("search")
            on(Exception).to("error")
        }

        search {
            on("search") { ProductSearchCommand search ->
                log.info("search: " + params)
                if (search.hasErrors()) {
                    flash.message = "Validation exception"
                    flow.search = search
                    return error()
                }
                try {
                    productService.findGoogleProducts(search)
                } catch (ApiException e) {
                    flash.message = e.message
                    return error()
                }

                [search: search]

            }.to("results")
            on("search").to("search")
            on("back").to("start")
            on("cancel").to("cancel")
        }
        error {

        }
        results {
            on("search") { ProductSearchCommand search ->
                log.info("results: " + params)
                if (search.hasErrors()) {
                    flow.search = search
                    return error()
                }
                if (search.searchTerms) {
                    try {
                        productService.findGoogleProducts(search)
                    } catch (ApiException e) {
                        flash.message = e.message
                        return error()
                    }
                }

                [search: search]

            }.to("results")

            on("select") {
                log.info("select: " + params)
                def product = Product.get(params.id)
                if (!product) {


                    def googleResultsMap = [:]
                    flow.search.results.each { googleProduct ->
                        googleResultsMap[googleProduct.googleId] = googleProduct
                    }

                    if (googleResultsMap) {
                        product = googleResultsMap[params.id]
                    }

                }
                flow.product = product
            }.to("verify")
            on("verify").to("verify")
            on("search").to("search")
            on("results").to("results")
            on("back").to("search")
            on("next").to("verify")
            on("previousResults") { ProductSearchCommand search ->

                if (search.searchTerms) {
                    search?.startIndex -= 25
                    try {
                        productService.findGoogleProducts(search)
                    } catch (ApiException e) {
                        return error()
                    }
                }
                [search: search]
            }.to("results")
            on("nextResults") { ProductSearchCommand search ->
                if (search.searchTerms) {
                    search?.startIndex += 25
                    try {
                        productService.findGoogleProducts(search)
                    } catch (ApiException e) {
                        return error()
                    }
                }
                [search: search]
            }.to("results")

            on("cancel").to("cancel")
        }
        verify {
            on("next") { ProductDetailsCommand command ->
                log.info("VERIFY: next")

                log.info("flow.product.category: " + flow.product.category)
                log.info("flow.product.description: " + flow.product.description)
                log.info("flow.product.title: " + flow.product.title)
                log.info("flow.product.gtin: " + flow.product.gtin)
                log.info("command.category: " + command.category)
                log.info("command.description: " + command.description)
                log.info("command.title: " + command.title)
                log.info("command.gtin: " + command.gtin)

                flow.product = command
                if (flow.product.hasErrors()) {
                    flash.message = "Validation exception"
                    return error()
                }
            }.to("create")
            on("search").to("search")
            on("results").to("results")
            on("back").to("results")
            on("cancel").to("cancel")
        }
        create {
            on("next") {

                Product productInstance = new Product()
                productInstance.category = flow.product.category
                productInstance.name = flow.product.title
                productInstance.description = flow.product.description
                productInstance.upc = flow.product.gtin

                if (!productInstance.hasErrors() && productInstance.save(flush: true)) {
                    flash.message = "${message(code: 'success.adding.product')}"
                    log.info("Saved product " + productInstance?.name + " with ID " + productInstance?.id)
                    flow.productInstance = productInstance
                    //return success()
                } else {
                    log.info("Validation errors " + productInstance.errors)
                    flow.product.errors = productInstance.errors
                    return error()
                }

            }.to("finish")
            on("search").to("search")
            on("results").to("results")
            on("back").to("verify")
            on("cancel").to("cancel")

        }

        finish {
            // redirect to product edit page
            redirect(controller: "product", action: "edit", id: flow.productInstance.id)
        }

        cancel {
            //redirect to inventory browse page on cancel
            redirect(controller: "inventory", action: "browse")
        }
    }
}



