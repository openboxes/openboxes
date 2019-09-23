/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.requisition

enum RequisitionItemStatus {
    PENDING(0),
    APPROVED(1),
    SUBSTITUTED(2),
    CHANGED(3),
    CANCELED(4),
    COMPLETED(5)

    int sortOrder

    RequisitionItemStatus(int sortOrder) {
        [
                this.sortOrder = sortOrder
        ]
    }

    static int compare(RequisitionItemStatus a, RequisitionItemStatus b) {
        return a.sortOrder <=> b.sortOrder
    }

    static list() {
        [PENDING, APPROVED, SUBSTITUTED, CHANGED, CANCELED, COMPLETED]
    }

    String toString() {
        return name()
    }
}
