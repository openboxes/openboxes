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

import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product

class ItemCommand implements Serializable {

    Product product
    String lotNumber
    InventoryItem inventoryItem
    Shipment shipment
    Container container
    ShipmentItem shipmentItem
    Integer quantity
    Integer quantityOnHand
    Integer quantityShipping
    Integer quantityReceiving


    static constraints = {
        product(nullable: true)
        lotNumber(nullable: true)
        inventoryItem(nullable: true)
        shipment(nullable: true)
        container(nullable: true)
        shipmentItem(nullable: true)
        quantity(nullable: true)
        quantityOnHand(nullable: true)
        quantityReceiving(nullable: true)
        quantityShipping(nullable: true)

    }


}
