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

import grails.gorm.PagedResultList

class BudgetCodeService {

    PagedResultList<BudgetCode> getBudgetCodes(BudgetCodeFilterCommand command) {
        return BudgetCode.createCriteria().list(max: command.max, offset: command.offset) {
            if (command.q) {
                ilike("code", "%" + command.q + "%")
            }
            or {
                if (command.active != null) {
                    eq("active", command.active)
                }
                if (command.includeIds) {
                    // include provided ids regardless of active filter
                    inList("id", command.includeIds)
                }
            }
            if (command.sort) {
                order(command.sort, command.order ?: 'asc')
            }
        } as PagedResultList<BudgetCode>
    }
}
