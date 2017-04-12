/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.core;

/**
 *
 */
enum LocationTypeCode {

    // Storage
    DEPOT(10),          // storage location
    BIN_LOCATION(20),   // sub-location under depot

    // Usage
    DISPENSARY(30),     // internal endpoint location where stock is dispensed (limited storage) eg. pharmacy
    WARD(40),           // internal endpoint location where stock is administered (limited storage) eg. surgery theater

    // Supplier
    SUPPLIER(50),       // external endpoint location where stock is received through PO workflow
    DONOR(60),          // external endpoint location where stock is received with no obligation for payment

    // Virtual
    VIRTUAL(100),       // virtual location where stock is sent when appropriate physical location type does not exist e.g. incinerator, damaged, repairs

    final Integer sortOrder

    LocationTypeCode(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    static list() {
        [DEPOT, DISPENSARY, WARD, BIN_LOCATION, VIRTUAL]
    }

}
