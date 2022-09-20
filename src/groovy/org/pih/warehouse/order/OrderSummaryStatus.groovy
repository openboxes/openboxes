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

enum OrderSummaryStatus {

    PENDING('warning'),
    APPROVED('primary'),
    PLACED('primary'),
    PARTIALLY_SHIPPED('primary'),
    SHIPPED('primary'),
    PARTIALLY_RECEIVED('primary'),
    RECEIVED('primary'),
    PARTIALLY_INVOICED('primary'),
    INVOICED('primary'),
    COMPLETED('success'),
    CANCELED('danger'),
    REJECTED('danger')

    String variant

    OrderSummaryStatus(String variant) { this.variant = variant }

    static orderStatuses() {
        [PENDING, PLACED, COMPLETED, CANCELED, REJECTED]
    }

    static shipmentStatuses() {
        [PARTIALLY_SHIPPED, SHIPPED]
    }

    static receiptStatuses() {
        [PARTIALLY_RECEIVED, RECEIVED]
    }

    static paymentStatuses() {
        [PARTIALLY_INVOICED, INVOICED]
    }

    static derivedStatuses() {
        [PENDING, APPROVED, PLACED, PARTIALLY_SHIPPED, SHIPPED, PARTIALLY_RECEIVED,
         RECEIVED, PARTIALLY_INVOICED, INVOICED, COMPLETED, CANCELED, REJECTED]
    }

    String toString() { return name() }

}
