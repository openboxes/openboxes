package org.pih.warehouse

import geb.spock.GebReportingSpec
import testutils.DbHelper


class ShipmentSpec extends GebReportingSpec{
    def "should send a sea shipment from Boston to Miami"(){
        given:
            DbHelper.CreateProductInInventory(product_name, 5000)

    }

   def product_name = "TestProd" + UUID.randomUUID().toString()[0..5]
}
