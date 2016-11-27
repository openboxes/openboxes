/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.core;

/**
 * Reasons for adjusting stock quantity:
 */
public enum AdjustmentReasonCode {

    CONSUMED(10),
    COUNT_ERROR(20),
    DAMAGED(30),
    DATA_ENTRY_ERROR(40),
    FOUND(50),
    MISSING(60),
    RETURNED(70),
    STORAGE_ERROR(80),
    SYSTEM_ERROR(90),
    THEFT(100),
    WASTAGE(110),
    OTHER(1000)


    final Integer sortOrder

    AdjustmentReasonCode(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    static list() {
        [CONSUMED, COUNT_ERROR, DAMAGED, DATA_ENTRY_ERROR, FOUND, MISSING, RETURNED, STORAGE_ERROR, SYSTEM_ERROR, THEFT, CONSUMED, WASTAGE, OTHER]
    }


}
