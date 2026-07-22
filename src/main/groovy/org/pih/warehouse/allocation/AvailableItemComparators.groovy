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
 * Shared item-level comparators for the rotation rules. Single source of truth so the FEFO ordering
 * is defined once.
 *
 * NOTE: {@link #BY_EXPIRATION_NULLS_LAST} duplicates the "first expiry, first out" comparator in
 * ProductAvailabilityService.sortAvailableItems() and InventoryService; a follow-up should collapse
 * those onto this constant.
 */
class AvailableItemComparators {

    private AvailableItemComparators() { }

    /** Earliest expiration date first; items without an expiration date sort last. */
    static final Comparator<AvailableItem> BY_EXPIRATION_NULLS_LAST = { AvailableItem a, AvailableItem b ->
        !a?.inventoryItem?.expirationDate ?
                !b?.inventoryItem?.expirationDate ? 0 : 1 :
                !b?.inventoryItem?.expirationDate ? -1 :
                        a?.inventoryItem?.expirationDate <=> b?.inventoryItem?.expirationDate
    } as Comparator<AvailableItem>
}
