package org.pih.warehouse.fulfillment

import java.io.Serializable;
import java.util.List;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.pih.warehouse.core.Person;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.request.RequestItem;
import org.pih.warehouse.shipping.Shipment;
import org.pih.warehouse.shipping.ShipmentItem;
import org.codehaus.groovy.grails.validation.Validateable

@Validateable
class FulfillmentItemCommand  implements Serializable {

	Shipment shipment
	RequestItem requestItem
	ShipmentItem shipmentItem	
	InventoryItem inventoryItem
	Integer quantity			
	
}

