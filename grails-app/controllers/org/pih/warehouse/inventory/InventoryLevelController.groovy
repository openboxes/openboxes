/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.inventory

import grails.orm.PagedResultList
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

class InventoryLevelController {

    def productService
    def dataService

    static allowedMethods = [save: "POST", update: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)

        def terms = params.q ? params?.q?.split(" ") : null
        def products = terms ? productService.searchProducts(terms, null) : []
        def location = Location.get(params?.location?.id)

        // Remove paging parameters if user is downloading CSV export
        if (params.format) {
            params.remove("max")
            params.remove("offset")
        }

        PagedResultList inventoryLevels = InventoryLevel.createCriteria().list(params) {
            if (location?.inventory) {
                eq("inventory", location.inventory)
            }
            if (products) {
                'in'("product", products)
            }
        }

        if (params.format && inventoryLevels) {
            def filename = "inventoryLevels.csv"
            String text = dataService.exportInventoryLevels(inventoryLevels)
            response.contentType = "text/csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: "text/csv", text: text)
            return
        }

        [inventoryLevelInstanceList: inventoryLevels, inventoryLevelInstanceTotal: inventoryLevels?.totalCount]
    }

    def create = {
        def inventoryLevelInstance = new InventoryLevel()
        inventoryLevelInstance.properties = params
        return [inventoryLevelInstance: inventoryLevelInstance]
    }

    def save = {
        def location = Location.get(params.location.id)
        def inventoryLevelInstance = new InventoryLevel(params)
        inventoryLevelInstance.inventory = location.inventory
        if (inventoryLevelInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel'), inventoryLevelInstance.id])}"
            redirect(controller: "product", action: "edit", id: inventoryLevelInstance?.product?.id)
        } else {
            render(view: "create", model: [inventoryLevelInstance: inventoryLevelInstance])
        }
    }

    def show = {
        def inventoryLevelInstance = InventoryLevel.get(params.id)
        if (!inventoryLevelInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel'), params.id])}"
            redirect(action: "list")
        } else {
            [inventoryLevelInstance: inventoryLevelInstance]
        }
    }

    def edit = {
        def inventoryLevelInstance = InventoryLevel.get(params.id)

        if (!inventoryLevelInstance) {
            def productInstance = Product.get(params.id)
            inventoryLevelInstance = InventoryLevel.findByProduct(productInstance)
        }
        if (!inventoryLevelInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel'), params.id])}"
            redirect(action: "create")
        } else {
            return [inventoryLevelInstance: inventoryLevelInstance]
        }
    }

    def update = {
        def inventoryLevelInstance = InventoryLevel.get(params.id)
        if (inventoryLevelInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (inventoryLevelInstance.version > version) {
                    inventoryLevelInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel')] as Object[], "Another user has updated this InventoryLevel while you were editing")
                    render(view: "edit", model: [inventoryLevelInstance: inventoryLevelInstance])
                    return
                }
            }
            inventoryLevelInstance.properties = params
            if (!inventoryLevelInstance.hasErrors() && inventoryLevelInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel'), inventoryLevelInstance.id])}"

                // FIXME Should do this in a filter
                if (params.redirectUrl) {
                    redirect(url: params.redirectUrl)
                }
                else {
                    redirect(controller: "product", action: "edit", id: inventoryLevelInstance?.product?.id)
                }

            } else {
                render(view: "edit", model: [inventoryLevelInstance: inventoryLevelInstance])
            }
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def inventoryLevelInstance = InventoryLevel.get(params.id)
        if (inventoryLevelInstance) {
            def productId = inventoryLevelInstance?.product?.id
            try {
                inventoryLevelInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel'), params.id])}"
                //redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel'), params.id])}"
                //redirect(action: "list", id: params.id)
            }
            redirect(controller: "product", action: "edit", id: productId)
        } else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel'), params.id])}"
            redirect(action: "list")
        }
    }

    def dialog = {
        def inventoryLevelInstance = InventoryLevel.get(params.id)
        if (!inventoryLevelInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel'), params.id])}"
        }
        render(template: "form", model: [inventoryLevelInstance: inventoryLevelInstance])
    }

    def markAsSupported = {
        log.info "Mark as supported " + params
        def productIds = params.list("product.id")
        def location = Location.get(session.warehouse.id)
        if (productIds) {
            productIds.each {
                def product = Product.get(it)
                markAs(product, location.inventory, InventoryStatus.SUPPORTED)
            }
            redirect(controller: "inventoryItem", action: "showStockCard", id: productIds[0])
            return
        }
        flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'products.label')])}"
        redirect(controller: "inventory", action: "browse")
    }

    def markAsNotSupported = {
        log.info "Mark as not supported " + params
        def productIds = params.list("product.id")
        def location = Location.get(session.warehouse.id)
        if (productIds) {
            productIds.each {
                def product = Product.get(it)
                markAs(product, location.inventory, InventoryStatus.NOT_SUPPORTED)
            }
            redirect(controller: "inventoryItem", action: "showStockCard", id: productIds[0])
            return
        }
        flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'products.label')])}"
        redirect(controller: "inventory", action: "browse")
    }

    def markAsNonInventoried = {
        log.info "Mark as non-inventoried " + params
        def productIds = params.product.id
        def location = Location.get(session.warehouse.id)
        productIds.each {
            def product = Product.get(it)
            markAs(product, location.inventory, InventoryStatus.SUPPORTED_NON_INVENTORY)
        }
        flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'products.label')])}"
        redirect(controller: "inventory", action: "browse")
    }


    def markAs(Product product, Inventory inventory, InventoryStatus inventoryStatus) {
        def inventoryLevel = InventoryLevel.findByProductAndInventory(product, inventory)
        // Add a new inventory level
        if (!inventoryLevel) {
            inventoryLevel = new InventoryLevel(product: product, status: inventoryStatus)
            inventory.addToConfiguredProducts(inventoryLevel)
            inventory.save()
        }
        // update existing inventory level
        else {
            inventoryLevel.status = inventoryStatus
            inventoryLevel.save()
        }
    }

    def export = {
        def date = new Date()
        def dateFormatted = "${date.format('yyyyMMdd-hhmmss')}"
        def inventoryLevels = []
        def product = Product.get(params.id)
        def location = Location.get(params?.location?.id ?: session?.warehouse?.id)
        def filename = "Inventory Levels - ${dateFormatted}"

        if (product) {
            filename = "Inventory Levels - ${product?.name} - ${dateFormatted}"
            inventoryLevels = product.inventoryLevels
        } else if (location) {
            filename = "Inventory Levels - ${location?.name} - ${dateFormatted}"
            inventoryLevels = InventoryLevel.findAllByInventory(location.inventory)
        } else if (location) {
            filename = "Inventory Levels - ${dateFormatted}"
            inventoryLevels = InventoryLevel.findAll()
        }


        if (inventoryLevels) {
            String text = dataService.exportInventoryLevels(inventoryLevels)
            response.contentType = "text/csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
            render(contentType: "text/csv", text: text)
            return
        } else {
            render(text: 'No inventory levels found', status: 404)
        }

    }


}
