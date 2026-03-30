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
    BACKORDERED(1),
    APPROVED(2),
    SUBSTITUTED(3),
    REDUCED(4),
    INCREASED(5),
    CHANGED(6),
    CANCELED(7),
    COMPLETED(8)

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
        [PENDING, BACKORDERED, APPROVED, SUBSTITUTED, REDUCED, INCREASED, CHANGED, CANCELED, COMPLETED]
    }

    String toString() {
        return name()
    }
}
