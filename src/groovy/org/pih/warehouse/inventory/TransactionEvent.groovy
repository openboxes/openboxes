/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.inventory

import org.springframework.context.ApplicationEvent

class TransactionEvent extends ApplicationEvent {

    String locationId
    List productIds
    Boolean forceRefresh = false

    TransactionEvent(Transaction source) {
        super(source)
        this.locationId = source.associatedLocation
        this.productIds = source.associatedProducts
    }

    TransactionEvent(Transaction source, Boolean forceRefresh) {
        super(source)
        this.locationId = source.associatedLocation
        this.productIds = source.associatedProducts
        this.forceRefresh = forceRefresh
    }

    TransactionEvent(Transaction source, String locationId, List<String> productIds, Boolean forceRefresh) {
        super(source)
        this.locationId = locationId
        this.productIds = productIds
        this.forceRefresh = forceRefresh
    }
}
