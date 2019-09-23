/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.shipping

import org.pih.warehouse.core.Location

class ShipmentStatus implements Comparable {

    Date date
    Location location
    ShipmentStatusCode code

    String getName() { return code.getName() }

    String toString() { return getName() }

    int compareTo(obj) {
        def diff = ShipmentStatusCode.compare(this.code, obj.code)
        if (diff == 0) {
            diff = this.date <=> obj.date
        }

        return diff
    }
}
