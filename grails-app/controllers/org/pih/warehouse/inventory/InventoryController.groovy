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

import grails.converters.JSON
import grails.plugin.springcache.annotations.Cacheable
import grails.validation.ValidationException
import groovy.time.TimeCategory
import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.ListUtils
import org.apache.commons.collections.list.LazyList
import org.apache.commons.lang.StringEscapeUtils
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.reporting.Consumption

import java.text.SimpleDateFormat;
// import java.util.Calendar;

import org.pih.warehouse.util.DateUtil

class InventoryController {
	
	def dataSource
    def productService
	def inventoryService
    def requisitionService

    static allowedMethods = [show: "GET", search: "POST", download: "GET"];

    def index = {
		redirect(action: "browse");
	}

	/**
	 * Allows a user to browse the inventory for a particular warehouse.  
	 */
    //@Cacheable("inventoryControllerCache")
	def browse = { InventoryCommand cmd ->
        if(!params.max) params.max = 10
        if(!params.offset) params.offset = 0

		// Get the current warehouse from either the request or the session
		cmd.warehouseInstance = Location.get(params?.warehouse?.id) 
		if (!cmd.warehouseInstance) {
			cmd.warehouseInstance = Location.get(session?.warehouse?.id);
		}

		// Get the primary category from either the request or the session or as the first listed by default
		def quickCategories = productService.getQuickCategories();
		/*
		cmd.categoryInstance = Category.get(params?.categoryId)
		if (!cmd.categoryInstance) {
			cmd.categoryInstance = Category.get(session?.inventoryCategoryId);
			if (!cmd.categoryInstance) {
				cmd.categoryInstance = productService.getRootCategory() 
				//cmd.categoryInstance =  quickCategories.get(0);
			}
		}
		session?.inventoryCategoryId = cmd?.categoryInstance?.id
		*/
		cmd.categoryInstance = productService.getRootCategory()
		
		// if we have arrived via a quick link tab, reset any subcategories or search terms in the session
		if (params?.resetSearch) {
			session?.inventorySubcategoryId = null
			session?.inventorySearchTerms = null
		}
		
		// Pre-populate the sub-category and search terms from the session
		cmd.subcategoryInstance = Category.get(session?.inventorySubcategoryId)
		cmd.searchTerms = session?.inventorySearchTerms
		cmd.showHiddenProducts = session?.showHiddenProducts
		cmd.showUnsupportedProducts = session?.showUnsupportedProducts
		cmd.showNonInventoryProducts = session?.showNonInventoryProducts
		cmd.showOutOfStockProducts = session?.showOutOfStockProducts ?: true
		
		// If a new search is being performed, override the session-based terms from the request
		if (request.getParameter("searchPerformed")) {
			cmd.subcategoryInstance = Category.get(params?.subcategoryId)
			session?.inventorySubcategoryId = cmd.subcategoryInstance?.id
			
			cmd.searchTerms = params.searchTerms
			session?.inventorySearchTerms = cmd.searchTerms
			
			cmd.showHiddenProducts = params?.showHiddenProducts == "on"
			session?.showHiddenProducts = cmd.showHiddenProducts

			cmd.showUnsupportedProducts = params?.showUnsupportedProducts == "on"
			session?.showUnsupportedProducts = cmd.showUnsupportedProducts

			cmd.showOutOfStockProducts = params?.showOutOfStockProducts == "on"
			session?.showOutOfStockProducts = cmd.showOutOfStockProducts
			
			cmd.showNonInventoryProducts = params?.showNonInventoryProducts == "on"
			session?.showNonInventoryProducts = cmd.showNonInventoryProducts

		}
        cmd.maxResults = params?.max
        cmd.offset = params?.offset

		// Pass this to populate the matching inventory items
		inventoryService.browseInventory(cmd);

		def tags = productService.getPopularTags()

		def categories = productService.getTopLevelCategories()
		
		[ commandInstance: cmd, quickCategories: quickCategories, tags : tags, numProducts : cmd.numResults, categories: categories, rootCategory: productService.getRootCategory() ]
	}
	
		
	/**
	 * 
	 */
	def create = {
		def warehouseInstance = Location.get(params?.warehouse?.id)
		if (!warehouseInstance) { 
			warehouseInstance = Location.get(session?.warehouse?.id);
		}
		return [warehouseInstance: warehouseInstance]
	}
	
	
	/**
	 * 
	 */
	def save = {		
		def warehouseInstance = Location.get(params.warehouse?.id)
		if (!warehouseInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'warehouse.label', default: 'Location'), params.id])}"
			redirect(action: "list")
		} else {  
			warehouseInstance.inventory = new Inventory(params);
			//inventoryInstance.warehouse = session.warehouse;
			if (warehouseInstance.save(flush: true)) {
				flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), warehouseInstance.inventory.id])}"
				redirect(action: "browse")
			}
			else {
				render(view: "create", model: [warehouseInstance: warehouseInstance])
			}
		}
	}
	
	/**
	 * 
	 */
	def show = {
        def quantityMap = [:]
        long startTime = System.currentTimeMillis()
        def location = Location.get(session.warehouse.id)
		def inventoryInstance = Inventory.get(params.id)
        if (!inventoryInstance) {
            inventoryInstance = location.inventory
        }
        if (!inventoryInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
            redirect(action: "list")
            return
        }


        def elapsedTime = (System.currentTimeMillis() - startTime)
        log.info("Show current inventory: " + (System.currentTimeMillis() - startTime) + " ms");
        //def inventoryMapping = inventoryInstance.inventoryItems.groupBy{ it?.product }
        [
            //inventoryMapping: inventoryMapping,
            location: location,
            elapsedTime: elapsedTime,
            quantityMap: quantityMap
        ]

	}

	def search = { QuantityOnHandReportCommand command ->
        def quantityMapByDate = [:]
        def startTime = System.currentTimeMillis()
        println "search " + params

        println "Locations: " + command?.locations?.toString() + ", Start date = " + command?.startDate + ", End Date = " + command?.endDate + ", Tag: " + command.tag

        if (command.validate()) {

            if (!command?.locations) {
                command.locations = [Location.get(session?.warehouse?.id)]
            }
            //def transactions = Transaction.findAllByInventory(location.inventory)
            //def transactionEntries = (transactions*.transactionEntries).flatten()
            //log.info "transactionEntries: " + transactionEntries.size()
            //def quantityMap = inventoryService.getQuantityByProductMap(transactionEntries)

            if (command.startDate && command.endDate) {
                //def duration = command?.endDate - command?.startDate
                //command.dates = new Date[duration+1]
                //(command?.startDate .. command?.endDate).eachWithIndex { date, i ->
                //    println "Date " + date + " i " + i
                //    command.dates[i] = date
                //}

                //def duration = command.endDate - command.endDate
                def count = 0;

                command.dates = getDatesBetween(command.startDate, command.endDate, command.frequency)
                //if (command.dates.size() >= 61) {
                //    command.dates = []
                    //throw new Exception("Choose a different frequency")
                //    command.errors.rejectValue("frequency","errors.frequency.code","Cannot run report for more than 60 days")
                //    render(view: "show", model: [quantityMapByDate: quantityMapByDate, command: command])
                //    return
                //}

                println "dates : " + command?.dates

            }

            else if (command.startDate) {
                command?.dates << command?.startDate
            }
            else if (command.endDate) {
                command?.dates << command?.endDate
            }

            println "dates: " + command?.dates

            command.locations.each { location ->
                for (date in command?.dates) {
                    println "Get quantity map " + date + " location = " + location
                    def quantityMap = [:]
                    quantityMap = inventoryService.getQuantityOnHandAsOfDate(location, date, command.tag)
                    def existingQuantityMap = quantityMapByDate[date]
                    if (existingQuantityMap) {
                        quantityMapByDate[date] = mergeQuantityMap(existingQuantityMap, quantityMap)
                    }
                    else {
                        quantityMapByDate[date] = quantityMap
                    }
                    println "quantityMap = " + quantityMap?.keySet()?.size() + " results "
                    println "Time " + (System.currentTimeMillis() - startTime) + " ms"
                }
            }


            def keys = quantityMapByDate[command.dates[0]]?.keySet()?.sort()
            println "keys: " + keys
            keys.each { product ->
                command.products << product
            }
        }

        if (params.button == 'download') {
            if (command.products) {
                def date = new Date();
                response.setHeader("Content-disposition", "attachment; filename='Baseline-QoH-${date.format("yyyyMMdd-hhmmss")}.csv'")
                response.contentType = "text/csv"
                def csv = inventoryService.exportBaselineQoH(command.products, quantityMapByDate)
                println "export products: " + csv
                render csv
            }
            else {
                render(text: 'No products found', status: 404)
            }
            return;
        }


        render(view: "show", model: [quantityMapByDate: quantityMapByDate, command: command])

    }

    def mergeQuantityMap(oldQuantityMap, newQuantityMap) {
        oldQuantityMap.each { product, oldQuantity ->
            def newQuantity = newQuantityMap[product]?:0
            oldQuantityMap[product] =  newQuantity + oldQuantity

        }
        return oldQuantityMap
    }


    def getDatesBetween(startDate, endDate, frequency) {

        def count = 0
        def dates = []
        if (startDate.before(endDate)) {
            def date = startDate
            while(date.before(endDate)) {
                println "Start date = " + date + " endDate = " + endDate

                dates << date
                if (params.frequency in ['Daily']) {
                    use(TimeCategory) {
                        date = date.plus(1.day)
                    }
                }
                else if (params.frequency in ['Weekly']) {
                    use(TimeCategory) {
                        date = date.plus(1.week)
                    }
                }
                else if (params.frequency in ['Monthly']) {
                    use(TimeCategory) {
                        date = date.plus(1.month)
                    }
                }
                else if (params.frequency in ['Quarterly']) {
                    use(TimeCategory) {
                        date = date.plus(3.month)
                    }
                }
                else if (params.frequency in ['Annually']) {
                    use(TimeCategory) {
                        date = date.plus(1.year)
                    }
                }
                else {
                    use(TimeCategory) {
                        date = date.plus(1.day)
                    }

                }
                count++
            }
        }
        return dates
    }


    def download = { QuantityOnHandReportCommand command ->

        println "search " + params
        println "search " + command.location + " " + command.startDate
        def quantityMap = inventoryService.getQuantityOnHandAsOfDate(command.location, command.startDate, command.tag)
        if (quantityMap) {
            def statusMap = inventoryService.getInventoryStatus(command.location)
            def filename = "Stock report - " +
                    (command?.tag?command?.tag?.tag:"All Products") + " - " +
                    command?.location?.name + " - " +
                    command?.startDate?.format("yyyyMMMdd") + ".csv"
            response.setHeader("Content-disposition", "attachment; filename='" + filename + "'")
            render(contentType: "text/csv", text:getCsvForProductMap(quantityMap, statusMap))
            return;
        }
        flash.message = "There are no search results available to download - please try again."
        redirect(action: "show")

    }

	
	
	def addToInventory = {
		def inventoryInstance = Inventory.get( params.id )
		def productInstance = Product.get( params.product.id )

		if (!productInstance) { 
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'product.label', default: 'Product'), params?.product?.id])}"
			redirect(action: "browse");
		}
		else { 
			def itemInstance = new InventoryItem(product: productInstance)
			if (!itemInstance.hasErrors() && itemInstance.save(flush: true)) {
				flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), inventoryInstance.id])}"
				redirect(action: "browse", id: inventoryInstance.id)
			}
			else {
				flash.message = "${warehouse.message(code: 'inventory.unableToCreateItem.message')}"
				//inventoryInstance.errors = itemInstance.errors;
				//render(view: "browse", model: [inventoryInstance: inventoryInstance])
			}			
		}
	}
	
	
	def edit = {
		def inventoryInstance = Inventory.get(params.id)
		if (!inventoryInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
			redirect(action: "list")
		}
		else {
			def productInstanceMap = Product.getAll().groupBy { it.productType } 
			
			return [inventoryInstance: inventoryInstance, productInstanceMap: productInstanceMap]
		}
	}
	
	def update = {
		def inventoryInstance = Inventory.get(params.id)
		if (inventoryInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (inventoryInstance.version > version) {					
					inventoryInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'inventory.label', default: 'Inventory')] as Object[], 
						"Another user has updated this Inventory while you were editing")
					render(view: "edit", model: [inventoryInstance: inventoryInstance])
					return
				}
			}
			inventoryInstance.properties = params
			if (!inventoryInstance.hasErrors() && inventoryInstance.save(flush: true)) {
				flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), inventoryInstance.id])}"
				redirect(action: "browse", id: inventoryInstance.id)
			}
			else {
				render(view: "edit", model: [inventoryInstance: inventoryInstance])
			}
		}
		else {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
			redirect(action: "list")
		}
	}
	
	def delete = {
		def inventoryInstance = Inventory.get(params.id)
		if (inventoryInstance) {
			try {
				inventoryInstance.delete(flush: true)
				flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
			redirect(action: "list")
		}
	}
	
	def addItem = {
		def inventoryInstance = Inventory.get(params?.inventory?.id)
		def productInstance = Product.get(params?.product?.id);
		def itemInstance = inventoryService.findByProductAndLotNumber(productInstance, params.lotNumber)
		if (itemInstance) {
			flash.message = "${warehouse.message(code: 'default.alreadyExists.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory item'), inventoryInstance.id])}"
			redirect(action: "show", id: inventoryInstance.id)
		}
		else {
			itemInstance = new InventoryItem(params)
			if (itemInstance.hasErrors() || !itemInstance.save(flush:true)) {
				flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory item'), inventoryInstance.id])}"
				redirect(action: "show", id: inventoryInstance.id)				
			}
			else {
				itemInstance.errors.each { println it }
				//redirect(action: "show", id: inventoryInstance.id)
				flash.message = "${warehouse.message(code: 'default.notUpdated.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory item'), inventoryInstance.id])}"
				render(view: "show", model: [inventoryInstance: inventoryInstance, itemInstance : itemInstance])
			}
		}
	}
	
	def deleteItem = {
		def itemInstance = InventoryItem.get(params.id)
		if (itemInstance) {
			try {
				itemInstance.delete(flush: true)
				flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Inventory item'), params.id])}"
				redirect(action: "show", id: params.inventory.id)
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Inventory item'), params.id])}"
				redirect(action: "show", id: params.inventory.id)
			}
		}
		else {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
			redirect(action: "show", id: params.inventory.id)
		}

				
	}
	
	def listTransactions = { 
		redirect(action: listAllTransactions)
	}
	
	def listDailyTransactions = { 
		def dateFormat = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
		def dateSelected = (params.date) ? dateFormat.parse(params.date) : new Date();
		
		def transactionsByDate = Transaction.list().groupBy { DateUtil.clearTime(it?.transactionDate) }?.entrySet()?.sort{ it.key }?.reverse()
		
		def transactions = Transaction.findAllByTransactionDate(dateSelected);
		
		[ transactions: transactions, transactionsByDate: transactionsByDate, dateSelected: dateSelected ]
	}



    def list = {
        println "List " + params
        def location = Location.get(session.warehouse.id)
        def quantityMap = inventoryService.getQuantityByProductMap(location.inventory)
        def statusMap = inventoryService.getInventoryStatus(location)
        println "QuantityMap: " + quantityMap
        [quantityMap:quantityMap,statusMap: statusMap]
    }


    def listReconditionedStock = {
        def location = Location.get(session.warehouse.id)
        def quantityMap = inventoryService.getReconditionedStock(location)
        def statusMap = inventoryService.getInventoryStatus(location)
        if (params.format == "csv") {
            def filename = "Reconditioned stock - " + location.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename='" + filename + "'")
            render(contentType: "text/csv", text:getCsvForProductMap(quantityMap, statusMap))
            return;
        }
        render (view: "list", model: [quantityMap:quantityMap, statusMap: statusMap])
    }


    def listTotalStock = {
        def location = Location.get(session.warehouse.id)
        def quantityMap = inventoryService.getTotalStock(location);
        def statusMap = inventoryService.getInventoryStatus(location)
        if (params.format == "csv") {
            def filename = "Total stock - " + location.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename='" + filename + "'")
            render(contentType: "text/csv", text:getCsvForProductMap(quantityMap, statusMap))
            return;
        }

        render (view: "list", model: [quantityMap:quantityMap, statusMap: statusMap])

    }

    def listInStock = {
        def location = Location.get(session.warehouse.id)
        def quantityMap = inventoryService.getInStock(location);
        def statusMap = inventoryService.getInventoryStatus(location)
        if (params.format == "csv") {
            def filename = "In stock - " + location.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename='" + filename + "'")
            render(contentType: "text/csv", text:getCsvForProductMap(quantityMap, statusMap))
            return;
        }

        render (view: "list", model: [quantityMap:quantityMap, statusMap: statusMap])

    }

    def listLowStock = {
        def location = Location.get(session.warehouse.id)
        def quantityMap = inventoryService.getLowStock(location);
        def statusMap = inventoryService.getInventoryStatus(location)
        if (params.format == "csv") {
            def filename = "Low stock - " + location.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename='" + filename + "'")
            render(contentType: "text/csv", text:getCsvForProductMap(quantityMap, statusMap))
            return;
        }

        //[inventoryItems:lowStock, quantityMap:quantityMap]
        render (view: "list", model: [quantityMap:quantityMap, statusMap: statusMap])
    }

    def listReorderStock = {
        def location = Location.get(session.warehouse.id)
        def quantityMap = inventoryService.getReorderStock(location);
        def statusMap = inventoryService.getInventoryStatus(location)
        if (params.format == "csv") {
            def filename = "Reorder stock - " + location.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename='" + filename + "'")
            render(contentType: "text/csv", text:getCsvForProductMap(quantityMap, statusMap))
            return;
        }

        render (view: "list", model: [quantityMap:quantityMap, statusMap: statusMap])
    }


    def listQuantityOnHandZero = {
        def location = Location.get(session.warehouse.id)
        def quantityMap = inventoryService.getQuantityOnHandZero(location);
        def statusMap = inventoryService.getInventoryStatus(location)
        if (params.format == "csv") {
            def filename = "Out of stock  - all - " + location.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename='" + filename + "'")
            render(contentType: "text/csv", text:getCsvForProductMap(quantityMap, statusMap))
            return;
        }

        //[inventoryItems:lowStock, quantityMap:quantityMap]
        render (view: "list", model: [quantityMap:quantityMap, statusMap: statusMap])
    }


    def listOverStock = {
        def location = Location.get(session.warehouse.id)
        def quantityMap = inventoryService.getOverStock(location)
        def statusMap = inventoryService.getInventoryStatus(location)
        if (params.format == "csv") {
            def filename = "Overstock - " + location.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename='" + filename + "'")
            render(contentType: "text/csv", text:getCsvForProductMap(quantityMap, statusMap))
            return;
        }

        //[inventoryItems:lowStock, quantityMap:quantityMap]
        render (view: "list", model: [quantityMap:quantityMap, statusMap: statusMap])
    }

    def listOutOfStock = {
        def location = Location.get(session.warehouse.id)
        def quantityMap = inventoryService.getOutOfStock(location);
        def statusMap = inventoryService.getInventoryStatus(location)
        if (params.format == "csv") {
            def filename = "Out of stock - supported - " + location.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename='" + filename + "'")
            render(contentType: "text/csv", text:getCsvForProductMap(quantityMap, statusMap))
            return;
        }

        render (view: "list", model: [quantityMap:quantityMap, statusMap: statusMap])
    }



	def listExpiredStock = { 
		def warehouse = Location.get(session.warehouse.id)
		def categorySelected = (params.category) ? Category.get(params.category) : null;		
		def expiredStock = inventoryService.getExpiredStock(categorySelected, warehouse);
		def categories = expiredStock?.collect { it.product.category }?.unique()
		def quantityMap = inventoryService.getQuantityForInventory(warehouse.inventory)
        def expiredStockMap = [:]
        expiredStock.each { inventoryItem ->
            expiredStockMap[inventoryItem] = quantityMap[inventoryItem]
        }
        if (params.format == "csv") {
            def filename = "Expired stock | " + warehouse.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename='" + filename + "'")
            render(contentType: "text/csv", text:getCsvForInventoryMap(expiredStockMap))
            return;
        }
		
		[inventoryItems:expiredStock, quantityMap:quantityMap, categories:categories, categorySelected:categorySelected]
	}
	
	
	def listExpiringStock = { 
		def threshold = (params.threshold) ? params.threshold as int : 0;
		def category = (params.category) ? Category.get(params.category) : null;
		def location = Location.get(session.warehouse.id)		
		def expiringStock = inventoryService.getExpiringStock(category, location, threshold)
		def categories = expiringStock?.collect { it?.product?.category }?.unique().sort { it.name } ;
		def quantityMap = inventoryService.getQuantityForInventory(location.inventory)
        def expiringStockMap = [:]
        expiringStock.each { inventoryItem ->
            expiringStockMap[inventoryItem] = quantityMap[inventoryItem]
        }

        if (params.format == "csv") {
            def filename = "Expiring stock | " + location.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename='" + filename + "'")
            render(contentType: "text/csv", text:getCsvForInventoryMap(expiringStockMap))
            return;
        }

		[inventoryItems:expiringStock, quantityMap:quantityMap, categories:categories, 
			categorySelected:category, thresholdSelected:threshold ]
	}


    def getCsvForInventoryMap(map) {
        return getCsvForInventoryMap(map, [:])
    }

    def getCsvForInventoryMap(map, statusMap) {
        def csv = "";
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.status.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.productCode.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryItem.lotNumber.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryItem.expirationDate.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'category.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.tags.label', default:'Tags')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.manufacturer.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.manufacturerCode.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.vendor.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.vendorCode.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.binLocation.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.unitOfMeasure.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.pricePerUnit.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.minQuantity.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.reorderQuantity.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.maxQuantity.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.currentQuantity.label', default: 'Current quantity')}"  + '"' + ","
        csv += "\n"

        map.each { inventoryItem, quantity ->

            def product = inventoryItem?.product
            def inventoryLevel = product?.getInventoryLevel(session.warehouse.id)
            def status = statusMap[product]
            if (!status) {
                status = product?.getStatus(session.warehouse.id, quantity?:0 as int)
            }
            def statusMessage = "${warehouse.message(code:'enum.InventoryLevelStatusCsv.'+status)}"
            def expirationDate = formatDate(date: inventoryItem?.expirationDate, format: "dd/MMM/yyyy");
            csv += '"' + (statusMessage?:"")  + '"' + ","
            csv += '"' + (product.productCode?:"")  + '"' + ","
            csv += StringEscapeUtils.escapeCsv(product?.name?:"") + ","
            csv += '"' + (inventoryItem?.lotNumber?:"")  + '"' + ","
            csv += '"' + formatDate(date: inventoryItem?.expirationDate, format: 'dd/MM/yyyy')  + '"' + ","
            csv += '"' + (product?.category?.name?:"")  + '"' + ","
            csv += '"' + (product?.tagsToString()?:"")  + '"' + ","
            csv += '"' + (product?.manufacturer?:"")  + '"' + ","
            csv += '"' + (product?.manufacturerCode?:"")  + '"' + ","
            csv += '"' + (product?.vendor?:"") + '"' + ","
            csv += '"' + (product?.vendorCode?:"") + '"' + ","
            csv += '"' + (inventoryLevel?.binLocation?:"") + '"' + ","
            csv += '"' + (product?.unitOfMeasure?:"") + '"' + ","
            csv += (product?.pricePerUnit?:"") + ","
            csv += (inventoryLevel?.minQuantity?:"") + ","
            csv += (inventoryLevel?.reorderQuantity?:"") + ","
            csv += (inventoryLevel?.maxQuantity?:"")+ ","
            csv += '' + (quantity?:"0")  + '' + ","
            csv += "\n"
        }
        return csv
    }

    def getCsvForProductMap(map) {
        return getCsvForProductMap(map, [:])
    }

    def getCsvForProductMap(map, statusMap) {
        def csv = "";
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.status.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.productCode.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'category.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.tags.label', default:'Tags')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.manufacturer.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.manufacturerCode.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.vendor.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.vendorCode.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.binLocation.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.abcClass.label', default: 'ABC Class')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.unitOfMeasure.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.pricePerUnit.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.minQuantity.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.reorderQuantity.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.maxQuantity.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.currentQuantity.label', default: 'Current quantity')}"  + '"' + ","
        csv += "\n"

        map.sort().each { product, quantity ->
            def inventoryLevel = product?.getInventoryLevel(session.warehouse.id)
            def status = statusMap[product]
            if (!status) {
                status = product?.getStatus(session.warehouse.id, quantity?:0 as int)
            }

            def statusMessage = "${warehouse.message(code:'enum.InventoryLevelStatusCsv.'+status)}"
            csv += '"' + (statusMessage?:"")  + '"' + ","
            csv += '"' + (product.productCode?:"")  + '"' + ","
            csv += StringEscapeUtils.escapeCsv(product?.name) + ","
            csv += '"' + (product?.category?.name?:"")  + '"' + ","
            csv += '"' + (product?.tagsToString()?:"")  + '"' + ","
            csv += '"' + (product?.manufacturer?:"")  + '"' + ","
            csv += '"' + (product?.manufacturerCode?:"")  + '"' + ","
            csv += '"' + (product?.vendor?:"") + '"' + ","
            csv += '"' + (product?.vendorCode?:"") + '"' + ","
            csv += '"' + (inventoryLevel?.binLocation?:"")  + '"' + ","
            csv += '"' + (inventoryLevel?.abcClass?:"")  + '"' + ","
            csv += '"' + (product?.unitOfMeasure?:"")  + '"' + ","
            csv += (product?.pricePerUnit?:"") + ","
            csv += (inventoryLevel?.minQuantity?:"") + ","
            csv += (inventoryLevel?.reorderQuantity?:"") + ","
            csv += (inventoryLevel?.maxQuantity?:"") + ","
            csv += (quantity?:"0") + ","
            csv += "\n"
        }
        return csv
    }


    /*
	def listLowStock = {
		def warehouse = Location.get(session.warehouse.id)
		def results = inventoryService.getProductsBelowMinimumAndReorderQuantities(warehouse.inventory, params.showUnsupportedProducts ? true : false)
		
		Map inventoryLevelByProduct = new HashMap();
		inventoryService.getInventoryLevelsByInventory(warehouse.inventory).each {
			inventoryLevelByProduct.put(it.product, it);
		}
		
		// Set of categories that we can filter by
		def categories = [] as Set
		categories.addAll(results['reorderProductsQuantityMap']?.keySet().collect { it.category })
		categories.addAll(results['minimumProductsQuantityMap']?.keySet().collect { it.category })
		categories = categories.findAll { it != null }
		
		// poor man's filter
		def categorySelected = (params.category) ? Category.get(params.category) : null;
		log.debug "categorySelected: " + categorySelected
		if (categorySelected) {
			results['reorderProductsQuantityMap'] = results['reorderProductsQuantityMap'].findAll { it.key?.category == categorySelected }
			results['minimumProductsQuantityMap'] = results['minimumProductsQuantityMap'].findAll { it.key?.category == categorySelected }
		}
		
		[reorderProductsQuantityMap: results['reorderProductsQuantityMap'], minimumProductsQuantityMap: results['minimumProductsQuantityMap'], 
			categories: categories, categorySelected: categorySelected, showUnsupportedProducts: params.showUnsupportedProducts, inventoryLevelByProduct: inventoryLevelByProduct]
	}
    */
    /*
	def listReorderStock = {

		def warehouse = Location.get(session.warehouse.id)
		
		def results = inventoryService.getProductsBelowMinimumAndReorderQuantities(warehouse.inventory, params.showUnsupportedProducts ? true : false)
		
		
		Map inventoryLevelByProduct = new HashMap();
		//inventoryService.getInventoryLevelsByInventory(warehouse.inventory).each {
		//	inventoryLevelByProduct.put(it.product, it);
		//}
		
		// Set of categories that we can filter by
		def categories = [] as Set
		categories.addAll(results['reorderProductsQuantityMap']?.keySet().collect { it.category })
		categories.addAll(results['minimumProductsQuantityMap']?.keySet().collect { it.category })
		categories = categories.findAll { it != null }
		
		// poor man's filter
		def categorySelected = (params.category) ? Category.get(params.category) : null;
		log.debug "categorySelected: " + categorySelected
		if (categorySelected) {
			results['reorderProductsQuantityMap'] = results['reorderProductsQuantityMap'].findAll { it.key?.category == categorySelected }
			results['minimumProductsQuantityMap'] = results['minimumProductsQuantityMap'].findAll { it.key?.category == categorySelected }
		}
		
		[reorderProductsQuantityMap: results['reorderProductsQuantityMap'], minimumProductsQuantityMap: results['minimumProductsQuantityMap'],
			categories: categories, categorySelected: categorySelected, showUnsupportedProducts: params.showUnsupportedProducts, inventoryLevelByProduct: inventoryLevelByProduct]
	}
	*/
	
	def searchRecall = {
		
		log.info "searchRecall " + params
		
		
		
	}

	
	def showConsumption = { ConsumptionCommand command ->
		
		def consumptions = inventoryService.getConsumptionTransactionsBetween(command?.startDate, command?.endDate)
		def consumptionMap = consumptions.groupBy { it.product };
		
		//def products = Product.list()
		def products = consumptions*.product.unique();
		//products = products.findAll { consumptionMap[it] > 0 }
		def productMap = products.groupBy { it.category };
		def dateFormat = new SimpleDateFormat("ddMMyyyy")
		//def dateKeys = inventoryService.getConsumptionDateKeys()
		def startDate = command?.startDate?:(new Date()-7)
		def endDate = command?.endDate?:new Date()
		
		def calendar = Calendar.instance
		def dateKeys = (startDate..endDate).collect { date ->
			calendar.setTime(date);
			[
				date: date,
				day: calendar.get(Calendar.DAY_OF_MONTH),
				week: calendar.get(Calendar.WEEK_OF_YEAR),
				month: calendar.get(Calendar.MONTH),
				year: calendar.get(Calendar.YEAR),
				key: dateFormat.format(date)
			]
		}.sort { it.date }
		
		
		def groupBy = command?.groupBy;
		log.debug("groupBy = " + groupBy)
		def daysBetween = (groupBy!="default") ? -1 : endDate - startDate
		if (daysBetween > 365 || groupBy.equals("yearly")) {
			groupBy = "yearly"
			dateFormat = new SimpleDateFormat("yyyy")
		}
		else if ((daysBetween > 61 && daysBetween < 365) || groupBy.equals("monthly")) {
			groupBy = "monthly"
			dateFormat = new SimpleDateFormat("MMM")
		}
		else if (daysBetween > 14 && daysBetween < 60 || groupBy.equals("weekly")) {
			groupBy = "weekly"
			dateFormat = new SimpleDateFormat("'Week' w")
		}
		else if (daysBetween > 0 && daysBetween <= 14 || groupBy.equals("daily")) {
			groupBy = "daily"
			dateFormat = new SimpleDateFormat("MMM dd")
		}
		dateKeys = dateKeys.collect { dateFormat.format(it.date) }.unique()
		
		
		log.debug("consumption " + consumptionMap)
		def consumptionProductDateMap = [:]
		consumptions.each { 
			def dateKey = it.product.id + "_" + dateFormat.format(it.transactionDate)
			def quantity = consumptionProductDateMap[dateKey];
			if (!quantity) quantity = 0;
			quantity += it.quantity?:0
			consumptionProductDateMap[dateKey] = quantity;
			
			def totalKey = it.product.id + "_Total"
			def totalQuantity = consumptionProductDateMap[totalKey];
			if (!totalQuantity) totalQuantity = 0;
			totalQuantity += it.quantity?:0
			consumptionProductDateMap[totalKey] = totalQuantity;

		}
		
		
		
			
		/*
		def today = new Date();
		def warehouse = Location.get(session.warehouse.id)
		
		// Get all transactions from the past week 
		def transactions = inventoryService.getConsumptionTransactions(today-7, today);
		def transactionEntries = []
		transactions.each { transaction -> 
			transaction.transactionEntries.each { transactionEntry -> 
				transactionEntries << transactionEntry
			}
		}
		
		def consumptionMap = [:]
		log.debug "Products " + products.size();
				
		def transactionEntryMap = transactionEntries.groupBy { it.inventoryItem.product }		
		transactionEntryMap.each { key, value ->
			def consumed = value.sum { it.quantity }			
			log.debug("key="+key + ", value = " + value + ", total consumed=" + consumed);
			consumptionMap[key] = consumed;
		}
		*/

		


		
				
		[
			command: command,
			productMap : productMap,
			consumptionMap: consumptionMap,
			consumptionProductDateMap: consumptionProductDateMap,
			productKeys: products, 
			results: inventoryService.getConsumptions(command?.startDate, command?.endDate, command?.groupBy),
			dateKeys: dateKeys]
	}

	def refreshConsumptionData = { 
		def consumptionType = TransactionType.get(2)		
		def transactions = Transaction.findAllByTransactionType(consumptionType)

		// Delete all consumption rows		
		Consumption.executeUpdate("delete Consumption c")
		
		// Reset auto increment counter to 0
		// ALTER TABLE consumption AUTO_INCREMENT=0
		
		transactions.each { xact ->
			xact.transactionEntries.each { entry -> 
				def consumption = new Consumption(
					product: entry.inventoryItem.product, 
					inventoryItem: entry.inventoryItem, 
					quantity: entry.quantity,
					transactionDate: entry.transaction.transactionDate,
					location: entry.transaction.inventory.warehouse,
					month: entry.transaction.transactionDate.month,
					day: entry.transaction.transactionDate.day,
					year: entry.transaction.transactionDate.year+1900);
				
				if (!consumption.hasErrors() && consumption.save()) { 
					
				}
				else { 
					flash.message = "error saving Consumption " + consumption.errors
				}
			}
		}
		redirect(controller: "inventory", action: "showConsumption")
	}
	
	
	/**
	 * Used to create default inventory items.
	 * @return
	 */
	def createDefaultInventoryItems = { 
		def products = inventoryService.findProductsWithoutEmptyLotNumber();
		products.each { product -> 
			def inventoryItem = new InventoryItem()
			inventoryItem.product = product
			inventoryItem.lotNumber = null;
			inventoryItem.expirationDate = null;
			inventoryItem.save();
		}
		redirect(controller: "inventory", action: "showProducts")
	}
	
		
	def showProducts = { 
		def products = inventoryService.findProductsWithoutEmptyLotNumber()
		[ products : products ]
		
	}
	
	
	def listAllTransactions = {		
		
		// FIXME Using the dynamic finder Inventory.findByLocation() does not work for some reason
		def currentInventory = Inventory.list().find( {it.warehouse.id == session.warehouse.id} )
		
		// we are only showing transactions for the inventory associated with the current warehouse
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		params.sort = params?.sort ?: "dateCreated"
		params.order = params?.order ?: "desc"
		def transactions = []
		def transactionCount = 0;
		def transactionType = TransactionType.get(params?.transactionType?.id)
		if (transactionType) { 
			transactions = Transaction.findAllByInventoryAndTransactionType(currentInventory, transactionType, params);			
			transactionCount = Transaction.countByInventoryAndTransactionType(currentInventory, transactionType);
		}
		else { 
			transactions = Transaction.findAllByInventory(currentInventory, params);
			transactionCount = Transaction.countByInventory(currentInventory);
		}
		def transactionMap = Transaction.findAllByInventory(currentInventory).groupBy { it?.transactionType?.id } 
		log.debug(transactionMap.keySet())
		render(view: "listTransactions", model: [transactionInstanceList: transactions, 
			transactionCount: transactionCount, transactionTypeSelected: transactionType, 
			transactionMap: transactionMap ])
	}

		
	def listPendingTransactions = { 
		def transactions = Transaction.findAllByConfirmedOrConfirmedIsNull(Boolean.FALSE)
		render(view: "listTransactions", model: [transactionInstanceList: transactions])
	}
	
	def listConfirmedTransactions = { 		
		def transactions = Transaction.findAllByConfirmed(Boolean.TRUE)
		render(view: "listTransactions", model: [transactionInstanceList: transactions])
	}
	
	
	def deleteTransaction = { 
		def transactionInstance = Transaction.get(params.id);
		
		if (transactionInstance) {
			try {
				if (inventoryService.isLocalTransfer(transactionInstance)) {
					inventoryService.deleteLocalTransfer(transactionInstance)
				}
				else {
					transactionInstance.delete(flush: true)
				}
				flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'transaction.label', default: 'Transaction'), params.id])}"
				redirect(action: "listTransactions")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'transaction.label', default: 'Transaction'), params.id])}"
				redirect(action: "editTransaction", id: params.id)
			}
		}
		else {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'transaction.label', default: 'Transaction'), params.id])}"
			redirect(action: "listTransactions")
		}
	}
	
	
	def saveTransaction = {	
		log.debug "save transaction: " + params
		def transactionInstance = Transaction.get(params.id);
		// def inventoryInstance = Inventory.get(params.inventory.id);
		
		if (!transactionInstance) {
			transactionInstance = new Transaction();
		} 
		
		transactionInstance.properties = params
		
		// either save as a local transfer, or a generic transaction
		// (catch any exceptions so that we display "nice" error messages)
		Boolean saved = null
		if (!transactionInstance.hasErrors()) {
			try {
				//if (inventoryService.isValidForLocalTransfer(transactionInstance)) {
				//	saved = inventoryService.saveLocalTransfer(transactionInstance) 
				//}
				//else {
				saved = transactionInstance.save(flush:true)
				//}
			}
			catch (Exception e) {
				log.error("Unable to save transaction ", e);
			}
		}
		
		if (saved) {	
			flash.message = "${warehouse.message(code: 'inventory.transactionSaved.message')}"
			redirect(action: "editTransaction", id: transactionInstance?.id);
		}
		else { 		
			flash.message = "${warehouse.message(code: 'inventory.unableToSaveTransaction.message')}"
			def model = [ 
				transactionInstance : transactionInstance,
				productInstanceMap: Product.list().groupBy { it.category },
				transactionTypeList: TransactionType.list(),
				locationInstanceList: Location.list(),
				warehouseInstance: Location.get(session?.warehouse?.id)
			]
			render(view: "editTransaction", model: model);
		}	
	}
	
	
	/**
	 * Show the transaction.
	 */
	def showTransaction = {
		def transactionInstance = Transaction.get(params.id);
		if (!transactionInstance) {
			flash.message = "${warehouse.message(code: 'inventory.noTransactionWithId.message', args: [params.id])}"
			transactionInstance = new Transaction();
		}
		
		def localTransfer = LocalTransfer.findBySourceTransactionOrDestinationTransaction(transactionInstance, transactionInstance)
		
		def model = [
			transactionInstance : transactionInstance,
			localTransfer: localTransfer,
			//productInstanceMap: Product.list().groupBy { it.category },
			//transactionTypeList: TransactionType.list(),
			//locationInstanceList: Location.list(),
			//warehouseInstance: Location.get(session?.warehouse?.id)
		];
		
		render(view: "showTransaction", model: model);
	}
	
	/**
	* Show the transaction.
	*/
   def showTransactionDialog = {
	   def transactionInstance = Transaction.get(params.id);
	   if (!transactionInstance) {
		 	flash.message = "${warehouse.message(code: 'inventory.noTransactionWithId.message', args: [params.id])}"
		   transactionInstance = new Transaction();
	   }
	   
	   def model = [
		   transactionInstance : transactionInstance,
		   productInstanceMap: Product.list().groupBy { it.category },
		   transactionTypeList: TransactionType.list(),
		   locationInstanceList: Location.list(),
		   warehouseInstance: Location.get(session?.warehouse?.id)
	   ];
	   
	   render(view: "showTransactionDialog", model: model);
	   
   }
   
   	
	def confirmTransaction = { 
		def transactionInstance = Transaction.get(params?.id)
		if (transactionInstance?.confirmed) { 
			transactionInstance?.confirmed = Boolean.FALSE;
			transactionInstance?.confirmedBy = null;
			transactionInstance?.dateConfirmed = null;
					flash.message = "${warehouse.message(code: 'inventory.transactionHasBeenUnconfirmed.message')}"
		}
		else { 
			transactionInstance?.confirmed = Boolean.TRUE;
			transactionInstance?.confirmedBy = User.get(session?.user?.id);
			transactionInstance?.dateConfirmed = new Date();
			flash.message = "${warehouse.message(code: 'inventory.transactionHasBeenConfirmed.message')}"
		}
		redirect(action: "listAllTransactions")
	}
	
	
	def createTransaction = { 
		log.debug("createTransaction params " + params)		
		def command = new TransactionCommand();
		def warehouseInstance = Location.get(session?.warehouse?.id);
		def transactionInstance = new Transaction(params);
		//transactionInstance?.transactionDate = new Date();
		//transactionInstance?.source = warehouseInstance
		log.debug("transactionType " + transactionInstance?.transactionType)
		if (!transactionInstance?.transactionType) { 
			flash.message = "Cannot create transaction for unknown transaction type";			
			redirect(controller: "inventory", action: "browse")
		}
		
		// Process productId parameters from inventory browser
		if (params.product?.id) {
			def productIds = params.list('product.id')
			def products = productIds.collect { String.valueOf(it); }
			command.productInventoryItems = inventoryService.getInventoryItemsByProducts(warehouseInstance, products);
		}
		// If given a list of inventory items, we just return those inventory items
		else if (params?.inventoryItem?.id) { 
			def inventoryItemIds = params.list('inventoryItem.id')
			def inventoryItems = inventoryItemIds.collect { InventoryItem.get(String.valueOf(it)); }
			command?.productInventoryItems = inventoryItems.groupBy { it.product } 
		}
		
		command.transactionInstance = transactionInstance
		command.warehouseInstance = warehouseInstance

		command.quantityMap = inventoryService.getQuantityForInventory(warehouseInstance?.inventory);
		command.transactionTypeList = TransactionType.list();
		command.locationList = Location.list();
		
		[command : command]
		
	}

	
	
	/**
	 * Save a transaction that sets the current inventory level for stock.
	 */
	def saveInventoryTransaction = { TransactionCommand command ->
		log.debug ("Saving inventory adjustment " + params)

		def transaction = command?.transactionInstance;
		def warehouseInstance = Location.get(session?.warehouse?.id);
		def quantityMap = inventoryService.getQuantityForInventory(warehouseInstance?.inventory)
		
		// Item cannot have a negative quantity
		command.transactionEntries.each {
			if (it.quantity < 0) {
				transaction.errors.rejectValue("transactionEntries", "transactionEntry.quantity.invalid", [it?.inventoryItem?.lotNumber] as Object[], "")
			}
		}

		// Check to see if there are errors, if not save the transaction
		if (!transaction.hasErrors()) {
			try {
				// Add validated transaction entries to the transaction we want to persist
				command.transactionEntries.each {
					
					// FIXME Need to do some validation at this point
					//def onHandQuantity = quantityMap[it.inventoryItem]					
					// If the quantity changes, we record a new transaction entry
					//if (it.quantity != onHandQuantity) { 
					def transactionEntry = new TransactionEntry()
					transactionEntry.inventoryItem = it.inventoryItem
					transactionEntry.quantity = it.quantity
					transaction.addToTransactionEntries(transactionEntry)
					//}
				}
				
				// Validate the transaction object
				if (!transaction.hasErrors() && transaction.validate()) {
					transaction.save(failOnError: true)
					flash.message = "Successfully saved transaction " + transaction?.transactionNumber
					//redirect(controller: "inventory", action: "browse")
					redirect(controller: "inventory", action: "showTransaction", id: transaction?.id)
				}
			} catch (ValidationException e) {
				log.debug ("caught validation exception " + e)
			}
		}

		// After the attempt to save the transaction, there might be errors on the transaction
		if (transaction.hasErrors()) {
			log.debug ("has errors" + transaction.errors)
			
			// Get the list of products that the user selected from the inventory browser			
			if (params.product?.id) {
				def productIds = params.list('product.id')
				def products = productIds.collect { String.valueOf(it); }
				command.productInventoryItems = inventoryService.getInventoryItemsByProducts(warehouseInstance, products);
			}
			// If given a list of inventory items, we just return those inventory items
			else if (params?.inventoryItem?.id) {
				def inventoryItemIds = params.list('inventoryItem.id')
				def inventoryItems = inventoryItemIds.collect { InventoryItem.get(String.valueOf(it)); }
				command?.productInventoryItems = inventoryItems.groupBy { it.product }
			}
	
			
			// Populate the command object and render the form view.
			command.transactionInstance = transaction
			command.warehouseInstance = warehouseInstance
			command.quantityMap = quantityMap;
			command.transactionTypeList = TransactionType.list();
			command.locationList = Location.list();
			
			render(view: "createTransaction", model: [command: command]);
		}
	}

	/**
	 * Save a transaction that debits stock from the given inventory.
	 * 
	 * TRANSFER_OUT, CONSUMED, DAMAGED, EXPIRED
	 */
	def saveDebitTransaction = { TransactionCommand command ->
		log.debug ("Saving debit transactions " + params)
		
		log.debug("size: " + command?.transactionEntries?.size());
			
		def transaction = command?.transactionInstance;
		def warehouseInstance = Location.get(session?.warehouse?.id);
		def quantityMap = inventoryService.getQuantityForInventory(warehouseInstance?.inventory)
		
		// Quantity cannot be greater than on hand quantity
		command.transactionEntries.each {
			def onHandQuantity = quantityMap[it.inventoryItem];
			if (it.quantity > onHandQuantity) {
				transaction.errors.rejectValue("transactionEntries", "transactionEntry.quantity.invalid", [it?.inventoryItem?.lotNumber] as Object[], "")
			}
		}

		// Check to see if there are errors, if not save the transaction
		if (!transaction?.hasErrors()) {
			try {
				// Add validated transaction entries to the transaction we want to persist
				command.transactionEntries.each {
					if (it.quantity) { 
						def transactionEntry = new TransactionEntry()
						transactionEntry.inventoryItem = it.inventoryItem
						transactionEntry.quantity = it.quantity
						transaction.addToTransactionEntries(transactionEntry)
					}
				}
				
				// Validate the transaction object
				if (!transaction?.hasErrors() && transaction?.validate()) {
					transaction.save(failOnError: true)
					flash.message = "Successfully saved transaction " + transaction?.transactionNumber
					//redirect(controller: "inventory", action: "browse")
					redirect(controller: "inventory", action: "showTransaction", id: transaction?.id)
				}
			} catch (ValidationException e) {
				log.debug ("caught validation exception " + e)
			}
		}

		// After the attempt to save the transaction, there might be errors on the transaction
		if (transaction?.hasErrors()) {
			log.debug ("has errors" + transaction.errors)
			
			// Get the list of products that the user selected from the inventory browser
			if (params.product?.id) {
				def productIds = params.list('product.id')
				def products = productIds.collect { String.valueOf(it); }
				command.productInventoryItems = inventoryService.getInventoryItemsByProducts(warehouseInstance, products);
			}
			// If given a list of inventory items, we just return those inventory items
			else if (params?.inventoryItem?.id) {
				def inventoryItemIds = params.list('inventoryItem.id')
				def inventoryItems = inventoryItemIds.collect { InventoryItem.get(String.valueOf(it)); }
				command?.productInventoryItems = inventoryItems.groupBy { it.product }
			}
	
			// Populate the command object and render the form view.
			command.transactionInstance = transaction
			command.warehouseInstance = warehouseInstance
			command.quantityMap = quantityMap;
			command.transactionTypeList = TransactionType.list();
			command.locationList = Location.list();
			
			render(view: "createTransaction", model: [command: command]);
		}
	}

	
	
	/**
	 * Save a transaction that debits stock from the given inventory.
	 * 
	 * TRANSFER_IN
	 */
	def saveCreditTransaction = { TransactionCommand command ->

		log.debug("Saving credit transaction: " + params)
		def transactionInstance = command?.transactionInstance
		def warehouseInstance = Location.get(session?.warehouse?.id);
		def quantityMap = inventoryService.getQuantityForInventory(warehouseInstance?.inventory)

		
		// Quantity cannot be less than 0 or else it would be in a debit transaction
		command.transactionEntries.each {
			if (it.quantity < 0) {
				transactionInstance.errors.rejectValue("transactionEntries", "transactionEntry.quantity.invalid", [it?.inventoryItem?.lotNumber] as Object[], "")
			}
		}

		// We need to process each transaction entry to make sure that it has a valid inventory item (or we will create one if not)
		command.transactionEntries.each { 
			log.debug(it?.inventoryItem?.id + " " + it.product + " " + it.lotNumber + " " + it.expirationDate)
			if (!it.inventoryItem) { 
				// Find an existing inventory item for the given lot number and product and description
				log.debug("Find inventory item " + it.product + " " + it.lotNumber)
				def inventoryItem = inventoryService.findInventoryItemByProductAndLotNumber(it.product, it.lotNumber)
				log.debug("Found inventory item? " + inventoryItem)
				
				// If the inventory item doesn't exist, we create a new one
				if (!inventoryItem) {
					inventoryItem = new InventoryItem();
					inventoryItem.lotNumber = it.lotNumber
					inventoryItem.expirationDate = (it.lotNumber) ? it.expirationDate : null
					inventoryItem.product = it.product;
					log.debug("Save inventory item " + inventoryItem)
					if (inventoryItem.hasErrors() || !inventoryItem.save()) {
						inventoryItem.errors.allErrors.each { error->
							command.errors.reject("inventoryItem.invalid",
								[inventoryItem, error.getField(), error.getRejectedValue()] as Object[],
								"[${error.getField()} ${error.getRejectedValue()}] - ${error.defaultMessage} ");
							
						}
					}
				}
				it.inventoryItem = inventoryItem
			}
		}	
		
		// Now that all transaction entries in the command have inventory items, 
		// we need to create a persistable transaction entry
		command.transactionEntries.each {
			def transactionEntry = new TransactionEntry(inventoryItem: it.inventoryItem, quantity: it.quantity)			
			transactionInstance.addToTransactionEntries(transactionEntry)
		}

		// Check to see if there are errors, if not save the transaction
		if (!transactionInstance.hasErrors()) {
			try {
				// Validate the transaction object
				if (!transactionInstance.hasErrors() && transactionInstance.validate()) {
					transactionInstance.save(failOnError: true)
					flash.message = "Successfully saved transaction " + transactionInstance?.transactionNumber
					//redirect(controller: "inventory", action: "browse")
					redirect(controller: "inventory", action: "showTransaction", id: transactionInstance?.id)
				}
			} catch (ValidationException e) {
				log.debug ("caught validation exception " + e)
			}
		}

		// Should be true if a validation exception was thrown 
		if (transactionInstance.hasErrors()) {
			log.debug ("has errors" + transactionInstance.errors)
		
			// Get the list of products that the user selected from the inventory browser
			if (params.product?.id) {
				def productIds = params.list('product.id')
				def products = productIds.collect { String.valueOf(it); }
				command.productInventoryItems = inventoryService.getInventoryItemsByProducts(warehouseInstance, products);
			}
			// If given a list of inventory items, we just return those inventory items
			else if (params?.inventoryItem?.id) {
				def inventoryItemIds = params.list('inventoryItem.id')
				def inventoryItems = inventoryItemIds.collect { InventoryItem.get(String.valueOf(it)); }
				command?.productInventoryItems = inventoryItems.groupBy { it.product }
			}
			
			// Populate the command object and render the form view.
			//command.transactionInstance = transaction
			command.warehouseInstance = warehouseInstance
			command.quantityMap = quantityMap;
			command.transactionTypeList = TransactionType.list();
			command.locationList = Location.list();
			
			render(view: "createTransaction", model: [command: command]);
		}
	}
	
	/**
	 * Save a transaction that debits stock from the given inventory.
	 * 
	 * Not used at the moment.  
	 */
	def saveOutgoingTransfer = { Transaction transaction, TransactionCommand command ->
		log.debug ("Saving stock transfer " + params)

		def warehouseInstance = Location.get(session?.warehouse?.id);
		def quantityMap = inventoryService.getQuantityForInventory(warehouseInstance?.inventory)
		
		// Validate transaction entries
		log.debug ("BEGINNING")
		def transactionEntriesToRemove = []
		transaction.transactionEntries.each {
			log.debug("transaction entry " + it.inventoryItem + " " + it.quantity);
			def quantityOnHand = quantityMap[it.inventoryItem]
			if (quantityOnHand < it.quantity) {
				transaction.errors.rejectValue("transactionEntries", "transactionEntry.quantity.invalid", 
													[it?.inventoryItem?.lotNumber] as Object[], "")
			} 
			
			if (!it.quantity) { 
				log.debug ("remove " + it?.inventoryItem?.id + " from transaction entries")
				transactionEntriesToRemove.add(it)
			}
		}

		// Remove any transaction entries that are invalid
		transactionEntriesToRemove.each {
			log.debug("REMOVE " + it.inventoryItem + " " + it.quantity);
			transaction.transactionEntries.remove(it)
		}
		
		log.debug ("REMAINING")
		transaction.transactionEntries.each {
			log.debug("transaction entry " + it.inventoryItem + " " + it.quantity);
		}
		// Check to see if there are errors, if not save the transaction 		
		if (!transaction.hasErrors()) { 
			try { 
				// Validate the transaction object
				if (!transaction.hasErrors() && transaction.validate()) { 
					transaction.save(failOnError: true)				
					flash.message = "Successfully saved transaction " + transaction?.transactionNumber
					//redirect(controller: "inventory", action: "browse")
					redirect(controller: "inventory", action: "showTransaction", id: transaction?.id)					
				} 
			} catch (ValidationException e) { 
				log.debug ("caught validation exception " + e)
			}
		}

		// After the attempt to save the transaction, there might be errors on the transaction		
		if (transaction.hasErrors()) { 
			log.debug ("has errors" + transaction.errors)
			
			// Get the list of products that the user selected from the inventory browser
			if (params.product?.id) {
				def productIds = params.list('product.id')
				def products = productIds.collect { String.valueOf(it); }
				command.productInventoryItems = inventoryService.getInventoryItemsByProducts(warehouseInstance, products);
			}
			// If given a list of inventory items, we just return those inventory items
			else if (params?.inventoryItem?.id) {
				def inventoryItemIds = params.list('inventoryItem.id')
				def inventoryItems = inventoryItemIds.collect { InventoryItem.get(String.valueOf(it)); }
				command?.productInventoryItems = inventoryItems.groupBy { it.product }
			}
			
			// Populate the command object and render the form view.
			command.transactionInstance = transaction
			command.warehouseInstance = warehouseInstance
			command.quantityMap = quantityMap;
			command.transactionTypeList = TransactionType.list();
			command.locationList = Location.list();
			
			render(view: "createTransaction", model: [command: command]);
		}			
	}

	def editTransaction = {
        def startTime = System.currentTimeMillis()
		log.info "edit transaction: " + params
		def transactionInstance = Transaction.get(params?.id)
		def warehouseInstance = Location.get(session?.warehouse?.id);
		def products = Product.list();
		def inventoryItems = InventoryItem.findAllByProductInList(products)
		def model = [ 
			inventoryItemsMap: inventoryItems.groupBy { it.product } ,
			transactionInstance: transactionInstance?:new Transaction(),
			productInstanceMap: Product.list().groupBy { it?.category },
			transactionTypeList: TransactionType.list(),
			locationInstanceList: Location.list(),
			quantityMap: inventoryService.getQuantityForInventory(warehouseInstance?.inventory),
			warehouseInstance: warehouseInstance
        ]

        println "Edit transaction " + (System.currentTimeMillis() - startTime) + " ms"

		render(view: "editTransaction", model: model)

	}
	
	
	/**
	* TODO These are the same methods used in the inventory browser.  Need to figure out a better
	* way to handle this (e.g. through a generic ajax call or taglib).
	*/
	def removeCategoryFilter = {
		def category = Category.get(params?.categoryId)
		if (category)
			session.inventoryCategoryFilters.remove(category?.id);
		redirect(action: browse);
	}
	
	def clearAllFilters = {
		session.inventoryCategoryFilters = [];
		session.inventorySearchTerms = [];
		redirect(action: browse);
	}
	def addCategoryFilter = {
		def category = Category.get(params?.categoryId);
		if (category && !session.inventoryCategoryFilters.contains(category?.id))
			session.inventoryCategoryFilters << category?.id;	
		redirect(action: browse);
	}
	def narrowCategoryFilter = {
		def category = Category.get(params?.categoryId);
		session.inventoryCategoryFilters = []
		if (category && !session.inventoryCategoryFilters.contains(category?.id))
			   session.inventoryCategoryFilters << category?.id;
		redirect(action: browse);
	}
	def removeSearchTerm = {
		if (params.searchTerm)
			session.inventorySearchTerms.remove(params.searchTerm);
		redirect(action: browse);
	}
	
}

class ConsumptionCommand {
		String groupBy
		Date startDate
		Date endDate

		static constraints = {

		}
	}

class QuantityOnHandReportCommand {
    //Location location
    //def locations = ListUtils.lazyList([], FactoryUtils.instantiateFactory(Location))
    List<Location> locations = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Location.class));
    List dates = []
    List products = []
    Tag tag
    Date startDate = new Date()
    Date endDate
    String frequency


    static constraints = {
        locations(nullable: false,
                validator: { value, obj-> value?.size() >= 1 })
        startDate(nullable:false,
                validator: { value, obj-> !obj.endDate || value.before(obj.endDate) })
        endDate(nullable: false)
        frequency(nullable: false, blank: false)
    }
}

