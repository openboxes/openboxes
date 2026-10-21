package org.pih.warehouse.core

import fr.w3blog.zpl.utils.ZebraUtils
import grails.core.GrailsApplication
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.fluent.Request
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.springframework.beans.BeansException

class ZebraService {

    // Labelary (https://labelary.com) renders ZPL as an image for label previews. The URLs are
    // configured under openboxes.barcode.labelaryApi; these are the defaults when a key is not set.
    static final String DEFAULT_LABELARY_URL = "https://api.labelary.com/v1/printers/12dpmm/labels/2x1/0/"
    static final String DEFAULT_LABELARY_DOCUMENT_PREVIEW_URL = "https://api.labelary.com/v1/printers/8dpmm/labels/3x2/0/"
    static final String DEFAULT_LABELARY_VIEWER_URL = "https://labelary.com/viewer.html"

    BeanPropertyTemplateService beanPropertyTemplateService
    GrailsApplication grailsApplication

    String renderZpl(Document document, Map model) {
        if (document?.documentType?.documentCode != DocumentCode.ZEBRA_TEMPLATE) {
            throw new LabelTemplateException(document, "only documents of type ZEBRA_TEMPLATE can be rendered as Zebra templates")
        }
        try {
            return beanPropertyTemplateService.renderTemplate(document, model)
        } catch (IllegalArgumentException | BeansException e) {
            throw new LabelTemplateException(document, e)
        }
    }

    def printDocument(Document document, Map model) {
        String renderedContent = renderZpl(document, model)
        String ipAddress = grailsApplication.config.openboxes.barcode.printer.ipAddress
        Integer port = grailsApplication.config.openboxes.barcode.printer.port
        log.info "Printing to ${ipAddress}:${port}:\n${renderedContent}"
        ZebraUtils.printZpl(renderedContent, ipAddress, port)
    }

    /**
     * Renders the label image of a Zebra template through openboxes.barcode.labelaryApi.url
     * (used by the container, bin location and product label endpoints).
     */
    InputStream renderDocument(Document document, Map model) {
        String body = renderZpl(document, model)
        assertLabelaryEnabled()
        log.info "body: " + body
        return postData(getLabelaryUrl("url", DEFAULT_LABELARY_URL), body)
    }

    /**
     * Renders the label image of already rendered ZPL through
     * openboxes.barcode.labelaryApi.documentPreviewUrl (used by the document label preview).
     */
    InputStream renderPreview(String zpl) {
        assertLabelaryEnabled()
        return requestImage(getLabelaryUrl("documentPreviewUrl", DEFAULT_LABELARY_DOCUMENT_PREVIEW_URL), zpl)
    }

    /**
     * The link that opens already rendered ZPL in the Labelary viewer
     * (openboxes.barcode.labelaryApi.viewerUrl), with the ZPL URL-encoded.
     */
    String getViewerUrl(String zpl) {
        assertLabelaryEnabled()
        String viewerUrl = getLabelaryUrl("viewerUrl", DEFAULT_LABELARY_VIEWER_URL)
        String encodedZpl = URLEncoder.encode(zpl ?: "", "UTF-8").replace("+", "%20")
        return viewerUrl + (viewerUrl.contains("?") ? "&" : "?") + "zpl=" + encodedZpl
    }

    /**
     * Whether label previews may use Labelary (openboxes.barcode.labelaryApi.enabled, true unless set to false).
     */
    boolean isLabelaryEnabled() {
        Object enabled = getLabelaryConfig("enabled")
        return enabled == null || !"false".equalsIgnoreCase(enabled.toString().trim())
    }

    private void assertLabelaryEnabled() {
        if (!labelaryEnabled) {
            throw new LabelPreviewDisabledException()
        }
    }

    private String getLabelaryUrl(String name, String defaultUrl) {
        Object url = getLabelaryConfig(name)
        return url != null ? url.toString().trim() : defaultUrl
    }

    private Object getLabelaryConfig(String name) {
        Object value = grailsApplication.config.openboxes.barcode.labelaryApi."${name}"
        // a key that is not set navigates to an empty map
        if (value == null || value instanceof Map || value.toString().trim().isEmpty()) {
            return null
        }
        return value
    }

    InputStream requestImage(String urlString, String zpl) {
        return Request.Post(urlString)
            .bodyString(zpl, ContentType.APPLICATION_FORM_URLENCODED)
            .execute()
            .returnContent()
            .asStream()
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
