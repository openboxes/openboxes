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

import grails.web.servlet.mvc.GrailsParameterMap

class BudgetCodeService {
    
    def getBudgetCodes(GrailsParameterMap params) {
        return BudgetCode.createCriteria().list(max: params.max, offset: params.offset) {
            if (params.q) {
                ilike("code", "%" + params.q + "%")
            }
        }
    }
}
