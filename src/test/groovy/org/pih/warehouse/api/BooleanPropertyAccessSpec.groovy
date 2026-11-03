package org.pih.warehouse.api

import grails.testing.gorm.DataTest
import spock.lang.Specification
import spock.lang.Unroll

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.requisition.Requisition

/**
 * Groovy 4 only resolves `obj.x` to `isX()` when isX() returns a primitive `boolean`.
 * These tests guard that callers can keep using property syntax on the computed
 * isX() getters, and that StockMovement.validate() (which calls every getter on a
 * Validateable) does not blow up on them.
 */
class BooleanPropertyAccessSpec extends Specification implements DataTest {

    void setupSpec() {
        mockDomains(Location, InventoryItem, Requisition)
    }

    @Unroll
    void "StockMovement.#property resolves via property access and defaults to false"() {
        given:
        StockMovement stockMovement = new StockMovement()

        expect:
        stockMovement."${property}" == false

        where:
        property << ["pending", "electronicType", "pendingApproval", "inApprovalState", "approvalRequired"]
    }

    @Unroll
    void "AvailableItem.#property resolves via property access"() {
        given:
        AvailableItem availableItem = new AvailableItem()

        expect:
        availableItem."${property}" == expected

        where:
        property    | expected
        "onHold"    | false
        "recalled"  | false
        "pickable"  | true
        "available" | false
    }

    void "StockMovement.approvalRequired does not throw when the requisition has no status"() {
        given:
        Location origin = Stub(Location) {
            isApprovalRequired() >> true
        }
        Requisition requisition = new Requisition(approvalRequired: true)
        requisition.status = null
        StockMovement stockMovement = new StockMovement(origin: origin, requisition: requisition)

        when:
        boolean approvalRequired = stockMovement.approvalRequired

        then:
        noExceptionThrown()
        !approvalRequired
    }

    void "StockMovement.validate() evaluates the boolean getters without throwing"() {
        given:
        // Only validate the boolean getters: validate() also calls every other getter (e.g. getDisplayStatus(),
        // which needs the ApplicationTagLib bean that isn't available in a unit test).
        List<String> booleanProperties = ["pending", "electronicType", "pendingApproval", "inApprovalState", "approvalRequired"]
        Location origin = Stub(Location) {
            isApprovalRequired() >> true
        }
        Requisition requisition = new Requisition(approvalRequired: true)
        requisition.status = null
        StockMovement stockMovement = new StockMovement(origin: origin, requisition: requisition)

        when:
        stockMovement.validate(booleanProperties)

        then:
        noExceptionThrown()
        !stockMovement.errors.hasFieldErrors("approvalRequired")

        and: "the boolean getters are picked up as constrained properties, so validate() really calls them"
        StockMovement.constraintsMap.keySet().containsAll(booleanProperties)
    }
}
