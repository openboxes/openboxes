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

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import org.apache.http.client.utils.URIBuilder
import org.grails.web.json.JSONObject

class CurrencyService {

    GrailsApplication grailsApplication
    def apiClientService

    def getExchangeRates() {
        JSONObject data
        try {
            String urlTemplate = grailsApplication.config.openboxes.locale.currencyApi.url
            String apiKey = grailsApplication.config.openboxes.locale.currencyApi.apiKey
            String apiKeyParameter = grailsApplication.config.openboxes.locale.currencyApi.apiKeyParameter
            String defaultCurrencyCode = grailsApplication.config.openboxes.locale.defaultCurrencyCode
            String url = buildUrl(urlTemplate, defaultCurrencyCode, apiKey, apiKeyParameter)
            data = apiClientService.get(url)
        } catch (Exception e) {
            log.error("Error trying to fetch exchange rates from the currency API ", e)
            throw new ApiException("Unable to query currency API: " + e.message)
        }
        if (!data?.rates) {
            // e.g. {"success": false, "error": {"type": "missing_access_key", ...}}
            String reason = data?.error ? data.error.toString() : "empty response"
            log.error("Currency API returned no exchange rates: ${reason}")
            throw new ApiException("Currency API returned no exchange rates: " + reason)
        }
        return data
    }

    /**
     * Fills the base currency into the configured URL template and, when an
     * API key is configured, sets it as the query parameter named by
     * apiKeyParameter (provider-specific, so it lives in the config too).
     */
    static String buildUrl(String urlTemplate, String baseCurrencyCode, String apiKey, String apiKeyParameter) {
        URIBuilder builder = new URIBuilder(String.format(urlTemplate, baseCurrencyCode))
        return (apiKey && apiKeyParameter ? builder.setParameter(apiKeyParameter, apiKey) : builder).toString()
    }

    @Transactional
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
