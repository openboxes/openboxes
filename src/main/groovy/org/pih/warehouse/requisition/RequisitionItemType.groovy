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

enum RequisitionItemType {
    ORIGINAL(0),
    SUBSTITUTION(1),
    QUANTITY_CHANGE(2),
    PACKAGE_CHANGE(3),
    ADDITION(4)

    int sortOrder

    RequisitionItemType(int sortOrder) {
        [
                this.sortOrder = sortOrder
        ]
    }

    static int compare(RequisitionItemType a, RequisitionItemType b) {
        return a.sortOrder <=> b.sortOrder
    }

    static list() {
        [
                ORIGINAL,
                SUBSTITUTION,
                QUANTITY_CHANGE,
                PACKAGE_CHANGE,
                ADDITION
        ]
    }

    String toString() {
        return name()
    }
}
