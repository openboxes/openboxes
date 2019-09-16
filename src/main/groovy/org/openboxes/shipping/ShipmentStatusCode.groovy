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

enum ShipmentStatusCode {

    CREATED(0),
    PENDING(1),
    SHIPPED(2),
    PARTIALLY_RECEIVED(3),
    RECEIVED(4)

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


    String getName() { return name() }

    String toString() { return name() }
}
