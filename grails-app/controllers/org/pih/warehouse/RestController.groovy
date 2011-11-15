package org.pih.warehouse

import grails.converters.*

class RestController {
	def warehouses = {
		render Location.list() as XML
	}
	
	def warehouse = {
		render Location.get(params.id) as XML
	}
	
	
	
	/* not working as expected -- causes segmentation fault */ 
	 /* 
	def warehouses = {
		def list = Location.list()
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
