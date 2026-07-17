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

/**
 * Warehouse first: preferred warehouse bins, then remaining warehouse bins, then display bins.
 */
class WarehouseFirstHandler extends AbstractAllocationStrategyHandler {

    @Override
    AllocationStrategy getStrategy() {
        return AllocationStrategy.WAREHOUSE_FIRST
    }

    @Override
    protected List<AvailableItem> orderGroups(AvailableItemGroups groups) {
        return groups.preferredWarehouseItems + groups.remainingWarehouseItems + groups.displayItems
    }
}
