/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

import org.pih.warehouse.product.Product

class TagController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [tagInstanceList: Tag.list(params), tagInstanceTotal: Tag.count()]
    }

    def create = {
        def tagInstance = new Tag()
        tagInstance.properties = params
        return [tagInstance: tagInstance]
    }

    def save = {
        def tagInstance = new Tag(params)
        if (tagInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'tag.label', default: 'Tag'), tagInstance.id])}"
            redirect(action: "list", id: tagInstance.id)
        } else {
            render(view: "create", model: [tagInstance: tagInstance])
        }
    }

    def show = {
        def tagInstance = Tag.get(params.id)
        if (!tagInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'tag.label', default: 'Tag'), params.id])}"
            redirect(action: "list")
        } else {
            [tagInstance: tagInstance]
        }
    }

    def edit = {
        def tagInstance = Tag.get(params.id)
        if (!tagInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'tag.label', default: 'Tag'), params.id])}"
            redirect(action: "list")
        } else {
            return [tagInstance: tagInstance]
        }
    }

    def update = {
        def tagInstance = Tag.get(params.id)
        if (tagInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (tagInstance.version > version) {

                    tagInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'tag.label', default: 'Tag')] as Object[], "Another user has updated this Tag while you were editing")
                    render(view: "edit", model: [tagInstance: tagInstance])
                    return
                }
            }
            tagInstance.properties = params
            if (!tagInstance.hasErrors() && tagInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'tag.label', default: 'Tag'), tagInstance.id])}"
                redirect(action: "list", id: tagInstance.id)
            } else {
                render(view: "edit", model: [tagInstance: tagInstance])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'tag.label', default: 'Tag'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def tagInstance = Tag.get(params.id)
        if (tagInstance) {
            try {
                tagInstance.products.each { product ->
                    tagInstance.removeFromProducts(product)
                }
                tagInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'tag.label', default: 'Tag'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'tag.label', default: 'Tag'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'tag.label', default: 'Tag'), params.id])}"
            redirect(action: "list")
        }
    }

    def doSomething = {
        println "do something " + params
    }

    def addToProducts = {
        println "add to products " + params
        Tag tag = Tag.get(params.id)

        if (tag) {
            if (params.productCodesToBeAdded) {
                flash.message = "Added products " + params
                def productCodes = params.productCodesToBeAdded.split(",")
                productCodes.each { productCode ->
                    def product = Product.findByProductCodeLike(productCode)
                    if (!tag.products.contains(product)) {
                        tag.addToProducts(product)
                        tag.save(flush: true)
                    }
                }

            } else {
                flash.message = "Please enter at least one product code " + params

            }
        } else {
            flash.message = "Could not find tag with ID " + params.id
            redirect(action: "list")
        }

        redirect(action: "edit", id: tag.id)

    }

    def removeFromProducts = {
        println "remove from products " + params
        Tag tag = Tag.get(params.id)

        if (tag) {
            def productIds = params.list("product.id")
            if (productIds) {
                flash.message = "Removed product ids " + productIds
                productIds.each { productId ->
                    def product = Product.get(productId)
                    tag.removeFromProducts(product)
                    tag.save(flush: true)
                }
                flash.message = "Removed products " + productIds
            } else {
                flash.message = "Please choose at least one product to remove"
            }
        } else {
            flash.message = "Could not find tag with ID " + params.id
            redirect(action: "list")
        }

        redirect(action: "edit", id: tag.id)

    }


}
