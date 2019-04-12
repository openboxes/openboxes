/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.inventory

//import java.util.Date;

import org.pih.warehouse.product.Product
import util.InventoryUtil;

class InventoryLevel {
	
	String id

	Product product;

	InventoryStatus status = InventoryStatus.SUPPORTED;

	Boolean preferred = Boolean.FALSE

	// Should warn user when quantity is below this value
	Integer minQuantity;

	// Should reorder product when quantity falls below this value
	Integer reorderQuantity;

	// Should alert user when quantity is below this value (emergency)
	//Integer lowQuantity;

	// Should warn user when the quantity is below this value
	//Integer idealQuantity;

	// Should warn user when quantity is above this value
	Integer maxQuantity;

    // Bin location where item is stored
    String binLocation

	// ABC analysis class
	String abcClass
	
	// Auditing
	Date dateCreated;
	Date lastUpdated;
	
	static mapping = {
		id generator: 'uuid'
		product index: 'inventory_level_prod_inv_idx'
		inventory index: 'inventory_level_prod_inv_idx'
		cache true
	}

	static belongsTo = [ inventory: Inventory ]
	
	static constraints = { 
		status(nullable:true)
		product(nullable:false)
		//supported(nullable:false)
		minQuantity(nullable:true, range: 0..2147483646)
		reorderQuantity(nullable:true, range: 0..2147483646)
		//lowQuantity(nullable:true)
		//idealQuantity(nullable:true)
		maxQuantity(nullable:true, range: 0..2147483646)
		binLocation(nullable:true)
        abcClass(nullable: true)
        preferred(nullable: true)
	}

    def statusMessage(Integer currentQuantity) {
        return InventoryUtil.getStatusMessage(status, minQuantity, reorderQuantity, maxQuantity, currentQuantity)
    }

    String toString() { return "${product?.productCode}:${preferred}:${minQuantity}:${reorderQuantity}:${maxQuantity}:${lastUpdated}"}


}
