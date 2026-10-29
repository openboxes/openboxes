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

import org.pih.warehouse.core.Location
import org.springframework.context.ApplicationEvent

class RefreshInventorySnapshotEvent extends ApplicationEvent {

    List<String> productIds
    Boolean forceRefresh

    RefreshInventorySnapshotEvent(Location source, List<String> productIds, Boolean forceRefresh) {
        super(source)
        this.productIds = productIds
        this.forceRefresh = forceRefresh
    }
}
