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

import org.pih.warehouse.api.StatusCategory
import org.pih.warehouse.core.StatusType

enum OrderStatus {

    PENDING(10, StatusType.WARNING, StatusCategory.OPEN),
    APPROVED(20, StatusType.PRIMARY, StatusCategory.OPEN),
    PLACED(30, StatusType.PRIMARY, StatusCategory.OPEN),
    PARTIALLY_RECEIVED(40, StatusType.PRIMARY, StatusCategory.OPEN),
    RECEIVED(50, StatusType.PRIMARY, StatusCategory.CLOSED),
    COMPLETED(60, StatusType.SUCCESS, StatusCategory.CLOSED),
    CANCELED(70, StatusType.DANGER, StatusCategory.CLOSED),
    REJECTED(80, StatusType.DANGER, StatusCategory.CLOSED)

    int sortOrder
    StatusType variant
    StatusCategory statusCategory

    OrderStatus(int sortOrder, StatusType variant, StatusCategory statusCategory) {
        this.sortOrder = sortOrder
        this.variant = variant
        this.statusCategory = statusCategory
    }

    static List<OrderStatus> toSet(StatusCategory statusCategory) {
        if (!statusCategory) return []
        return values().findAll { it.statusCategory == statusCategory } as List<OrderStatus>
    }

    static int compare(OrderStatus a, OrderStatus b) {
        return a?.sortOrder <=> b?.sortOrder
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
