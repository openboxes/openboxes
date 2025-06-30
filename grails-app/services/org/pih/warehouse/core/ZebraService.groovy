package org.pih.warehouse.core

import fr.w3blog.zpl.utils.ZebraUtils
import grails.core.GrailsApplication
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder

class ZebraService {
    TemplateService templateService
    GrailsApplication grailsApplication

    def printDocument(Document document, Map model) {
        String renderedContent = templateService.renderTemplate(document, model)
        String ipAddress = grailsApplication.config.openboxes.barcode.printer.ipAddress
        Integer port = grailsApplication.config.openboxes.barcode.printer.port
        log.info "Printing to ${ipAddress}:${port}:\n${renderedContent}"
        ZebraUtils.printZpl(renderedContent, ipAddress, port)
    }

    InputStream renderDocument(Document document, Map model) {
        String body = templateService.renderTemplate(document, model)
        log.info "body: " + body
        String renderApiUrl = grailsApplication.config.openboxes.barcode.labelaryApi.url
        return postData(renderApiUrl, body)
    }

    InputStream postData(String urlString, String body) {
        def url = new URI(urlString)
        def request = new HttpPost(url)
        request.setHeader("Content-Type", "application/x-www-form-urlencoded")
        StringEntity entity = new StringEntity(body, "UTF-8")
        request.setEntity(entity)
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponse response = httpClient.execute(request)
        return response.entity.content
    }
}
