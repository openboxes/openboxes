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

import org.pih.warehouse.core.StatusType

enum OrderSummaryStatus {

    PENDING(StatusType.WARNING),
    APPROVED(StatusType.PRIMARY),
    PLACED(StatusType.PRIMARY),
    PARTIALLY_SHIPPED(StatusType.PRIMARY),
    SHIPPED(StatusType.PRIMARY),
    PARTIALLY_RECEIVED(StatusType.PRIMARY),
    RECEIVED(StatusType.PRIMARY),
    PARTIALLY_INVOICED(StatusType.PRIMARY),
    INVOICED(StatusType.PRIMARY),
    COMPLETED(StatusType.SUCCESS),
    CANCELED(StatusType.DANGER),
    REJECTED(StatusType.DANGER)

    StatusType variant

    OrderSummaryStatus(StatusType variant) { this.variant = variant }

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
