/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.barcode
// import org.krysalis.barcode4j.impl.code128.Code128Bean

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
	

