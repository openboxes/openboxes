/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.receiving

enum ReceiptStatusCode {

    PENDING(0),
    RECEIVED(1),
    CANCELED(1)

    int sortOrder

    ReceiptStatusCode(int sortOrder) { [this.sortOrder = sortOrder] }

    static int compare(ReceiptStatusCode a, ReceiptStatusCode b) {
        return a.sortOrder <=> b.sortOrder
    }

    static list() {
        [PENDING, RECEIVED, CANCELED]
    }


    String getName() { return name() }

    String toString() { return name() }
}