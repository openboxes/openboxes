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

enum AllocationStep {

    /** Locations that hold enough stock, in source strategy order. The ordinary case. */
    AVAILABLE_STOCK(1),

    /** Locations permitted to hold a negative quantity, in source strategy order. */
    NEGATIVE_INVENTORY(2),

    /** The fallback location, which always succeeds. No suitable location exists. */
    INVENTORY_SHORTFALL(3)

    final Integer stepNumber

    AllocationStep(Integer stepNumber) { this.stepNumber = stepNumber }
}
