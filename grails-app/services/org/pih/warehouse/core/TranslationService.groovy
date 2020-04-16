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

class TranslationService {

    def grailsApplication

    def getTranslation(String text, String source, String destination) {
        String translation
        try {
            def email = grailsApplication.config.openboxes.translation.apiKey
            def password = grailsApplication.config.openboxes.translation.password
            String urlString = grailsApplication.config.openboxes.translation.url
                    //[source.encodeAsURL(), destination.encodeAsURL(), text.encodeAsURL(), email.encodeAsURL(), password.encodeAsURL()])


            log.info "urlString " + urlString
            def url = new URL(urlString)
            def connection = url.openConnection()
            log.info "content type; " + connection.contentType
            if (connection.responseCode == 200) {
                def xml = connection.content.text
                log.info "XML: " + xml
                def root = new XmlParser(false, true).parseText(xml)
                translation = root.translation.text()
            } else {
                log.info "connection " + connection.responseCode
                log.info "contentType" + connection.contentType
                translation = connection.content.toString()
            }
        } catch (Exception e) {
            log.error("Error trying to translate using syslang API ", e)
            throw new ApiException("Unable to query syslang API: " + e.message)
        }
        return translation
    }}
