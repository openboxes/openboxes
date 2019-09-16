/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.order

enum OrderStatus {

    PENDING(1),
    PLACED(2),
    PARTIALLY_RECEIVED(3),
    RECEIVED(4),
    COMPLETED(5),
    CANCELED(6)

    int sortOrder

    OrderStatus(int sortOrder) { [this.sortOrder = sortOrder] }

    static int compare(OrderStatus a, OrderStatus b) {
        return a.sortOrder <=> b.sortOrder
    }

    static list() {
        [PENDING, PLACED, PARTIALLY_RECEIVED, RECEIVED, COMPLETED, CANCELED]
    }

    String toString() { return name() }

}
