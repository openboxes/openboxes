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

import fr.w3blog.zpl.utils.ZebraUtils
import groovyx.net.http.HTTPBuilder

class ZebraService {

    boolean transactional = false

    def templateService
    def grailsApplication

    def printDocument(Document document, Map model) {
        String renderedContent = templateService.renderTemplate(document, model)
        String ipAddress = grailsApplication.config.openboxes.barcode.printer.ipAddress
        Integer port = grailsApplication.config.openboxes.barcode.printer.port
        log.info "Printing to ${ipAddress}:${port}:\n${renderedContent}"
        ZebraUtils.printZpl(renderedContent, ipAddress, port)
    }

    def renderDocument(Document document, Map model) {
        String body = templateService.renderTemplate(document, model)
        log.info "body: " + body
        String renderApiUrl = grailsApplication.config.openboxes.barcode.labelaryApi.url
        def http = new HTTPBuilder(renderApiUrl)
        return http.post(body: body)
    }

}
