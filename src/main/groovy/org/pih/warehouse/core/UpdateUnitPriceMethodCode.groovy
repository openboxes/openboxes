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

enum UpdateUnitPriceMethodCode {

    USER_DEFINED_PRICE("USER_DEFINED_PRICE"),
    AVERAGE_PURCHASE_PRICE("AVERAGE_PURCHASE_PRICE"),
    FIRST_PURCHASE_PRICE("FIRST_PURCHASE_PRICE"),
    LAST_PURCHASE_PRICE("LAST_PURCHASE_PRICE"),

    final String id

    UpdateUnitPriceMethodCode(String id) { this.id = id }

    static list() {
        [
                LAST_PURCHASE_PRICE,
                FIRST_PURCHASE_PRICE,
                USER_DEFINED_PRICE,
                AVERAGE_PURCHASE_PRICE,
        ]
    }
}
