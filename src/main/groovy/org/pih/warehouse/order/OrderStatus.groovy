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

import org.pih.warehouse.core.StatusType

enum OrderStatus {

    PENDING(10, StatusType.WARNING),
    APPROVED(20, StatusType.PRIMARY),
    PLACED(30, StatusType.PRIMARY),
    PARTIALLY_RECEIVED(40, StatusType.PRIMARY),
    RECEIVED(50, StatusType.PRIMARY),
    COMPLETED(60, StatusType.SUCCESS),
    CANCELED(70, StatusType.DANGER),
    REJECTED(80, StatusType.DANGER)

    int sortOrder
    StatusType variant

    OrderStatus(int sortOrder, StatusType variant) {
        this.sortOrder = sortOrder
        this.variant = variant
    }

    static int compare(OrderStatus a, OrderStatus b) {
        return a.sortOrder <=> b.sortOrder
    }

    static list() {
        [PENDING, APPROVED, PLACED, PARTIALLY_RECEIVED, RECEIVED, COMPLETED, CANCELED, REJECTED]
    }

    static listStockTransfer() {
        [PENDING, APPROVED, COMPLETED, CANCELED]
    }

    static listPending() {
        [PENDING, APPROVED, PLACED, PARTIALLY_RECEIVED]
    }

    String toString() { return name() }

}
