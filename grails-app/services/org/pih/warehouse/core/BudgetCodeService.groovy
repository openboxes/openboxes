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

class BudgetCodeService {

    List<BudgetCode> getBudgetCodes(Map params) {
        return BudgetCode.createCriteria().list(max: params.max, offset: params.offset) {
            if (params.q) {
                ilike("code", "%" + params.q + "%")
            }
            if (params.active != null || params.includeIds) {
                or {
                    if (params.active != null) {
                        eq("active", params.active?.toBoolean())
                    }
                    if (params.includeIds) {
                        // include provided ids regardless of active filter
                        'in'("id", params.includeIds)
                    }
                }
            }
            if (params.sort) {
                order(params.sort, params.order ?: 'asc')
            }
        } as List<BudgetCode>
    }
}
