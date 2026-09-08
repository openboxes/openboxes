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

import org.pih.warehouse.core.Location

/**
 * The location the fallback resolver chose, together with the allocation step that chose it.
 */
class AllocationFallbackResolution {

    final Location binLocation

    final AllocationStep step

    AllocationFallbackResolution(Location binLocation, AllocationStep step) {
        this.binLocation = binLocation
        this.step = step
    }

    String toString() {
        return "step ${step?.stepNumber} (${step}) -> ${binLocation?.name}"
    }
}
