package org.pih.warehouse.request

import java.util.Date;
import org.pih.warehouse.core.Comment;
import org.pih.warehouse.core.User;
import org.pih.warehouse.inventory.InventoryItem;
import org.pih.warehouse.product.Category;
import org.pih.warehouse.product.Product;
import org.pih.warehouse.shipping.ShipmentItem;

class RequestShipment implements Serializable {

	static belongsTo = [shipmentItem: ShipmentItem, requestItem: RequestItem]
	
}
