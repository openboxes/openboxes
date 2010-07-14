package org.pih.warehouse

import grails.converters.*

class RestController {
	def warehouses = {
		render Warehouse.list() as XML
	}
	
	def warehouse = {
		render Warehouse.get(params.id) as XML
	}
	
	
	
	/* not working as expected -- causes segmentation fault */ 
	 /* 
	def warehouses = {
		def list = Warehouse.list()
		render(contentType:"text/xml"){
			warehouses {
				for(w in list){
					warehouse(id:w.id, name: w.name){
						address(w.address)
					}
				}
			}
		}
	}*/
}
