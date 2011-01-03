package org.pih.warehouse.receiving;

import org.pih.warehouse.product.Product;

class ReceiptController {
    def scaffold = Receipt
	
	
	
	def process = { 
		def receiptInstance = Receipt.get(params.id);
				
	}
	
	

}
