/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.inventory

import org.pih.warehouse.core.Constants

class TransactionType implements Serializable {

    String id
    String name
    String description
    Integer sortOrder = 0
    Date dateCreated
    Date lastUpdated
    TransactionCode transactionCode

    static constraints = {
        name(nullable: false, maxSize: 255)
        description(nullable: true, maxSize: 255)
        sortOrder(nullable: true)
        transactionCode(nullable: false)
        dateCreated(display: false)
        lastUpdated(display: false)
    }

    static mapping = {
        id generator: 'uuid'
        sort "sortOrder"
    }

    Boolean compareName(String transactionTypeName) {
        String regex = "\\${Constants.LOCALIZED_STRING_SEPARATOR}"
        return name.split(regex)[0] == transactionTypeName.split(regex)[0]
    }
}
