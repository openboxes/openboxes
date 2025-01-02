/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.invoice

import org.pih.warehouse.core.StatusType

enum OrderAdjustmentInvoiceStatus {

    NOT_INVOICED(0, StatusType.WARNING),
    PARTIALLY_INVOICED(10, StatusType.PRIMARY),
    INVOICED(20, StatusType.SUCCESS),

    int sortOrder
    StatusType variant

    OrderAdjustmentInvoiceStatus(int sortOrder, StatusType variant) {
        this.sortOrder = sortOrder
        this.variant = variant
    }

    static int compare(OrderAdjustmentInvoiceStatus a, OrderAdjustmentInvoiceStatus b) {
        return a.sortOrder <=> b.sortOrder
    }

    static list() {
        [NOT_INVOICED, INVOICED, PARTIALLY_INVOICED]
    }

    String toString() { return name() }

}
