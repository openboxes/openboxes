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

import org.pih.warehouse.inventory.StockMovementStatusCode

enum ShipmentStatusCode {

    CREATED(0),
    PENDING(10),
    ACCEPTED(20),
    SHIPPED(30),
    PARTIALLY_RECEIVED(40),
    RECEIVED(50)

    int sortOrder

    ShipmentStatusCode(int sortOrder) { [this.sortOrder = sortOrder] }

    ShipmentStatusCode getDisplayStatus() {
        return this
    }

    static int compare(ShipmentStatusCode a, ShipmentStatusCode b) {
        return a.sortOrder <=> b.sortOrder
    }

    static list() {
        [CREATED, PENDING, SHIPPED, PARTIALLY_RECEIVED, RECEIVED]
    }

    static listPending() {
        [CREATED, PENDING, SHIPPED, PARTIALLY_RECEIVED]
    }


    String getName() { return name() }

    String toString() { return name() }

static toStockMovementStatus(ShipmentStatusCode shipmentStatusCode) {
        switch(shipmentStatusCode) {
            case ShipmentStatusCode.PENDING:
                return StockMovementStatusCode.PENDING
            case ShipmentStatusCode.CREATED:
                return StockMovementStatusCode.CREATED
            case ShipmentStatusCode.SHIPPED:
                return StockMovementStatusCode.DISPATCHED
            case ShipmentStatusCode.PARTIALLY_RECEIVED:
                return StockMovementStatusCode.DISPATCHED
            case ShipmentStatusCode.RECEIVED:
                return StockMovementStatusCode.DISPATCHED
            case null:
                return StockMovementStatusCode.PENDING
            default:
                return StockMovementStatusCode.valueOf(shipmentStatusCode.toString())
        }
    }

    static fromStockMovementStatus(StockMovementStatusCode stockMovementStatus) {
        switch(stockMovementStatus) {
            case StockMovementStatusCode.PENDING:
                return ShipmentStatusCode.PENDING

            case StockMovementStatusCode.CREATED:
                return ShipmentStatusCode.CREATED

            case StockMovementStatusCode.DISPATCHED:
                return ShipmentStatusCode.SHIPPED

            default:
                return ShipmentStatusCode.valueOf(stockMovementStatus.toString())
        }
    }
}
