/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.requisition

enum CommodityClass {

    CONSUMABLES(0),
    MEDICATION(1),
    MIXED(2),
    COLD_CHAIN(3),
    CONTROLLED_SUBSTANCE(4),
    HAZARDOUS_MATERIAL(5),
    DURABLE(6),
    NONE(7)

    int sortOrder

    CommodityClass(int sortOrder) {
        [
                this.sortOrder = sortOrder
        ]
    }

    static int compare(CommodityClass a, CommodityClass b) {
        return a.sortOrder <=> b.sortOrder
    }

    static list() {
        [
                NONE,
                CONSUMABLES,
                MEDICATION,
                DURABLE,
                MIXED,
                COLD_CHAIN,
                CONTROLLED_SUBSTANCE,
                HAZARDOUS_MATERIAL
        ]
    }

    String toString() {
        return name()
    }
}
