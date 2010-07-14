package org.pih.warehouse.inventory



class StockCardController {
	
	static allowedMethods = [save: "POST", update: "POST", delete: "POST", saveEntry: "POST"]
	
	def index = {
		redirect(action: "list", params: params)
	}
	
	def manage = {
		println "manage stock card $params.id"
		//def product = Product.get(params.id)
		//if (!product)
		//    redirect(controller: "product", action: "list");
		//def stockCardInstance = StockCard.findByProduct(product);
		//def stockCardInstance = StockCard.findWhere(product:product);
		
		def stockCardInstance = StockCard.get(params.id);
		
		println "stock card instance $stockCardInstance"
		/*
		 if (!stockCardInstance) {
		 println "creating new stock card ";	    
		 stockCardInstance = new StockCard(product:product);
		 stockCardInstance.save();
		 println "creating new stock card $stockCardInstance.id";
		 }*/
		
		//def stockCardInstance = StockCard.get(params.id)
		if (!stockCardInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'stockCard.label', default: 'StockCard'), params.id])}"
			redirect(action: "list")
		}
		else {
			[stockCardInstance: stockCardInstance]
		}
	}
	
	/*
	 def save = {
	 def trip = new Trip(params)
	 if(!trip.hasErrors() && trip.save()) {
	 flash.message = "Trip ${trip.id} created"
	 redirect(action:show,id:trip.id)
	 }
	 else {
	 render(view:'create',model:[trip:trip])
	 }
	 }
	 */
	
	def saveEntry = {
		
		println "\n\nsaving stock card entry $params";
		def stockCardEntry = new StockCardItem(params)
		println "saving stock card entry ${stockCardEntry.id} for stock card ${stockCardEntry.stockCard.id}"
		if (stockCardEntry.save(flush: true)) {
			println "saved stock card entry ${stockCardEntry.id}"
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'stockCardEntry.label', default: 'Stock Card Entry'), stockCardEntry.id])}"
			redirect(action: "manage", id: stockCardEntry.stockCard.id)
		}
		else {
			println "render create form"
			
			
			println "$stockCardEntry.errors"
			//render(controller: "stockCardEntry", view: "create", model: [stockCardEntry: stockCardEntry])
			
			render(view: "manage", model: [stockCardEntryInstance: stockCardEntry, stockCardInstance: stockCardEntry.stockCard])
		}
		
		
		/*
		 def stockCardEntry = new StockCardEntry(params);
		 def saved = stockCardEntry.save(flush: true);
		 println "saved stock card entry $stockCardEntry ";
		 if (saved) {
		 println "saved stock card entry $stockCardEntry";
		 flash.message = '''${message(code: 'default.created.message',
		 args: [message(code: 'stockCardEntry.label', default: 'Stock Card Entry'), stockCardEntry.id])}'''
		 //redirect(action: "show", id: stockCardEntry.id)
		 redirect(action: "manage", id: stockCardEntry.stockCard.id);
		 }
		 else {
		 println "need to create new stock card entry $params.stockCard.id";
		 redirect(action: "manage", id: params.stockCard.id)
		 //render(view: "create", model: [stockCardEntry: stockCardEntry])
		 }
		 */
	}
	
	
	/**
	 * Returns a list of stock cards (one per product)
	 */
	def list = {
		//def stockCards = new ArrayList<StockCard>();
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		//[stockCardInstanceList: StockCard.list(params), stockCardInstanceTotal: StockCard.count()]
		//def products = Product.list(params);
		//def productCount = Product.count();
		/*
		 for (product in products) {
		 def stockCard = new StockCard(product: product);
		 }*/
		
		[stockCardInstanceList: StockCard.list(params), stockCardInstanceTotal: StockCard.count()];
		//[stockCardInstanceList: stockCards, stockCardInstanceTotal: productCount];
		
	}
	
	def create = {
		def stockCardInstance = new StockCard()
		stockCardInstance.properties = params
		return [stockCardInstance: stockCardInstance]
	}
	
	def save = {
		def stockCardInstance = new StockCard(params)
		if (stockCardInstance.save(flush: true)) {
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'stockCard.label', default: 'StockCard'), stockCardInstance.id])}"
			redirect(action: "show", id: stockCardInstance.id)
		}
		else {
			render(view: "create", model: [stockCardInstance: stockCardInstance])
		}
	}
	
	def show = {
		
		def product = Product.get(params.id)
		def stockCardInstance = new StockCard(product: product);
		//def stockCardInstance = StockCard.get(params.id)
		if (!stockCardInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'stockCard.label', default: 'StockCard'), params.id])}"
			redirect(action: "list")
		}
		else {
			[stockCardInstance: stockCardInstance]
		}
	}
	
	def edit = {
		def stockCardInstance = StockCard.get(params.id)
		if (!stockCardInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'stockCard.label', default: 'StockCard'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [stockCardInstance: stockCardInstance]
		}
	}
	
	def update = {
		def stockCardInstance = StockCard.get(params.id)
		if (stockCardInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (stockCardInstance.version > version) {
					
					stockCardInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'stockCard.label', default: 'StockCard')] as Object[], "Another user has updated this StockCard while you were editing")
					render(view: "edit", model: [stockCardInstance: stockCardInstance])
					return
				}
			}
			stockCardInstance.properties = params
			if (!stockCardInstance.hasErrors() && stockCardInstance.save(flush: true)) {
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'stockCard.label', default: 'StockCard'), stockCardInstance.id])}"
				redirect(action: "show", id: stockCardInstance.id)
			}
			else {
				render(view: "edit", model: [stockCardInstance: stockCardInstance])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'stockCard.label', default: 'StockCard'), params.id])}"
			redirect(action: "list")
		}
	}
	
	def delete = {
		def stockCardInstance = StockCard.get(params.id)
		if (stockCardInstance) {
			try {
				stockCardInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'stockCard.label', default: 'StockCard'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'stockCard.label', default: 'StockCard'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'stockCard.label', default: 'StockCard'), params.id])}"
			redirect(action: "list")
		}
	}
}
