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

enum InvoiceStatus {

    PENDING(0, StatusType.WARNING),  // Drafted
    INVOICED(10, StatusType.PRIMARY), // Invoice has been issued / received
    SUBMITTED(20, StatusType.PRIMARY), // Invoice has been submitted for approval
    POSTED(30, StatusType.SUCCESS),  // Invoice has been posted
    PAID(40, StatusType.PRIMARY) // Invoice has been paid

    int sortOrder
    StatusType variant

    InvoiceStatus(int sortOrder, StatusType variant) {
        this.sortOrder = sortOrder
        this.variant = variant
    }

    static int compare(InvoiceStatus a, InvoiceStatus b) {
        return a.sortOrder <=> b.sortOrder
    }

    static list() {
        [PENDING, INVOICED, SUBMITTED, POSTED, PAID]
    }

    String toString() { return name() }

}
