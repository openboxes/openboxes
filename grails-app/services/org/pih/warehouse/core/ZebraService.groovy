package org.pih.warehouse.core

import fr.w3blog.zpl.utils.ZebraUtils

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

        return postData(renderApiUrl, body)
    }

    private Map postData(String urlString, String body) {
        // TODO: to be tested once we got zebra document type on the server
        try {
            def url = new URL(urlString)
            def connection = (HttpURLConnection) url.openConnection()
            connection.setRequestMethod("POST")
            connection.setDoOutput(true)
            connection.setRequestProperty("Content-Type", "application/json")

            connection.outputStream.withWriter("UTF-8") { writer ->
                writer.write(body)
            }

            def responseCode = connection.responseCode
            def responseMessage = connection.inputStream.text

            return [status: responseCode, body: responseMessage]
        } catch (Exception e) {
            log.error "Error making HTTP POST request to ${urlString}: ${e.message}"
            return [status: 500, body: "Error: ${e.message}"]
        }
    }
}
