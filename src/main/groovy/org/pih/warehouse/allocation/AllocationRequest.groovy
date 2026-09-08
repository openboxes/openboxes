/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.allocation

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.requisition.RequisitionItem

class AllocationRequest {
    Integer quantityRequired
    RequisitionItem requisitionItem
    AllocationMode allocationMode
    List<AvailableItem> availableItems
    List<AllocationSourceStrategy> allocationStrategies

    /**
     * Set only by the cross-dock release, once the putaway has moved the stock into the cross-dock
     * zone. Ordinary allocation must leave a backordered demand alone until then.
     */
    Boolean crossDockRelease = false
}
