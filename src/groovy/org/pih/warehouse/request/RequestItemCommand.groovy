/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.request

import java.io.Serializable;
import java.util.List;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentItem;
import org.codehaus.groovy.grails.validation.Validateable

@Validateable
class RequestItemCommand  implements Serializable {

	Boolean primary
	RequestItem requestItem
	Shipment shipment
	ShipmentItem shipmentItem
	
	// from request item
	String type
	String description
	Integer quantityRequested
	
	// for shipment item
	String lotNumber
	Date expirationDate
	Product productReceived
	InventoryItem inventoryItem
	Integer quantityReceived			
	
}

