package org.pih.warehouse

import grails.converters.*;
import org.pih.warehouse.shipping.*;


class JsonController {

	def findShipperServiceByName = {
		log.info params
		def items = new TreeSet();
		if (params.term) {
			items = ShipperService.withCriteria {
				or {
					ilike("name", "%" +  params.term + "%")					
					ilike("description", "%" +  params.term + "%")
					shipper { 
						ilike("name", "%" +  params.term + "%")
					}
				}
			}
			if (items) {
				items = items.collect() {
					[	value: it.id,
						label: it.shipper.name + " " + it.name,
						valueText: it.shipper.name + " " + it.name,
						desc: it.description,
						icon: "none"]
				}
			}
		}
		render items as JSON;
	}
	
	def findShipperByName = {
		log.info params
		def items = new TreeSet();
		if (params.term) {
			items = Shipper.withCriteria {
				or {
					ilike("name", "%" +  params.term + "%")
					ilike("description", "%" +  params.term + "%")
				}
			}
			if (items) {
				items = items.collect() {
					[	value: it.id,
						label: it.name,
						valueText: it.name,
						desc: it.description,
						icon: "none"]
				}
			}
		}
		render items as JSON;
	}
	
	
}
