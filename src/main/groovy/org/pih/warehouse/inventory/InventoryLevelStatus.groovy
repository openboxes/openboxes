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

enum InventoryLevelStatus {

    IN_STOCK(0),
    OUT_OF_STOCK(1),
    NEVER_IN_STOCK(2),
    BELOW_MINIMUM(3),
    BELOW_REORDER(4),
    BELOW_MAXIMUM(5),
    ABOVE_MAXIMUM(6)

    int sortOrder

    InventoryLevelStatus(int sortOrder) { [this.sortOrder = sortOrder] }

    static int compare(InventoryLevelStatus a, InventoryLevelStatus b) {
        return a.sortOrder <=> b.sortOrder
    }

    static list() {
       return [IN_STOCK, OUT_OF_STOCK, NEVER_IN_STOCK, BELOW_MINIMUM, BELOW_REORDER, BELOW_MAXIMUM, ABOVE_MAXIMUM]
    }

    static listReplenishmentOptions() {
        return [BELOW_MINIMUM, BELOW_REORDER, BELOW_MAXIMUM]
    }

    String toString() { return name() }

}
