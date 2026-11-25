package org.pih.warehouse.core.http

import org.apache.commons.lang.StringUtils

/**
 * Holds the context required to build/render an HTTP response object.
 */
class HttpResponseContext {

    /**
     * The data to be rendered in the response.
     *
     * Represents the response body when rendering JSON.
     * Represents the "model" when rendering HTML.
     * Represents the rows when exporting bulk data as CSV, XLS/XLSX.
     */
    Object data

    /**
     * The HTTP status code to return.
     */
    int status = 200

    /**
     * The server-side rendering type to perform.
     * Only needed if we intend for the API to render HTML.
     */
    RenderType renderType

    /**
     * The path to the HTML file/view/template to use when rendering.
     * Only needed if we intend for the API to render HTML.
     */
    String htmlFile

    /**
     * Will be concatenated to construct the file name of the file being output.
     * Only needed if we are writing to a file.
     */
    Collection<Object> fileNameArgs = []

    /**
     * A map of custom fields to include in the response object.
     *
     * These fields are in addition to the automatically populated fields (such as "data" and "status"),
     * and will be added at the root level of the response.
     *
     * For example, if given an additionalFields == [myField: 0], the response will contain:
     *
     * {
     *     data: [...],
     *     status: 200,
     *     myField: 0,
     * }
     */
    Map<String, Object> additionalFields = [:]

    HttpResponseContext() {

    }

    HttpResponseContext(Map<String, Object> args) {
        // Maintain the map constructor but error if we get any keys that don't map to a field.
        args.each { key, value ->
            if (!this.hasProperty(key)) {
                throw new MissingPropertyException(
                        "HttpResponseContext does not have a field '$key'.", key, HttpResponseContext)
            }
            this."$key" = value
        }
    }

    /**
     * @return A builder object for conveniently constructing a context.
     */
    static HttpResponseContextBuilder builder() {
        return new HttpResponseContextBuilder()
    }

    static class HttpResponseContextBuilder {
        HttpResponseContext context = new HttpResponseContext()

        HttpResponseContext build() {
            return context
        }

        /**
         * For when the API should support returning a response with a body.
         *
         * Content types: JSON, XML
         */
        HttpResponseContextBuilder forResponseBody(Object data, Map<String, Object> additionalFields=[:]) {
            context.data = data
            context.additionalFields = additionalFields
            return this
        }

        /**
         * For when the API should support rendering a full UI page.
         *
         * Content types: HTML
         */
        HttpResponseContextBuilder forView(String view, Object model=null) {
            validateHtmlFileNotSet()
            context.renderType = RenderType.VIEW
            context.htmlFile = view
            context.data = model
            return this
        }

        /**
         * For when the API should support rendering a template, which is a reusable UI
         * sub-component that is embedded into a part of a View, a PDF, or used for AJAX calls.
         *
         * Content types: HTML
         */
        HttpResponseContextBuilder forTemplate(String template, Object model=null) {
            validateHtmlFileNotSet()
            context.renderType = RenderType.TEMPLATE
            context.htmlFile = template
            context.data = model
            return this
        }

        /**
         * For when the API should support rendering a styled/formatted template file.
         *
         * Content types: PDF
         */
        HttpResponseContextBuilder forTemplateFile(String template,
                                                   Object model=null,
                                                   Collection<Object> fileNameArgs=null) {
            validateHtmlFileNotSet()
            context.renderType = RenderType.TEMPLATE
            context.htmlFile = template
            context.data = model
            context.fileNameArgs = fileNameArgs
            return this
        }

        void validateHtmlFileNotSet() {
            if (context.renderType || StringUtils.isNotBlank(context.htmlFile)) {
                throw new RuntimeException("Cannot set multiple HTML files for a single API.")
            }
        }
    }
}
