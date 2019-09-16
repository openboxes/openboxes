/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.product

enum ProductAssociationTypeCode {

    ACCESSORY(10),
    COMPATIBLE(20),
    COMPLEMENT(30),
    COMPONENT(40),
    EQUIVALENT(50),
    INCOMPATIBLE(60),
    REPLACEMENT(80),
    SUBSTITUTE(80),
    UPGRADE(90),
    VARIANT(100),

    int sortOrder

    ProductAssociationTypeCode(int sortOrder) { [this.sortOrder = sortOrder] }

    static int compare(ProductAssociationTypeCode a, ProductAssociationTypeCode b) {
        return a.sortOrder <=> b.sortOrder
    }

    static list() {
        [ACCESSORY, COMPATIBLE, COMPLEMENT, COMPONENT, EQUIVALENT, INCOMPATIBLE, REPLACEMENT, SUBSTITUTE, UPGRADE, VARIANT]
    }

    String getName() { return name() }

    String toString() { return name() }
}
