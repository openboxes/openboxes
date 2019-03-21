/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse

import grails.converters.JSON
import grails.plugin.springcache.annotations.CacheFlush
import grails.plugin.springcache.annotations.Cacheable
import groovy.sql.Sql
import groovy.time.TimeCategory
import org.apache.commons.lang.StringEscapeUtils
import org.hibernate.FetchMode
import org.hibernate.annotations.Cache
import org.pih.warehouse.core.*
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventorySnapshot
import org.pih.warehouse.inventory.InventoryStatus
import org.pih.warehouse.jobs.CalculateHistoricalQuantityJob
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductGroup
import org.pih.warehouse.reporting.Indicator
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.shipping.Container
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.springframework.transaction.annotation.Transactional
import util.InventoryUtil
import java.text.NumberFormat
import java.text.SimpleDateFormat

class JsonController {

    def dataSource
    def dashboardService
	def inventoryService
	def productService
	def localizationService
	def shipmentService
    def reportService
	def messageSource
    def consoleService
    def userService
    def inventorySnapshotService

    def evaluateIndicator = {
        def indicator = Indicator.get(params.id)
        if (indicator) {
            def results = consoleService.eval(indicator.expression, true, request)
            render results.result
        }
        else {
            render "error"
        }
    }

    def calculateQuantityOnHand = {
        def location = Location.load(params.locationId)
        def products = Product.list(params)
        products.each { product ->
            inventoryService.calculateQuantityOnHand(product, location)
        }
    }


    def addToRequisitionItems = {
        log.info "addToRequisitionItems: ${params} "
        def json
        def requisition = Requisition.get(params?.requisition?.id)
        def product = Product.get(params?.product?.id);
        if (!requisition) {
            json = [success: false, errors: ["Unable to find requisition with ID ${params?.requisition?.id}"]]
        }
        else if (!product) {
            json = [success: false, errors: ["Unable to find product with ID ${params?.product?.id}"]]
        }
        else {
            def quantity = (params.quantity) ? params.int("quantity") : 1
            def orderIndex = (params.orderIndex) ? params.int("orderIndex") : 0
            def requisitionItem = new RequisitionItem()
            if (requisition) {
                requisitionItem.product = product
                requisitionItem.quantity = quantity
                requisitionItem.substitutable = false
                requisitionItem.orderIndex = orderIndex
                requisition.updatedBy = session.user
                requisition.addToRequisitionItems(requisitionItem)
                if (requisition.validate() && requisition.save(flush: true)) {
                    json = [success: true, data: requisition]
                } else {
                    json = [success: false, errors: requisitionItem.errors]
                }
            }
        }
        log.info(json as JSON)
        render json as JSON
    }

    def getTranslation = {
        def translation = getTranslation(params.text, params.src, params.dest)
        def json = [translation]
        render json as JSON
    }

    def getTranslation(String text, String source, String destination) {
        def translation = ""
        text = text.encodeAsURL()
        def email = "openboxes@pih.org"
        def password = "0p3nb0x3s"
        String urlString = "http://www.syslang.com/frengly/controller?action=translateREST&src=${source.encodeAsHTML()}&dest=${destination}&text=${text.encodeAsHTML()}&email=${email}&password=${password}"
        try {
            log.info "Before " + urlString
            def url = new URL(urlString)
            def connection = url.openConnection()
            log.info "content type; " + connection.contentType
            if(connection.responseCode == 200){
                def xml = connection.content.text
                log.info "XML: " + xml
                def root = new XmlParser(false, true).parseText(xml)
                translation = root.translation.text()
            }
            else {
                log.info "connection " + connection.responseCode

            }
        } catch (Exception e) {
            log.error("Error trying to translate using syslang API ", e);
            throw new ApiException(message: "Unable to query syslang API: " + e.message)
        }
        return translation
    }

	def getLocalization = {
		log.info "get localization " + params
		def localization = Localization.get(params.id)
		// Get the localization from the database
		// Create a new localization based on the message source

		if (!localization) {
            localization = new Localization();

            // Get translation from message source
			def message = messageSource.getMessage(params.code, null, params.resolvedMessage, session?.user?.locale?:"en")
            log.info "get translation for code " + params.code + ", " + session?.user?.locale + " = " + message

            //try {
            //    localization.translation = getTranslation(message, "en", session?.user?.locale?.toString()?:"en")
            //} catch (Exception e) {
                //localization.translation = message;
            //}
            localization.translation = message
			localization.code = params.code
			localization.text = message

			//localization.args = []
			localization.locale = session.user.locale
		}

        // If the translation message is empty, set it equal to the same as the localization text
        if (!localization.translation)
            localization.translation = localization.text

        log.info "localization.toJson() = " + (localization.toJson() as JSON)


		render localization.toJson() as JSON;
	}

	def saveLocalization = {
		log.info "Save localization " + params
		def data = request.JSON
		log.info "Data " + data
		log.info "ID: " + data.id
		def locale = session?.user?.locale
		def localization = Localization.get(data?.id?.toString())
        log.info "found localization " + localization
		if (!localization) {
            log.info "Nope.  Looking by code and locale"
			localization = Localization.findByCodeAndLocale(data.code, locale?.toString())
		    if (!localization) {
                log.info "Nope. Creating empty localization "
			    localization = new Localization();
		    }
        }

		//localization.properties = data
		localization.text = data.translation
		localization.code = data.code
		localization.locale = locale

        log.info localization.id
        log.info localization.code
        log.info localization.text
        log.info localization.locale
		def jsonResponse = []

        // Attempt to save localization
		if (!localization.hasErrors() && localization.save()) {
			jsonResponse = [success: true, data: localization.toJson()]
		}
		else {
			jsonResponse = [success: false, errors: localization.errors]
		}
		log.info(jsonResponse as JSON)
		render jsonResponse as JSON
		//def localization = new Localization(params)
		return true
	}

    def deleteLocalization = {
        log.info "get localization " + params
        // Get the localization from the database
        def jsonResponse = []
        def localization = Localization.get(params.id)
        try {
            if (localization) {
                localization.delete();
                jsonResponse = [success: true, message: "successfully deleted translation"]
            }
        } catch (Exception e) {
            jsonResponse = [success: false, message: e.message]
        }
        render jsonResponse as JSON
    }

    @Cacheable("inventoryBrowserCache")
	def getQuantityToReceive = {
		def product = Product.get(params?.product?.id)
		def location = Location.get(params?.location?.id)
		def quantityToReceive = inventoryService.getQuantityToReceive(location, product)
		//println "quantityToReceive(" + params + "): " + quantityToReceive
		render (quantityToReceive?:"0")
	}

    @Cacheable("inventoryBrowserCache")
	def getQuantityToShip = {
		def product = Product.get(params?.product?.id)
		def location = Location.get(params?.location?.id)
		def quantityToShip = inventoryService.getQuantityToShip(location, product)
		//println "quantityToShip(" + params + "): " + quantityToShip
		render (quantityToShip?:"0")
	}

    @Cacheable("inventoryBrowserCache")
	def getQuantityOnHand = {
		def product = Product.get(params?.product?.id)
		def location = Location.get(params?.location?.id)
		def quantityOnHand = inventoryService.getQuantityOnHand(location, product)
		//println "quantityOnHand(" + params + "): " + quantityOnHand
        //println "${createLink(controller:'inventoryItem', action: 'showStockCard', id: product.id)}"
		render (quantityOnHand?:"0")
	}
    @Cacheable("inventoryBrowserCache")
    def flushInventoryBrowserCache = {
        redirect(controller: "inventory", action: "browse")
    }


    @Cacheable("dashboardCache")
    def getGenericProductSummary = {
        def startTime = System.currentTimeMillis()
        def location = Location.get(session?.warehouse?.id)
        def genericProductByStatusMap = inventoryService.getGenericProductSummary(location)

        // Convert from map of objects to map of statistics
        genericProductByStatusMap.each { k, v ->
            genericProductByStatusMap[k] = v?.size()?:0
        }

        render ([elapsedTime: (System.currentTimeMillis()-startTime),
                totalCount:genericProductByStatusMap.values().size(),
                genericProductByStatusMap: genericProductByStatusMap] as JSON)

    }


    // FIXME Remove - Only used for comparison
    def getQuantityByProductMap = {
        def location = Location.get(session?.warehouse?.id)
        def quantityMap = inventoryService.getQuantityByProductMap(location.inventory)

        render quantityMap as JSON
    }


    // FIXME Remove - Only used for compaison
    def getQuantityByProductMap2 = {
        def location = Location.get(session?.warehouse?.id)
        def quantityMap = inventoryService.getCurrentInventory(location)

        render quantityMap as JSON
    }

    def getQuantityByInventoryItem = {
        def location = Location.get(session?.warehouse?.id)
        def quantityMap = inventoryService.getQuantityForInventory(location.inventory)

        quantityMap = quantityMap.sort()


        render quantityMap as JSON
    }


    def getQuantityByInventoryItem2 = {
        def location = Location.get(session?.warehouse?.id)
        def quantityMap = inventoryService.getQuantityOnHandByInventoryItem(location)

        quantityMap = quantityMap.sort()


        render quantityMap as JSON
    }



    @Cacheable("dashboardCache")
    def getDashboardAlerts = {
        def location = Location.get(session?.warehouse?.id)
        def dashboardAlerts = dashboardService.getDashboardAlerts(location)

        render dashboardAlerts as JSON
    }

    @Cacheable("dashboardCache")
    def getDashboardExpiryAlerts = {
        def location = Location.get(session?.warehouse?.id)
        def map = dashboardService.getExpirationSummary(location)
        render map as JSON
    }

    @Cacheable("dashboardCache")
    def getTotalStockValue = {
        def location = Location.get(session?.warehouse?.id)
        def result = dashboardService.getTotalStockValue(location)
        def totalValue = g.formatNumber(number: result.totalStockValue)
        def lastUpdated = inventorySnapshotService.getLastUpdatedInventorySnapshotDate()
        lastUpdated = "Last updated " + prettytime.display([date: lastUpdated, showTime: true, capitalize: false]) + "."
        def data = [
                lastUpdated: lastUpdated,
                anotherAttr: "anotherValue",
                totalStockValue:result.totalStockValue,
                hitCount: result.hitCount,
                missCount: result.missCount,
                totalCount: result.totalCount,
                totalValue: totalValue]
        render data as JSON
    }

    def getStockValueByProduct = {
        def location = Location.get(session?.warehouse?.id)
        def result = dashboardService.getTotalStockValue(location)
        def hasRoleFinance = userService.hasRoleFinance(session?.user)

        def stockValueByProduct = []
        result.stockValueByProduct.sort { it.value }.reverseEach { Product product, value ->
            value = g.formatNumber(number: value, format: "#######.00")
            stockValueByProduct << [
                    id: product.id,
                    productCode: product.productCode,
                    productName: product.name,
                    unitPrice: hasRoleFinance ? product.pricePerUnit : null,
                    totalValue: hasRoleFinance ? value : null
            ]
        }

        render ([aaData: stockValueByProduct] as JSON)
    }


    @CacheFlush("dashboardTotalStockValueCache")
    def refreshTotalStockValue = {
        render ([success:true] as JSON)
    }



    @Cacheable("dashboardCache")
    def getReconditionedStockCount = {
        def location = Location.get(params?.location?.id)
        def results = dashboardService.getReconditionedStock(location)
        render (results?.keySet()?.size()?:"0")
    }

    @Cacheable("dashboardCache")
    def getTotalStockCount = {
        def location = Location.get(params?.location?.id)
        def results = dashboardService.getTotalStock(location)
        render (results?.keySet()?.size()?:"0")
    }

    @Cacheable("dashboardCache")
    def getInStockCount = {
        def location = Location.get(params?.location?.id)
        def results = dashboardService.getInStock(location)
        //println "in stock: " + results
        render (results?.keySet()?.size()?:"0")
    }

    @Cacheable("dashboardCache")
    def getOutOfStockCount = {
        def location = Location.get(params?.location?.id)
        def results = dashboardService.getOutOfStock(location)
        render (results?.keySet()?.size()?:"0")
    }

    @Cacheable("dashboardCache")
    def getOverStockCount = {
        def location = Location.get(params?.location?.id)
        def results = dashboardService.getOverStock(location)
        render (results?.keySet()?.size()?:"0")
    }

    @Cacheable("dashboardCache")
    def getLowStockCount = {
		def location = Location.get(params?.location?.id)
		def results = dashboardService.getLowStock(location)
        //println "low: " + results
		render (results?.keySet()?.size()?:"0")
	}

    @Cacheable("dashboardCache")
	def getReorderStockCount = {
		def location = Location.get(params?.location?.id)
		def results = dashboardService.getReorderStock(location)
        //println "reorder: " + results
		render (results?.keySet()?.size()?:"0")
	}

    @Cacheable("dashboardCache")
	def getExpiringStockCount = {
		def daysUntilExpiry = Integer.valueOf(params.daysUntilExpiry)
		def location = Location.get(params?.location?.id)
		def results = dashboardService.getExpiringStock(null, location, daysUntilExpiry)
		render ((results)?results?.size():"0")
	}

    @Cacheable("dashboardCache")
	def getExpiredStockCount = {
		//println "expired stock count " + params
		def location = Location.get(params?.location?.id)
		def results = dashboardService.getExpiredStock(null, location)
		render ((results)?results.size():"0")
	}


    def getInventorySnapshots = {

        def location = Location.get(params?.location?.id)
        def results = inventorySnapshotService.findInventorySnapshotByLocation(location)

        def inStockCount = results.findAll { it.quantityOnHand > 0 && it.status == InventoryStatus.SUPPORTED }.size()
        def lowStockCount = results.findAll { it.quantityOnHand > 0 && it.quantityOnHand <= it.minQuantity && it.status == InventoryStatus.SUPPORTED }.size()
        def reoderStockCount = results.findAll { it.quantityOnHand > it.minQuantity && it.quantityOnHand <= it.reorderQuantity && it.status == InventoryStatus.SUPPORTED }.size()
        def overStockCount = results.findAll { it.quantityOnHand > it.reorderQuantity && it.quantityOnHand <= it.maxQuantity && it.status == InventoryStatus.SUPPORTED }.size()
        def stockOutCount = results.findAll { it.quantityOnHand <= 0 && it.status == InventoryStatus.SUPPORTED }.size()

        def totalCount = results.size()


        render ([
                summary: [
                        totalCount: totalCount,
                         inStockCount: inStockCount,
                         lowStockCount: lowStockCount,
                         reoderStockCount: reoderStockCount,
                         overStockCount: overStockCount,
                         stockOutCount:stockOutCount
                ],
                details: [results:results]
        ] as JSON)//results.collect { [productCode: it.productCode, quantityOnHand: it.quantityOnHand, ]}

    }

    def getQuantityOnHandMap = {
        def startTime = System.currentTimeMillis()
        //def location = Location.get(session?.warehouse?.id)
        //def results = inventoryService.getQuantityByProductMap(location.inventory)
        def results = inventoryService.getQuantityByProductMap(session?.warehouse?.id)

        def elapsedTime = System.currentTimeMillis() - startTime
        render ([count:results.size(),elapsedTime:elapsedTime,results:results] as JSON)
    }


    // FIXME Remove if not used
//    Map<Product,Integer> getQuantityOnHand(Inventory inventory) {
//        return inventoryService.getQuantityByProductMap(inventory)
//    }


    def findProductCodes = {
        def searchTerm = params.term + "%";
        def c = Product.createCriteria()
        def products = c.list {
            eq("active", true)
            or {
                ilike("productCode", searchTerm)
                ilike("name", searchTerm)
            }
        }

        //"id": "Netta rufina", "label": "Red-crested Pochard", "value": "Red-crested Pochard" },
        def results = products.unique().collect { [ value: it.productCode, label: it.productCode + " " + it.name ] }
        render results as JSON;
    }


	def findTags = {
		def searchTerm = "%" + params.term + "%";
		def c = Tag.createCriteria()
		def tags = c.list {
			projections { property "tag" }
			ilike("tag", searchTerm)
		}

		def results = tags.unique().collect { [ value: it, label: it ] }
		render results as JSON;
	}

	def autoSuggest = {
        log.info "autoSuggest: " + params
		def searchTerm = "%" + params.term + "%";
		def c = Product.createCriteria()
		def results = c.list {
			projections {
				property "${params.field}"
			}
            eq("active", true)
			ilike("${params.field}", searchTerm)
		}
		results = results.unique().collect { [ value: it, label: it ] }
		render results as JSON;
	}

    def autoSuggestProductGroups = {
        log.info "autoSuggest: " + params
        def searchTerms = params.term.split(" ")
        //def searchTerm = "%" + params.term + "%";
        def c = ProductGroup.createCriteria()
        def results = c.list {
            projections {
                property "name"
            }
            and {
                searchTerms.each { searchTerm ->
                    ilike("name", "%" + searchTerm + "%" )
                }
            }
        }
        results = results.unique().collect { [ value: it, label: it ] }
        render results as JSON;
    }


	def findProductNames = {
		def searchTerm = "%" + params.term + "%";
		def c = Product.createCriteria()
		def productNames = c.list {
			projections {
				property "name"
			}
            eq("active", true)
			ilike("name", searchTerm)
		}

		def results = productNames.unique().collect { [ value: it, label: it ] }
		render results as JSON;
	}

	def findPrograms = {
        log.info "find programs " + params
		def searchTerm = params.term + "%";
		def c = Requisition.createCriteria()

		def names = c.list {
			projections {
				property "recipientProgram"
			}
			ilike("recipientProgram", searchTerm)
		}
		// Try again
		if (names.isEmpty()) {
			searchTerm = "%" + params.term + "%";
			c = Requisition.createCriteria()
			names = c.list {
				projections {
					property "recipientProgram"
				}
				ilike("recipientProgram", searchTerm)
			}
		}

		if (names.isEmpty()) {
			names = []
			names << params.term
		}

		def results = names.collect { [ value: it, label: it ] }
		render results as JSON;
	}


	def getInventoryItem = {
		render InventoryItem.get(params.id).toJson() as JSON;
	}

	def getQuantity = {
		log.info params
		def quantity = 0
		def location = Location.get(session.warehouse.id);
		def lotNumber = (params.lotNumber) ? (params.lotNumber) : "";
		def product = (params.productId) ? Product.get(params.productId) : null;

		def inventoryItem = inventoryService.findInventoryItemByProductAndLotNumber(product, lotNumber);
		if (inventoryItem) {
			quantity = inventoryService.getQuantity(location?.inventory, inventoryItem)
		}
		log.info "quantity by lotnumber '" + lotNumber + "' and product '" + product + "' = " + quantity;
		render quantity ?: "N/A";
	}

	def sortContainers = {
		def container
		params.get("container[]").eachWithIndex { id, index ->
			container = Container.get(id)
			container.sortOrder = index
			container.save(flush:true);
            log.info ("container " + container.name + " saved at index " + index)
		}
		container.shipment.save(flush:true)
        //container.shipment.refresh()

		render(text: "", contentType: "text/plain")
	}

    def sortRequisitionItems = {
        log.info "sort requisition items " + params

        def requisitionItem
        params.get("requisitionItem[]").eachWithIndex { id, index ->
            requisitionItem = RequisitionItem.get(id)
            requisitionItem.orderIndex = index
            requisitionItem.save(flush:true);
            log.info ("requisitionItem " + id + " saved at index " + index)
        }
        requisitionItem.requisition.refresh()
        render(text: "", contentType: "text/plain")
    }

	/**
	 * Ajax method for the Record Inventory page.
	 */
	def getInventoryItems = {
		log.info params
		def productInstance = Product.get(params?.product?.id);
		def inventoryItemList = inventoryService.getInventoryItemsByProduct(productInstance)
		render inventoryItemList as JSON;
	}


	/**
	 * Returns inventory items for the given location, lot number, and product.
	 */
	def findInventoryItems = {
		log.info params
        long startTime = System.currentTimeMillis()
		def inventoryItems = []
		def location = Location.get(session.warehouse.id);
		if (params.term) {

			// Improved the performance of the auto-suggest by moving
			def tempItems = inventoryService.findInventoryItems(params.term)

			if (tempItems) {
                def maxResults = grailsApplication.config.openboxes.shipping.search.maxResults?:1000
                if (tempItems.size() > maxResults) {
                    def message = "${warehouse.message(code:'inventory.tooManyItemsFound.message', default: 'Found {1} items for term "{0}". Too many items so disabling QoH. Try searching by product code.', args: [params.term, tempItems.size()])}"
                    inventoryItems << [id: 'null', value: message]
                }
                else {
                    def quantitiesByInventoryItem = [:]
                    quantitiesByInventoryItem = inventoryService.getQuantityByInventoryItemMap(location, tempItems*.product)

                    tempItems.each {
                        def quantity = quantitiesByInventoryItem[it]
                        quantity = (quantity) ?: 0

                        def localizedName = localizationService.getLocalizedString(it.product.name)
                        localizedName = (it.product.productCode ?: " - ") + " " + localizedName
                        inventoryItems = inventoryItems.sort { it.expirationDate }

                        if (quantity > 0) {

                            String description = (it?.lotNumber ?: "${g.message(code:'default.noLotNumber.label')}") +
                                    " - ${g.message(code:'default.expires.label')} " + (it?.expirationDate?.format("MMM yyyy")?:"${g.message(code:'default.never.label')}") +
                                    " - ${quantity} ${it?.product?.unitOfMeasure ?: 'EA'}"

                            String label = "${localizedName} x ${quantity?:0} ${it?.product?.unitOfMeasure?:'EA'}"

                            String expirationDate = it?.expirationDate ?
                                    g.formatDate(date: it.expirationDate, format: "MMM yyyy") :
                                    g.message(code:'default.never.label')

                            inventoryItems << [
                                    id            : it.id,
                                    value         : it.lotNumber,
                                    imageUrl      : "${resource(dir: 'images', file: 'default-product.png')}",
                                    label         : label,
                                    description   : description,
                                    valueText     : it.lotNumber,
                                    lotNumber     : it.lotNumber,
                                    product       : it.product.id,
                                    productId     : it.product.id,
                                    productName   : localizedName,
                                    quantity      : quantity,
                                    expirationDate: expirationDate
                            ]
                        }
                    }

                    def count = inventoryItems.size()
                    def responseTime = System.currentTimeMillis() - startTime
                    inventoryItems.add(0, [id: 'null', value: "Searching for '${params.term}'", description: "Returned ${count} items in ${responseTime} ms"])

                }
			}
		}
		if (inventoryItems.size() == 0) {
			def message = "${warehouse.message(code:'inventory.noItemsFound.message', args: [params.term])}"
			inventoryItems << [id: 'null', value: message]
		}
		else {
			inventoryItems = inventoryItems.sort { it.expirationDate }
		}

		render inventoryItems as JSON;
	}

	def findLotsByName = {
		log.info params
		// Constrain by product id if the productId param is passed in
		def items = new TreeSet();
		if (params.term) {
			def searchTerm = "%" + params.term + "%";
			items = InventoryItem.withCriteria {
				and {
					or {
						ilike("lotNumber", searchTerm)
					}
					// Search within the inventory items for a specific product
					if (params?.productId) {
						eq("product.id", params.productId)
					}
				}
			}

			def warehouse = Location.get(session.warehouse.id);
			def quantitiesByInventoryItem = inventoryService.getQuantityForInventory(warehouse?.inventory)

			if (items) {
				items = items.collect() { item ->
					def quantity = quantitiesByInventoryItem[item]
					quantity = (quantity) ?: 0

					def localizedName = localizationService.getLocalizedString(item.product.name)

					[
						id: item.id,
						value: item.lotNumber,
						label:  localizedName + " --- Item: " + item.lotNumber + " --- Qty: " + quantity + " --- ",
						valueText: item.lotNumber,
						lotNumber: item.lotNumber,
						expirationDate: item.expirationDate
					]
				}
			}
		}
		render items as JSON;
	}


    def createPerson = {
        log.info ("createPerson" + params)
        def data = [id: null, label: "Unable to create person with name " + params.name]

        def names = params.name.split(" ")
        if (names) {
            Person person
            if (names.length == 1) {
                throw new Exception("Person must have at least two names")
            }
            if (names.length == 2) {
                person = new Person(firstName: names[0], lastName: names[1])
            }
            else if (names.length == 3) {
                person = new Person(firstName: names[0] + " " + names[1], lastName: names[2])
            }
            else {
                throw new Exception("Person must have at most three names")
            }

            if (person) {
                person.save(flush: true)
                data = [id: person.id, value: person.name]
            }
        }

        render data as JSON

    }

	def findPersonByName = {
		log.info "findPersonByName: " + params
		def items = new TreeSet();
		try {

			if (params.term) {

				def terms = params.term.split(" ")
				for (term in terms) {
					items = Person.withCriteria {
						or {
							ilike("firstName", term + "%")
							ilike("lastName", term + "%")
							ilike("email", term + "%")
						}
					}
				}

				if (items) {
					items.unique();
                    items = items.collect() {

						[
                            id: it.id,
                            label:  it.name,
                            description: it?.email,
                            value: it.id,
							valueText: it.name,
							desc: (it?.email) ? it.email : "",
						]
					}
                }
				else {
                    /*
                    response.status = 404;
                    render "${warehouse.message(code: 'person.doesNotExist.message', args: [params.term])}"
                    */
//					def item =  [
//						value: "null",
//						valueText : params.term,
//						label: "${warehouse.message(code: 'person.doesNotExist.message', args: [params.term])}",
//						desc: params.term,
//						icon: ""
//					];
//					items.add(item)
				}
                items.add([id: "new", label: 'Create new record for ' + params.term, value: null, valueText: params.term])
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
        log.info "returning ${items?.size()} items: " + items
        render items as JSON;


	}

	def findProductByName = {

		log.info("find products by name " + params)
		def dateFormat = new SimpleDateFormat(Constants.SHORT_MONTH_YEAR_DATE_FORMAT);
		def products = new TreeSet();

		if (params.term) {
			// Match full name

            products = Product.withCriteria {
                ilike("productCode", params.term + "%")
            }
            if (!products) {
                products = Product.withCriteria {
                    ilike("name", "%" + params.term + "%")
                }
            }
		}

		def location = Location.get(params.warehouseId);
		log.info ("warehouse: " + location);
		def quantityMap = [:]

        if (false) {
		    quantityMap = inventoryService.getQuantityForInventory(location?.inventory)
        }

		// FIXME Needed to create a new map with inventory item id as the index
		// in order to get the quantity below.  For some reason, the inventory item
		// object was getting toString()'d when used below as a key and therefore
		// the keys were mismatched and the quantity was always null.
		def idQuantityMap = [:]
		quantityMap.keySet().each {
			idQuantityMap[it.id] = quantityMap[it]
		}

		// Convert from products to json objects
		if (products) {
			// Make sure items are unique
			//products.unique();
			products = products.collect() { product ->
				def productQuantity = 0;
				// We need to check to make sure this is a valid product
				def inventoryItemList = []
				if (product.id) {
					def inventoryItems = InventoryItem.findAllByProduct(product);
					inventoryItemList = inventoryItems.collect() { inventoryItem ->
						// FIXME Getting the quantity from the inventory map does not work at the moment
						def quantity = idQuantityMap[inventoryItem.id]?:0;

						// Create inventory items object
						//if (quantity > 0) {
							[
								id: inventoryItem.id?:0,
								lotNumber: (inventoryItem?.lotNumber)?:"",
								expirationDate: (inventoryItem?.expirationDate) ?
									(dateFormat.format(inventoryItem?.expirationDate)) :
									"${warehouse.message(code: 'default.never.label')}",
								quantity: quantity
							]
						//}
					}

					// Sort using First-expiry, first out policy
					inventoryItemList = inventoryItemList.sort { it?.expirationDate }
				}

				def localizedName = localizationService.getLocalizedString(product.name)


				// Convert product attributes to JSON object attributes
				[
                    id: product?.id,
					product: product,
					category: product?.category,
					quantity: productQuantity,
                    productCode: product?.productCode,
					value: product.id,
					label: product?.productCode + " " + localizedName,
					valueText: localizedName,
					desc: product.description,
					inventoryItems: inventoryItemList,
					icon: "none"
				]
			}
		}

		if (products.size() == 0) {
			products << [ value: null, label: warehouse.message(code:'product.noProductsFound.message')]
		}

		log.info "Returning " + products.size() + " results for search " + params.term
		render products as JSON;
	}

	def findRequestItems = {

		log.info("find request items by name " + params)

		//def items = new TreeSet();
		def items = []
		if (params.term) {
			// Match full name
			def products = Product.withCriteria {
				ilike("name", "%" + params.term + "%")
			}
			items.addAll(products)

			def productGroups = ProductGroup.withCriteria {
				ilike("name", "%" + params.term + "%")
			}
			productGroups.each { items << [id: it.id, name: it.name, class: it.class] }
			//items.addAll(productGroups)


			def categories = Category.withCriteria {
				ilike("name", "%" + params.term + "%")
			}
			items.addAll(categories)
		}

		// Convert from products to json objects
		if (items) {
			// Make sure items are unique
			//items.unique();
			items = items.collect() { item ->
				def type = item.class.simpleName
				def localizedName = localizationService.getLocalizedString(item.name)
				// Convert product attributes to JSON object attributes
				[
					value: type + ":" + item.id,
					type: type,
					label: localizedName + "(" + type + ")",
					valueText: localizedName,
				]
			}
		}

		if (items.size() == 0) {
			items << [ value: null, label: warehouse.message(code:'product.noProductsFound.message')]
			//items << [value: null, label: params.term]
		}

		log.info "Returning " + items.size() + " results for search " + params.term
		render items as JSON;
	}



    def searchProductPackages = {

        log.info "Search product packages " + params
        def location = Location.get(session.warehouse.id);
        def results = productService.searchProductAndProductGroup(params.term)
        if (!results) {
            results = productService.searchProductAndProductGroup(params.term, true)
        }

        def productIds = results.collect { it[0] }
        def products = productService.getProducts(productIds as String[])

        //def quantities = inventoryService.getQuantityForProducts(location.inventory, productIds)
        // To reference quantities ...
        // quantities[productData[0]]
        def result = []
        def value = ""
        def productPackageName = ""
        products.each { product ->
            //println "productData " + productData
            //if(productData[3] && !result.any{it.id == productData[3] && it.type == "ProductGroup"})
            //result.add([id: productData[3], value: productData[2], type:"ProductGroup", group: ""])
            //result.add([id: productData[0], value: productData[2] + " - " + productData[1], type:"Product", quantity: null, group: ""])
            productPackageName = "EA/1"
            value = product?.productCode + " " + product?.name?.trim() + " (" + productPackageName + ")"
            value = value.trim()

            // Add the EACH level items
            result.add([id: product.id, value: value, type: "Product", quantity: null, group: null])

            // Add the package level items
            /*
            product.packages.each { pkg ->
                productPackageName = pkg?.uom?.code + "/" + pkg?.quantity;
                value = product?.productCode + " " + product?.name?.trim() + " (" + productPackageName + ")"
                value = value.trim()
                result.add([id: product.id, value: value, type: "Product", quantity: null, group: null,
                        productPackageId: pkg?.id, productPackageName: productPackageName, productPackageQty: pkg?.quantity])
            }
            */

        }
        log.info result
        render result.sort { "${it.group}${it.value}" } as JSON
    }



    def searchProduct = {
        def location = Location.get(session.warehouse.id);
        def results = productService.searchProductAndProductGroup(params.term)
        if (!results) {
            results = productService.searchProductAndProductGroup(params.term, true)
        }

        def productIds = results.collect { it[0] }
        def products = productService.getProducts(productIds as String[])

        //def quantities = inventoryService.getQuantityForProducts(location.inventory, productIds)
        // To reference quantities ...
        // quantities[productData[0]]
        def result = []
        def value = ""
        def productPackageName = ""
        products.each { product ->
            //println "productData " + productData
            //if(productData[3] && !result.any{it.id == productData[3] && it.type == "ProductGroup"})
            //result.add([id: productData[3], value: productData[2], type:"ProductGroup", group: ""])
            //result.add([id: productData[0], value: productData[2] + " - " + productData[1], type:"Product", quantity: null, group: ""])
            productPackageName = "EA/1"
            value = product?.productCode + " " + product?.name?.trim() + " (" + productPackageName + ")"
            value = value.trim()

            // Add the EACH level items
            result.add([id: product.id, value: value, type: "Product", quantity: null, group: null])

            // Add the package level items
            /*
            product.packages.each { pkg ->
                productPackageName = pkg?.uom?.code + "/" + pkg?.quantity;
                value = product?.productCode + " " + product?.name?.trim() + " (" + productPackageName + ")"
                value = value.trim()
                result.add([id: product.id, value: value, type: "Product", quantity: null, group: null,
                        productPackageId: pkg?.id, productPackageName: productPackageName])

            }
            */

        }
        log.info result
        render result.sort { "${it.group}${it.value}" } as JSON
    }


	def searchPersonByName = {
		def items = []
		def terms = params.term?.split(" ")
		terms?.each{ term ->
			def result = Person.withCriteria {
				or {
					ilike("firstName", term + "%")
					ilike("lastName", term + "%")
					ilike("email", term + "%")
				}
			}
			items.addAll(result)
		}
		items.unique{ it.id }
		def json = items.collect{
			[id: it.id, value: it.name, label: it.name+ " " + it.email]
		}
		render json as JSON
	}


	def globalSearch = {

        def minLength = grailsApplication.config.openboxes.typeahead.minLength
        if (params.name && params.name.size()<minLength) {
            render([:] as JSON)
            return
        }

		def terms = params.term?.split(" ")
        def location = Location.get(session.warehouse.id)

        // Get all products that match terms
        def products = productService.searchProducts(terms, [])

        products = products.unique()

        // Only calculate quantities if there are products - otherwise this will calculate quantities for all products in the system
        def quantityMap = products ? getQuantityByProductMapCached(location, products) : [:]

        if (terms) {
            products = products.sort() {
                a, b ->
                    (terms.any { a?.productCode?.contains(it) ? a.productCode : null }) <=> (terms.any { b?.productCode.contains(it) ? b.productCode : null }) ?:
                        (terms.any { a?.name.contains(it) ? a.name : null }) <=> (terms.any { b?.name.contains(it) ? b.name : null })
            }
            products = products.reverse()
        }

        def items = []
        items.addAll(products)
		items.unique{ it.id }
		def json = items.collect {
            def quantity = quantityMap[it] ?: 0
            def type = it.class.simpleName.toLowerCase()
            [
                    id   : it.id,
                    type : it.class,
                    url  : request.contextPath + "/" + type + "/redirect/" + it.id,
                    value: it.name,
                    label: it.productCode + " " + it.name + " x " + quantity + " " + (it?.unitOfMeasure ?: "EA")
            ]
        }
		render json as JSON
	}

    /**
     * Caches the quantity on hand values indexed by product.
     *
     * @param location
     * @param products
     * @return
     */
    //@Cacheable("dashboardCache")
    Map<Product, Integer> getQuantityByProductMapCached(Location location, List<Product> products) {
        return inventoryService.getQuantityByProductMap(location.inventory, products)
    }

    @CacheFlush("quantityOnHandCache")
    def flushQuantityOnHandCache = {
        redirect(controller:"inventory", action: "analyze")
    }

    @Cacheable("quantityOnHandCache")
    def calculateQuantityOnHandByProduct = {

        log.info "Calculating quantity on hand by product ..." + params

        def items = []
        def startTime = System.currentTimeMillis()
        def location = Location.get(session.warehouse.id)
        def quantityMap = inventoryService.getQuantityByProductMap(session.warehouse.id)
        def inventoryStatusMap = inventoryService.getInventoryStatusAndLevel(location)
        quantityMap.each { Product product, value ->
            def inventoryLevel = inventoryStatusMap[product]?.inventoryLevel
            def status = inventoryStatusMap[product]?.inventoryStatus
            def quantity = inventoryStatusMap[product]?.quantity?:0

            items << [
                    id:product.id,
                    name: product.name,
                    status: status,
                    productCode: product.productCode,
                    genericProduct:product?.genericProduct?.name?:"Empty",
                    inventoryLevel: inventoryLevel,
                    minQuantity: inventoryLevel?.minQuantity?:0,
                    maxQuantity: inventoryLevel?.maxQuantity?:0,
                    reorderQuantity: inventoryLevel?.reorderQuantity?:0,
                    unitOfMeasure: product.unitOfMeasure,
                    unitPrice: product.pricePerUnit?:0,
                    onHandQuantity:value?:0.0,
                    totalValue: (product.pricePerUnit?:0) * (value?:0)
            ]
        }


        def elapsedTime = (System.currentTimeMillis() - startTime) / 1000

        def inStockCount = items.findAll { it.status == "IN_STOCK" }.size()
        def reorderStockCount = items.findAll { it.status == "REORDER" }.size()
        def lowStockCount = items.findAll { it.status == "LOW_STOCK" }.size()
        def outOfStockCount = items.findAll { it.status == "STOCK_OUT" }.size()
        def overStockCount = items.findAll { it.status == "OVERSTOCK" }.size()

        def totalValue = items.sum { it.totalValue }
        def data = [
                totalValue:totalValue,
                items:items,
                elapsedTime:elapsedTime,
                allStockCount:items.size(),
                inStockCount:inStockCount,
                reorderStockCount:reorderStockCount,
                lowStockCount:lowStockCount,
                outOfStockCount:outOfStockCount,
                overStockCount:overStockCount
        ]

        log.info "Elapsed time " + elapsedTime + " s"
        //render "${params.callback}(${result as JSON})"
        render text: "${params.callback}(${data as JSON})", contentType: "application/javascript", encoding: "UTF-8"


    }

    /**
     * Analytics > Inventory Browser > Data Table
     */
    def getQuantityOnHandByProductGroup = {
        def startTime = System.currentTimeMillis()
        log.info "getQuantityOnHandByProductGroup " + params
        def aaData = new HashSet() //data.productGroupDetails.ALL.values()
        if (params["status[]"]) {
            def data = reportService.calculateQuantityOnHandByProductGroup(params.location.id)
            params["status[]"].split(",").each {
                log.info "Add entries from data.productGroupDetails[${it}]"
                def entry = data.productGroupDetails[it]
                if (entry) {
                    aaData += entry.values()
                }
            }
        }

        //aaData.unique()

        def totalValue = 0
        totalValue = aaData.sum { it.totalValue?:0 }
        NumberFormat numberFormat = NumberFormat.getNumberInstance()
        String currencyCode = grailsApplication.config.openboxes.locale.defaultCurrencyCode?:"USD"
        numberFormat.currency = Currency.getInstance(currencyCode)
        numberFormat.maximumFractionDigits = 2
        numberFormat.minimumFractionDigits = 2
        def totalValueFormatted = numberFormat.format(totalValue?:0)
        //def totalValue = aaData.collect { it.totalValue }.sum()
        log.info "totalValue = " + totalValueFormatted

        render (["aaData":aaData,
                "processingTime":"Took " + (System.currentTimeMillis()-startTime) + " ms to process",
                totalValue:totalValue,totalValueFormatted:totalValueFormatted] as JSON)
    }

    def getSummaryByProductGroup = {
        log.info "getSummaryByProductGroup " + params
        def data = reportService.calculateQuantityOnHandByProductGroup(params.location.id)


        render (data.productGroupSummary as JSON)
    }

    def mostRecentQuantityOnHand = {
        def product = Product.get(params.id)
        def location = Location.get(session?.warehouse?.id)
        def object = inventoryService.getMostRecentQuantityOnHand(product, location)
        render ([mostRecentQuantityOnHand:object] as JSON)
    }


    def mostRecentQuantityOnHandByLocation = {
        def location = Location.get(session?.warehouse?.id)
        def results = inventoryService.getMostRecentQuantityOnHand(location)
        render ([results:results] as JSON)
    }

    def quantityMap = {
        def location = Location.get(session?.warehouse?.id)
        def quantityMap = inventoryService.getQuantityMap(location)
        render ([quantityMap:quantityMap] as JSON)
    }


    def distinctProducts = {
        def location = Location.get(session?.warehouse?.id)
        def products = inventoryService.getDistinctProducts(location)
        render ([products:null] as JSON)
    }

    def scanBarcode = {
        log.info "Scan barcode: " + params

        def url
        def type
        def barcode = params.barcode

        def product = Product.findByProductCode(barcode);
        if (product) {
            url = g.createLink(controller: "inventoryItem", action: "showStockCard", id: product.id, absolute: true)
            type = "stock card"
        }
        else {
            def requisition = Requisition.findByRequestNumber(barcode);
            if (requisition) {
                url = g.createLink(controller: "requisition", action: "show", id: requisition.id, absolute: true)
                type = "requisition"
            }
            else {
                def shipment = Shipment.findByShipmentNumber(barcode);
                if (shipment) {
                    url = g.createLink(controller: "shipment", action: "showDetails", id: shipment.id, absolute: true)
                    type = "shipment"
                }
                else {
                    def purchaseOrder = Order.findByOrderNumber(barcode);
                    if (purchaseOrder) {
                        url = g.createLink(controller: "purchaseOrder", action: "show", id: purchaseOrder.id, absolute: true)
                        type = "purchase order"
                    }
                }
            }
        }
        render ([url:url,type:type,barcode:barcode] as JSON)
    }

    /**
     * Stock Card > Snapshot graph
     */
    def getQuantityOnHandByMonth = {
        log.info params;
        def dates = []
        def format = "MMM-yy"
        def numMonths = (params.numMonths as int)?:12
        def location = Location.get(params.location.id)
        def product = Product.get(params.product.id)

        def today = new Date()
        today.clearTime()


        if (numMonths >= 24) {
            use(groovy.time.TimeCategory) {
                numMonths.times { i ->
                    dates << (today - i.months)
                }
            }
            format = "yyyy"
        }
        else if (numMonths > 12) {
            use(groovy.time.TimeCategory) {
                numMonths.times { i ->
                    dates << (today - i.months)
                }
            }
            format = "MMM-yy"
        }
        else if (numMonths >= 6) {
            use(groovy.time.TimeCategory) {
                numMonths.times { i ->
                    dates << (today - i.months)
                }
            }
            format = "MMM-yy"
        }
        else if (numMonths >= 2) {
            use(groovy.time.TimeCategory) {
                (numMonths*4).times { i ->
                    dates << (today - i.weeks)
                }
            }
            format = "'wk' w"
        }
        else {
            use(groovy.time.TimeCategory) {
                (numMonths*21).times { i ->
                    dates << (today - i.days)
                }
            }
            format = "dd-MMM"
        }
        log.info "dates: " + dates
        dates = dates.sort()
        log.info "dates sorted: " + dates

        // initialize data
        def data = dates.inject([:].withDefault { [label: null, date: null, days: 0, totalQuantity: 0, maxQuantity: 0, month: 0, year: 0, day: 0] }) { map, date ->
            def dateKey = date.format(format)
            map[dateKey].label = dateKey
            map[dateKey].day = date.day
            map[dateKey].month = date.month
            map[dateKey].year = date.year
            map[dateKey].totalQuantity = 0
            map[dateKey].maxQuantity = 0
            map[dateKey].days = 0
            map
        }

        log.info "Data initialized " + data


        // Get all inventory snapshots for the current product and location
        //def inventorySnapshots = InventorySnapshot.findAllByProductAndLocation(product, location)

        log.info "Inventory snapshots between " + dates[0] + " " + dates[dates.size()-1]
        def inventorySnapshots = InventorySnapshot.createCriteria().list() {
            eq("product", product)
            eq("location", location)
            between("date", dates[0], dates[dates.size()-1])
            order("date", "asc")
        }

        def newData = []
        log.info "dates: " + dates
        log.info "inventorySnapshots: " + inventorySnapshots*.date
        if (inventorySnapshots) {
            def items = inventorySnapshots.collect { [date:it.date, quantityOnHand:it.quantityOnHand] }
            items.sort { it.date }.each { item ->
                def dateKey = item.date.format(format)
                data[dateKey].date = item.date
                data[dateKey].label = dateKey
                data[dateKey].month = item.date.month
                data[dateKey].day = item.date.day
                data[dateKey].year = item.date.year
                data[dateKey].totalQuantity += item.quantityOnHand
                if (item.quantityOnHand > data[dateKey].maxQuantity) {
                    data[dateKey].maxQuantity = item.quantityOnHand
                }
                data[dateKey].days++
                data
            }


            data.each { key, value ->
                log.info "KEY: " + key
                log.info "VALUE: " + value
                if (value.days) {
                    //newData << [key, (value.totalQuantity/value.days) ]
                    newData << [key, (value.maxQuantity) ]
                }
                else {
                    newData << [key, 0]
                }
            }
        }
        log.info "newData: " + newData


        render ([label: "${product?.name}", location: "${location.name}", data:newData] as JSON);
    }



    /**
     * Dashboard > Fast movers
     */
    @Cacheable("fastMoversCache")
    def getFastMovers = {
        def dateFormat = new SimpleDateFormat("MM/dd/yyyy")
        def date = new Date()
        if (params.date) {
            date = dateFormat.parse(params.date)
            date.clearTime()
        }
        def location = Location.get(params?.location?.id?:session?.warehouse?.id)
        def data = dashboardService.getFastMovers(location, date, params.max as int)

        render ([aaData: data?.results?:[]] as JSON)
    }


    def getOrderItem = {
        def orderItem = OrderItem.get(params.id)
        render ([id:orderItem.id, product:orderItem.product, order:orderItem.order, quantity:orderItem.quantity, unitPrice:orderItem.unitPrice] as JSON)
    }


    def enableCalculateHistoricalQuantityJob = {
        CalculateHistoricalQuantityJob.enabled = true
        render([message: "CalculateHistoricalQuantityJob has been ${CalculateHistoricalQuantityJob.enabled?'enabled':'disabled'}"] as JSON)
    }

    def disableCalculateHistoricalQuantityJob = {
        CalculateHistoricalQuantityJob.enabled = false
        render([message: "CalculateHistoricalQuantityJob has been ${CalculateHistoricalQuantityJob.enabled?'enabled':'disabled'}"] as JSON)
    }

    def statusCalculateHistoricalQuantityJob = {
        render "${CalculateHistoricalQuantityJob.enabled?'enabled':'disabled'}"
    }

    def pendingShipments = {
        def location = Location.get(session?.warehouse?.id)
        def shipments = shipmentService.getPendingShipments(location)
        render ([count: shipments.size(), shipments:shipments] as JSON)
    }

    @Cacheable("binLocationSummaryCache")
    def getBinLocationSummary = {
        String locationId = params?.location?.id ?: session?.warehouse?.id
        Location location = Location.get(locationId)
        def binLocationReport = inventoryService.getBinLocationReport(location)

        render(binLocationReport["summary"] as JSON)
    }

    //@Cacheable("binLocationReportCache")
    def getBinLocationReport = {
        log.info "binLocationReport: " + params
        String locationId = params?.location?.id ?: session?.warehouse?.id
        Location location = Location.get(locationId)
        def binLocationReport = inventoryService.getBinLocationReport(location)

        def data = binLocationReport["data"]
        if (params.status) {
            data = data.findAll { it.status == params.status }
        }

        def hasRoleFinance = userService.hasRoleFinance(session?.user)

        // Flatten the data to make it easier to display
        data = data.collect {
            def quantity = it?.quantity?:0
            def unitCost = hasRoleFinance ? (it?.product?.pricePerUnit?:0.0) : null
            def totalValue = hasRoleFinance ? g.formatNumber(number: quantity * unitCost) : null
            [
                    id: it.product?.id,
                    status: g.message(code: "binLocationSummary.${it.status}.label"),
                    productCode: it.product?.productCode,
                    productName: it?.product?.name,
                    productGroup: it?.product?.genericProduct?.name,
                    category: it?.product?.category?.name,
                    lotNumber: it?.inventoryItem?.lotNumber,
                    expirationDate: g.formatDate(date: it?.inventoryItem?.expirationDate, format: "dd/MMM/yyyy"),
                    unitOfMeasure: it?.product?.unitOfMeasure,
                    binLocation: it?.binLocation?.name,
                    quantity: quantity,
                    unitCost: unitCost,
                    totalValue: totalValue
            ]
        }



        render(["aaData":data] as JSON)

    }


    def getShipmentsWithInvalidStatus = {
        def shipments = shipmentService.shipmentsWithInvalidStatus
        render ([count: shipments.size(), shipments:shipments] as JSON)
    }

    def fixShipmentsWithInvalidStatus = {
        def count = shipmentService.fixShipmentsWithInvalidStatus()
        render ([count: count] as JSON)
    }

    // TODO The following method should be removed before merge
    def getTransactionEntriesByLocation = {
        Location location = Location.get(session.warehouse.id)
        def startTime = System.currentTimeMillis()
        def results = inventoryService.getTransactionEntriesByLocation(location)
        render ([status: "OK", count: results.size(), responseTime: "${System.currentTimeMillis()-startTime} ms",
        results:results] as JSON)
        //render ([result: result] as JSON)

    }

    // TODO The following method should be removed before merge
    def getLocation = {
        List locations = Location.list([max:10])
        render ([status: "OK", locations: locations] as JSON)

    }

}
