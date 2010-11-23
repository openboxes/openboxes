package org.pih.warehouse.barcode

import org.krysalis.barcode4j.impl.code39.Code39Bean
import org.krysalis.barcode4j.impl.code128.Code128Bean

class BarcodeController {
	def barcode = {
		// Create and configure the generator
		//def generator = new Code39Bean()
		//generator.height = 12

		//def generator =
		
		def barcodeValue = "12345"
		renderBarcodePng(generator, barcodeValue, [antiAlias: false])
	}
}
	

