package org.pih.warehouse.inventory

import grails.test.GrailsUnitTestCase
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.RequisitionItem

class InventoryItemTests extends GrailsUnitTestCase {

    void testToJsonData(){
        def product = new Product(id: "prod1", name:"aspin")
        def item = new InventoryItem(
                id: "1234",
                product: product,
                lotNumber: "ABCD"
        )
        Map json = item.toJson()
        assert json.id == item.id
        assert json.productId == item.product.id
        assert json.productName == item.product.name
        assert json.lotNumber == item.lotNumber
    }
}
