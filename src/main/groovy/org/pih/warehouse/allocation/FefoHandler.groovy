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
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product

/**
 * First expired, first out: earliest expiration date first, items without an expiration date last.
 */
class FefoHandler implements AllocationStrategyHandler {

    @Override
    AllocationStrategy getStrategy() {
        return AllocationStrategy.FEFO
    }

    @Override
    List<AvailableItem> order(Location facility, Product product, List<AvailableItem> availableItems) {
        // The items most likely already arrive sorted by expiration date, but we sort it again to make sure
        // they are sorted by expiration date
        return availableItems?.sort(false) { a, b ->
            !a?.inventoryItem?.expirationDate ?
                    !b?.inventoryItem?.expirationDate ? 0 : 1 :
                    !b?.inventoryItem?.expirationDate ? -1 :
                            a?.inventoryItem?.expirationDate <=> b?.inventoryItem?.expirationDate
        }
    }
}
