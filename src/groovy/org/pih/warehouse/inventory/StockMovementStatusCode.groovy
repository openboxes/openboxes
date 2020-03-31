/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.inventory

enum StockMovementStatusCode {

    // Display status code
    PENDING,

    // Actual status code
    CREATED(0),
    REQUESTING(1, PENDING),
    REQUESTED(2, PENDING),
    VALIDATING(2, PENDING),
    VALIDATED(3, PENDING),
    PICKING(3, PENDING),
    PICKED(4, PENDING),
    CHECKING(4, PENDING),
    CHECKED(6, PENDING),
    PACKED(7, PENDING),
    REVIEWING(8, PENDING),
    DISPATCHED(9),
    CANCELED(10)

    int sortOrder
    StockMovementStatusCode displayStatusCode

    StockMovementStatusCode() { }

    StockMovementStatusCode(int sortOrder) { this.sortOrder = sortOrder }

    StockMovementStatusCode(int sortOrder, StockMovementStatusCode displayStatusCode) {
        this.sortOrder = sortOrder
        this.displayStatusCode = displayStatusCode
    }

    StockMovementStatusCode getDisplayStatus() {
        return this.displayStatusCode?:this
    }

    static int compare(StockMovementStatusCode a, StockMovementStatusCode b) {
        return a.sortOrder <=> b.sortOrder
    }

    static listInbound() {
        [CREATED, REQUESTED, CHECKING, DISPATCHED, CANCELED]
    }

    static listOutbound() {
        [CREATED, REQUESTED, VALIDATED, PICKING, PICKED, CHECKING, CHECKED, PACKED, REVIEWING, DISPATCHED, CANCELED]
    }

    String getName() { return name() }

    String toString() { return name() }
}
