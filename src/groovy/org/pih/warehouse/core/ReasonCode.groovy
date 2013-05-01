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
 * Reasons for changing request:
 * Stock-out
 * Low stock
 * Expired product
 * Clinical override
 * Replaced with formulary/stock item
 * Package size
 * Cancelled by requester
 * Other
 *
 * Reason for changing what was supposed to be picked:
 * Stock-out
 * Expired product
 * Damaged product
 * Could not locate
 * Other
 *
 */
public enum ReasonCode {

    STOCKOUT(0),
    LOW_STOCK(1),
    EXPIRED(2),
    DAMAGED(3),
    CLINICAL_OVERRIDE(4),
    REPLACED_BY_FORMULARY_ITEM(5),
    PACKAGE_SIZE(6),
    CANCELED_BY_REQUESTER(7),
    COULD_NOT_LOCATE(8),
    DIFFERENT_LOCATION(9),
    OTHER(10)


    final Integer sortOrder

    ReasonCode(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
	
	static listReasonCodesForRequisitionChange() {
		[STOCKOUT,LOW_STOCK,EXPIRED,CLINICAL_OVERRIDE,REPLACED_BY_FORMULARY_ITEM,PACKAGE_SIZE,CANCELED_BY_REQUESTER,DIFFERENT_LOCATION,OTHER]
	}

    static listReasonCodesForPicklistChange() {
        [STOCKOUT,EXPIRED,DAMAGED,COULD_NOT_LOCATE,OTHER]
    }
}
