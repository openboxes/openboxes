package org.pih.warehouse.api

import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.core.Location
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class AvailableItemStatusSpec extends Specification {

    void "should return RECALLED when inventory item is recalled"() {
        given:
        AvailableItem item = new AvailableItem(
                inventoryItem: Stub(InventoryItem) {
                    isRecalled() >> true
                },
                quantityOnHand: 10,
                quantityAvailable: 10,
        )

        expect:
        item.status == AvailableItemStatus.RECALLED
    }

    void "should return HOLD when bin location is on hold"() {
        given:
        AvailableItem item = new AvailableItem(
                binLocation: Stub(Location) {
                    isOnHold() >> true
                },
                quantityOnHand: 10,
                quantityAvailable: 10,
        )

        expect:
        item.status == AvailableItemStatus.HOLD
    }

    void "AvailableItem.getStatus() should return #expectedStatus for case: #scenario"() {
        given:
        AvailableItem item = new AvailableItem(
                quantityOnHand: quantityOnHand,
                quantityAvailable: quantityAvailable,
        )

        expect:
        item.status == expectedStatus

        where:
        quantityOnHand | quantityAvailable || expectedStatus                    | scenario
        0              | 5                 || AvailableItemStatus.NOT_AVAILABLE | "quantityOnHand is zero"
        -1             | 5                 || AvailableItemStatus.NOT_AVAILABLE | "quantityOnHand is negative"
        10             | 0                 || AvailableItemStatus.NOT_AVAILABLE | "quantityAvailable is zero"
        10             | -1                || AvailableItemStatus.NOT_AVAILABLE | "quantityAvailable is negative"
        0              | 0                 || AvailableItemStatus.NOT_AVAILABLE | "both quantityOnHand and quantityAvailable are zero"
        -5             | -10               || AvailableItemStatus.NOT_AVAILABLE | "both negative"
        10             | 5                 || AvailableItemStatus.PICKED        | "quantityAvailable less than quantityOnHand"
        10             | 10                || AvailableItemStatus.AVAILABLE     | "quantityAvailable equals quantityOnHand"
        5              | 10                || AvailableItemStatus.AVAILABLE     | "quantityAvailable greater than quantityOnHand"
    }

    void "should prioritize RECALLED over all other statuses"() {
        given:
        AvailableItem item = new AvailableItem(
                inventoryItem: Stub(InventoryItem) {
                    isRecalled() >> true
                },
                binLocation: Stub(Location) {
                    isOnHold() >> true
                },
                quantityOnHand: 0,
                quantityAvailable: 0,
        )

        expect:
        item.status == AvailableItemStatus.RECALLED
    }

    void "should prioritize HOLD over NOT_AVAILABLE"() {
        given:
        AvailableItem item = new AvailableItem(
                binLocation: Stub(Location) {
                    isOnHold() >> true
                },
                quantityOnHand: 0,
                quantityAvailable: 0,
        )

        expect:
        item.status == AvailableItemStatus.HOLD
    }
}
