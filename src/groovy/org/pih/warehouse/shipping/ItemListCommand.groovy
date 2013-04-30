/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.shipping

import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList

// import java.io.Serializable

class ItemListCommand implements Serializable {
	
	def items =
		LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(ItemCommand.class));

	static constraints = {
		items(validator: { val, obj, errors ->
			def errorsFound = false;
			/*
			val.each{ item ->
				// Ignore a null order item
				if (item) {
					// If the quantity is not null and the item does not validate, reject the
					if(item?.quantity) {	// && !item?.validate()
						item.errors.allErrors.each { error ->
							println(">>>>>>>>>>>>> ERROR " + error.getCode() + " ")
							obj.errors.rejectValue("orderItems", error.getField() + "." + error.getCode())
						}
					}
				}
			}
			*/
			return errorsFound;
		});
	}
}


