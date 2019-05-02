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

import org.grails.plugins.csv.CSVWriter
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

class InventoryLevelController {

    static allowedMethods = [save: "POST", update: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [inventoryLevelInstanceList: InventoryLevel.list(params), inventoryLevelInstanceTotal: InventoryLevel.count()]
    }

    def create = {
        def inventoryLevelInstance = new InventoryLevel()
        inventoryLevelInstance.properties = params
        return [inventoryLevelInstance: inventoryLevelInstance]
    }

    def save = {
        def inventoryLevelInstance = new InventoryLevel(params)
        if (inventoryLevelInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel'), inventoryLevelInstance.id])}"
            //redirect(action: "list", id: inventoryLevelInstance.id)
            redirect(controller: "product", action: "edit", id: inventoryLevelInstance?.product?.id )
        }
        else {
            render(view: "create", model: [inventoryLevelInstance: inventoryLevelInstance])
        }
    }

    def show = {
        def inventoryLevelInstance = InventoryLevel.get(params.id)
        if (!inventoryLevelInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel'), params.id])}"
            redirect(action: "list")
        }
        else {
            [inventoryLevelInstance: inventoryLevelInstance]
        }
    }

    def edit = {
        def inventoryLevelInstance = InventoryLevel.get(params.id)

        if (!inventoryLevelInstance) {
            def productInstance = Product.get(params.id)
            inventoryLevelInstance = InventoryLevel.findByProduct(productInstance)
        }
        //def inventoryLevelInstance = InventoryLevel.get(params.id)
        if (!inventoryLevelInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel'), params.id])}"
            //redirect(action: "list")
			redirect(action: "create")
        }
        else {
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
                //redirect(action: "list", id: inventoryLevelInstance.id)
                redirect(controller: "product", action: "edit", id: inventoryLevelInstance?.product?.id )

            }
            else {
                render(view: "edit", model: [inventoryLevelInstance: inventoryLevelInstance])
            }
        }
        else {
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
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel'), params.id])}"
            redirect(action: "list")
        }
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
            return;
        }
		flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code:'products.label')])}"
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
            return;
        }
		flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code:'products.label')])}"
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
		flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code:'products.label')])}"
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
        def date = new Date();
        def dateFormatted = "${date.format('yyyyMMdd-hhmmss')}"
        def inventoryLevels = []
        def product = Product.get(params.id)
        def location = Location.get(params?.location?.id?:session?.warehouse?.id)
        def filename = "Inventory Levels - ${dateFormatted}"

        if (product) {
            filename = "Inventory Levels - ${product?.name} - ${dateFormatted}"
            inventoryLevels = product.inventoryLevels
        }
        else if (location) {
            filename = "Inventory Levels - ${location?.name} - ${dateFormatted}"
            inventoryLevels = InventoryLevel.findAllByInventory(location.inventory)
        }
        else if (location) {
            filename = "Inventory Levels - ${dateFormatted}"
            inventoryLevels = InventoryLevel.findAll()
        }


        if (inventoryLevels) {
            def sw = new StringWriter()

            def csv = new CSVWriter(sw, {
                "Product Code" {it.productCode}
                "Product Name" {it.productName}
                "Inventory" {it.inventory}
                "Status" {it.status}
                "Bin Location" {it.binLocation}
                "Preferred" {it.preferred}
                "ABC Class" {it.abcClass}
                "Min Quantity" {it.minQuantity}
                "Reorder Quantity" {it.reorderQuantity}
                "Max Quantity" {it.maxQuantity}
                "Forecast Quantity" {it.forecastQuantity}
                "Forecast Period" {it.forecastPeriodDays}
                "UOM" {it.unitOfMeasure}
            })
            inventoryLevels.each { inventoryLevel ->
                csv << [
                        productCode: inventoryLevel.product.productCode,
                        productName: inventoryLevel.product.name,
                        inventory: inventoryLevel.inventory.warehouse.name,
                        status: inventoryLevel.status,
                        binLocation: inventoryLevel.binLocation?:"",
                        preferred: inventoryLevel.preferred?:"",
                        abcClass: inventoryLevel.abcClass?:"",
                        minQuantity: inventoryLevel.minQuantity?:"",
                        reorderQuantity: inventoryLevel.reorderQuantity?:"",
                        maxQuantity: inventoryLevel.maxQuantity?:"",
                        forecastQuantity: inventoryLevel.forecastQuantity?:"",
                        forecastPeriodDays: inventoryLevel.forecastPeriodDays?:"",
                        unitOfMeasure: inventoryLevel?.product?.unitOfMeasure?:"EA"
                ]
            }
            println csv.writer.toString()
            response.contentType = "text/csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
            render(contentType:"text/csv", text: csv.writer.toString())
            return;
        }
        else {
            render(text: 'No inventory levels found', status: 404)
        }

    }


}
