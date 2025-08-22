/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.core

class PaymentTerm implements Serializable {

    String id
    String code
    String name
    String description

    BigDecimal prepaymentPercent
    // Days payment due after invoice
    Integer daysToPayment

    // Audit fields
    Date dateCreated
    Date lastUpdated

    static mapping = {
        id generator: 'uuid'
    }

    static constraints = {
        code(nullable: false, blank: false)
        name(nullable: false, blank: false)
        description(nullable: true)
        prepaymentPercent(nullable: true)
        daysToPayment(nullable: true)
    }

    Map toJson() {
        return [
            id          : id,
            code        : code,
            name        : name,
            description : description,
        ]
    }
}
