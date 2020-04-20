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

class TranslationService {

    def grailsApplication
    def apiClientService

    def getTranslation(String text, String source, String destination) {
        JSONObject data
        try {
            String url = grailsApplication.config.openboxes.locale.translationApi.url
            String apiKey = grailsApplication.config.openboxes.locale.translationApi.apiKey
            String format = grailsApplication.config.openboxes.locale.translationApi.format
            String lang = "$source-$destination"
            String urlFormatted = String.format(url, apiKey, text.encodeAsURL(), lang.encodeAsURL(), format)
            data = apiClientService.get(urlFormatted)

        } catch (Exception e) {
            log.error("Error trying to translate using translation API ", e)
            throw new ApiException("Unable to query translation API: ${e.message}")
        }
        return data
    }

}
