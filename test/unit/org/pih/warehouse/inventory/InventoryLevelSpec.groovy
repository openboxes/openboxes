package org.pih.warehouse.inventory

import grails.plugin.spock.UnitSpec

class InventoryLevelSpec extends UnitSpec {

    def "validate should return false"() {
        when:
        mockDomain(InventoryLevel)
        InventoryLevel inventoryLevel = new InventoryLevel()

        then:
        inventoryLevel.validate() == false
    }
}
