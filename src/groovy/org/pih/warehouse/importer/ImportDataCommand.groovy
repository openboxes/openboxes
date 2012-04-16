package org.pih.warehouse.importer

import org.pih.warehouse.core.Location;

class ImportDataCommand {
	def filename
	def importFile
	def type
	Location location
	def columnMap
	def data
	//def errors
	
	def products = []
	def categories = []
	def inventoryItems = []
	def transaction 
	
	static constraints = {
		filename(nullable:true)
		importFile(nullable:true)
		type(nullable:false)
		location(nullable:false)
		columnMap(nullable:true)
		data(nullable:true)
		products(nullable:true)
		categories(nullable:true)
		inventoryItems(nullable:true)
		transaction(nullable:true)
	}
	
}