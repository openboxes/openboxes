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

    CREATED(0),
    REQUESTING(1),
    REQUESTED(2),
    VALIDATING(2),
    VALIDATED(3),
    PICKING(3),
    PICKED(4),
    CHECKING(4),
    CHECKED(6),
    PACKED(7),
    REVIEWING(8),
    DISPATCHED(9),
    CANCELED(10)

    int sortOrder

    StockMovementStatusCode(int sortOrder) { [this.sortOrder = sortOrder] }

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
