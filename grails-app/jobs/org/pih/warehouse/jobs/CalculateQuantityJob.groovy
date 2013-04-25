package org.pih.warehouse.jobs

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventorySnapshot
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.ShipmentItem

class CalculateQuantityJob {
	
	def inventoryService
	
	static triggers = { 
		//simple repeatInterval: 30000l // execute job once in 5 seconds, every ten minutes
		//simple name:'simpleTrigger', startDelay:10000, repeatInterval: 30000, repeatCount: 10
		//cron name:'cronTrigger', startDelay:10000, cronExpression: '0/6 * 15 * * ?'
		//custom name:'customTrigger', triggerClass:MyTriggerClass, myParam:myValue, myAnotherParam:myAnotherValue
	}

	def calculatePendingQuantity(product, location) { 
		def inboundQuantity = 0;
		def outboundQuantity = 0;
		try { 
			def shipmentItems = ShipmentItem.withCriteria { 
				shipment { 
					eq("destination", location)
				}
				or { 
					inventoryItem { 
						eq("product", product)
					}
					eq("product", product)
				}
			}
			inboundQuantity = shipmentItems.sum { it.quantity }
			shipmentItems = ShipmentItem.withCriteria { 
				shipment { 
					eq("origin", location)
				}
				or { 
					inventoryItem { 
						eq("product", product)
					}
					eq("product", product)
				}
			}
			outboundQuantity = shipmentItems.sum { it.quantity } 
			 
		} catch (Exception e) { 
			println ("Error " + e.message)
		}
		
		[inboundQuantity, outboundQuantity]
	}
	
	
	def execute() {
		println "Starting inventory snapshot process" + new Date()
		
		try { 
			def locations = Location.list()
			def products = Product.list() 			
		
			def location = Location.findByNameLike("%Boston%")
			//locations.each { location ->
			 
				if (location?.inventory) { 
					def productQuantityMap = inventoryService.getQuantityByProductMap(location.inventory)
					products.each { product ->
						log.info "Updating inventory snapshot for product " + product.name + " @ " + location.name
						try { 							
							def inventorySnapshot = InventorySnapshot.findByLocationAndProduct(location, product)					
							if (!inventorySnapshot) { 
								inventorySnapshot = new InventorySnapshot(location: location, product: product)
								inventorySnapshot.save(flush:true)
							}
							def pendingQuantity = calculatePendingQuantity(product, location)					
							inventorySnapshot.quantityOnHand = productQuantityMap[inventorySnapshot.product]?:0
							inventorySnapshot.quantityInbound = pendingQuantity[0]
							inventorySnapshot.quantityOutbound = pendingQuantity[1]
							inventorySnapshot.lastUpdated = new Date()
							inventorySnapshot.save(flush:true)
						} catch (Exception e) { 
							log.warn("Error saving inventory snapshot for product " + product.name + " and location " + location.name + " -> " + e.message)
						}
					}			
				}
				else { 
					log.warn("Could not process location ")
				}
			//}
			log.info "Saved inventory snapshot for ${products.size()} products over ${locations.size()} locations"
		} catch (Exception e) { 
			log.error("Unable to complete inventory snapshot process", e)
		}
		
		println "Finished inventory snapshot process"
	}
}
