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
 * First expired, first out: earliest expiration date first, items without an expiration date last.
 */
class FefoRotationStrategy implements RotationStrategy {

    @Override
    RotationRule getRule() {
        return RotationRule.FEFO
    }

    @Override
    List<AvailableItem> sort(List<AvailableItem> items) {
        return items ? items.sort(false, AvailableItemComparators.BY_EXPIRATION_NULLS_LAST) : []
    }
}
