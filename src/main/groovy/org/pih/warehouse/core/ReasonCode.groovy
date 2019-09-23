/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

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
enum ReasonCode {

    STOCKOUT(1),
    LOW_STOCK(2),
    EXPIRED(3),
    DAMAGED(4),
    SUBSTITUTION(5),
    PACKAGE_SIZE(6),
    CLINICAL_OVERRIDE(7),
    INSUFFICIENT_CONSUMPTION(8),
    REPLACED_BY_FORMULARY_ITEM(9),
    CANCELED_BY_REQUESTER(10),
    NON_FORMULARY_NO_SUBSTITUTION(11),
    NOT_STOCKED(12),
    AVAILABLE_STOCK_RESERVED(13),
    COULD_NOT_LOCATE(14),
    DIFFERENT_LOCATION(15),
    DATA_ENTRY_ERROR(16),
    SUPPLY_MAX_QUANTITY(17),
    NOT_ON_STOCK_LIST(18),
    INSUFFICIENT_QUANTITY_RECONDITIONED(19),
    SUBSTITUTION_WITHIN_PRODUCT_GROUP(20),
    SUPPLIED_BY_GOVERNMENT(21),
    APPROVED_CHANGE(22),
    EARLIER_EXPIRATION_DATE(23),
    CONSUMED(24),
    RETURNED(25),
    FOUND(26),
    MISSING(27),
    STOLEN(28),
    RECOUNTED(29),
    CORRECTION(30),
    SCRAPPED(31),
    REJECTED(32),
    OTHER(100)


    final Integer sortOrder

    ReasonCode(Integer sortOrder) {
        this.sortOrder = sortOrder
    }

    static list() {
        [STOCKOUT,
         LOW_STOCK,
         EXPIRED,
         DAMAGED,
         SUBSTITUTION,
         SUBSTITUTION_WITHIN_PRODUCT_GROUP,
         PACKAGE_SIZE,
         CLINICAL_OVERRIDE,
         INSUFFICIENT_CONSUMPTION,
         REPLACED_BY_FORMULARY_ITEM,
         CANCELED_BY_REQUESTER,
         NON_FORMULARY_NO_SUBSTITUTION,
         NOT_STOCKED,
         AVAILABLE_STOCK_RESERVED,
         COULD_NOT_LOCATE,
         DIFFERENT_LOCATION,
         DATA_ENTRY_ERROR,
         SUPPLY_MAX_QUANTITY,
         NOT_ON_STOCK_LIST,
         INSUFFICIENT_QUANTITY_RECONDITIONED,
         SUPPLIED_BY_GOVERNMENT,
         APPROVED_CHANGE,
         EARLIER_EXPIRATION_DATE,
         OTHER
        ]
    }

    static listDefault() {
        return [
                STOCKOUT,
                LOW_STOCK,
                PACKAGE_SIZE,
                APPROVED_CHANGE,
                EARLIER_EXPIRATION_DATE,
                SUPPLIED_BY_GOVERNMENT,
                COULD_NOT_LOCATE
        ]
    }


    static listInventoryAdjustmentReasonCodes() {
        return [
                CONSUMED,
                CORRECTION,
                DAMAGED,
                DATA_ENTRY_ERROR,
                EXPIRED,
                FOUND,
                MISSING,
                RECOUNTED,
                REJECTED,
                RETURNED,
                SCRAPPED,
                STOLEN,
                OTHER
        ]
    }

    static listRequisitionQuantityChangeReasonCodes() {
        [
                PACKAGE_SIZE,
                STOCKOUT,
                LOW_STOCK,
                DAMAGED,
                EXPIRED,
                INSUFFICIENT_CONSUMPTION,
                CANCELED_BY_REQUESTER,
                NON_FORMULARY_NO_SUBSTITUTION,
                NOT_STOCKED,
                AVAILABLE_STOCK_RESERVED,
                DATA_ENTRY_ERROR,
                SUPPLY_MAX_QUANTITY,
                NOT_ON_STOCK_LIST,
                INSUFFICIENT_QUANTITY_RECONDITIONED,
                SUPPLIED_BY_GOVERNMENT,
                OTHER
        ]
    }


    static listRequisitionSubstitutionReasonCodes() {
        [
                SUBSTITUTION,
                SUBSTITUTION_WITHIN_PRODUCT_GROUP,
                REPLACED_BY_FORMULARY_ITEM,
                STOCKOUT,
                LOW_STOCK,
                EXPIRED,
                DAMAGED,
                AVAILABLE_STOCK_RESERVED,
                DATA_ENTRY_ERROR,
                SUPPLY_MAX_QUANTITY,
                NOT_ON_STOCK_LIST,
                INSUFFICIENT_QUANTITY_RECONDITIONED,
                SUPPLIED_BY_GOVERNMENT,
                OTHER
        ]
    }

    static listPicklistQuantityChangeReasonCodes() {
        [
                STOCKOUT,
                EXPIRED,
                DAMAGED,
                COULD_NOT_LOCATE,
                DATA_ENTRY_ERROR,
                SUPPLY_MAX_QUANTITY,
                NOT_ON_STOCK_LIST,
                INSUFFICIENT_QUANTITY_RECONDITIONED,
                SUPPLIED_BY_GOVERNMENT,
                OTHER
        ]
    }
}
