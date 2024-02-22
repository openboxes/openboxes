/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api

import grails.converters.JSON
import org.pih.warehouse.core.UnitOfMeasureType

class UnitOfMeasureApiController extends BaseDomainApiController {

    def uomService

    def currencies() {
        def currencies = uomService.getCurrencies()
        render ([data:currencies] as JSON)
     }

    def uomOptions() {
        UnitOfMeasureType uomType = UnitOfMeasureType.valueOf(params.type)
        List<Map<String, String>> uoms = uomService.getUoms(uomType).collect {
            [
                id: it.id,
                value: it.id,
                label: it.name
            ]
        }

        render([data: uoms] as JSON)
    }
}
