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

import org.codehaus.groovy.grails.web.json.JSONObject

class CurrencyService {

    def grailsApplication
    def apiClientService

    def getExchangeRates() {
        JSONObject data
        try {
            String urlTemplate = grailsApplication.config.openboxes.locale.currencyApi.url
            String defaultCurrencyCode = grailsApplication.config.openboxes.locale.defaultCurrencyCode
            String url = String.format(urlTemplate, defaultCurrencyCode)
            data = apiClientService.get(url)

        } catch (Exception e) {
            log.error("Error trying to translate using translation API ", e)
            throw new ApiException("Unable to query translation API: " + e.message)
        }
        return data
    }

    def updateExchangeRates() {
        def data = getExchangeRates()
        data.rates.each { String toUom, BigDecimal value ->
            log.info "fromUom: ${data.base}, toUom: ${toUom}, value: ${value}"
            UnitOfMeasure fromUnitOfMeasure = UnitOfMeasure.findByCode(data.base)
            UnitOfMeasure toUnitOfMeasure = UnitOfMeasure.findByCode(toUom)
            if (fromUnitOfMeasure && toUnitOfMeasure) {
                UnitOfMeasureConversion uomConversion = UnitOfMeasureConversion.find("from UnitOfMeasureConversion as u " +
                        "where u.fromUnitOfMeasure = :fromUom " +
                        "and u.toUnitOfMeasure = :toUom", [fromUom: fromUnitOfMeasure, toUom: toUnitOfMeasure])
                if (!uomConversion) {
                    uomConversion = new UnitOfMeasureConversion()
                    uomConversion.active = Boolean.TRUE
                    uomConversion.fromUnitOfMeasure = fromUnitOfMeasure
                    uomConversion.toUnitOfMeasure = toUnitOfMeasure
                    uomConversion.conversionRate = value
                } else {
                    if (uomConversion.active) {
                        uomConversion.conversionRate = value
                    }
                }
                uomConversion.save(flush:true)
            }
        }
    }
}
