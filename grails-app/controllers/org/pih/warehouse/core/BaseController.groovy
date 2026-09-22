package org.pih.warehouse.core

import java.nio.charset.StandardCharsets
import java.time.Instant
import org.apache.commons.lang.StringUtils
import org.springframework.http.HttpStatus

import org.pih.warehouse.core.file.FileExtension
import org.pih.warehouse.core.file.FileNameGenerator
import org.pih.warehouse.core.http.ContentType
import org.pih.warehouse.core.http.JsonSerializer
import org.pih.warehouse.core.http.JsonSerializerContext
import org.pih.warehouse.core.http.RenderType
import org.pih.warehouse.core.http.HttpResponseContext

/**
 * Base class for all Grails Controller components.
 *
 * We intentionally don't provide methods for rendering error responses. To return an error response,
 * throw an Exception, which will be processed by the {@link ErrorsController}.
 */
abstract class BaseController {

    FileNameGenerator fileNameGenerator
    JsonSerializer jsonSerializer

    /**
     * Returns an empty 204 NO RESPONSE status.
     */
    void renderNoContentResponse() {
        render((Map) [status: HttpStatus.NO_CONTENT.value()])
    }

    /**
     * Renders an HTTP API response. Convenience method for when using a Map to define the context.
     */
    void renderResponse(Map contextParams) {
        // Safe to do because will throw an error if we're given any fields that don't exist in the context.
        renderResponse(new HttpResponseContext(contextParams))
    }

    /**
     * Renders an HTTP API response. Convenience method for when using the context builder.
     */
    void renderResponse(HttpResponseContext.HttpResponseContextBuilder contextBuilder) {
        renderResponse(contextBuilder.build())
    }

    /**
     * Renders an HTTP API response. Convenience method for when we just want to return some data in a standard way.
     */
    void renderResponse(Object data) {
        renderResponse(new HttpResponseContext(data: data))
    }

    /**
     * Renders an HTTP API response.
     *
     * When possible, use this method instead of the individual renderXResponse methods because it allows the client
     * to control what content-type they receive.
     *
     * The renderer/content-type that will be used can be controlled by the client via:
     *  1) The HTTP Accept header                      -> Accept: text/csv
     *  2) The value of the "format" request parameter -> /api/products?format=CSV
     *  3) The extension at the end of the URI         -> /api/products.csv
     */
    void renderResponse(HttpResponseContext context) {
        if (context == null) {
            throw new RuntimeException("Response context is required to render an API response.")
        }

        // https://grails.apache.org/docs/latest/guide/theWebLayer.html#contentNegotiation
        withFormat {
            // When the client sends the "Accept: */*" header (meaning it accepts any format), the first format
            // defined here is used. We put JSON first since it's our preferred format. This is only relevant for
            // requests coming from non-browsers. All modern browsers send a variation of the following:
            // "Accept: text/html,application/xhtml+xml,application/xml" which will correctly enter the "html" block.
            json {
                renderJsonResponse(context.data, context.additionalFields, context.status)
            }
            csv {
                renderCsvResponse(context.data)
            }
            xls {
                renderXlsResponse(context.data)
            }
            xlsx {
                renderXlsxResponse(context.data)
            }
            pdf {
                renderPdfResponse(context.data, context.htmlFile, context.fileNameArgs)
            }
            html {
                renderHtmlResponse(context.data, context.renderType, context.htmlFile)
            }
            // If we cannot resolve the requested format, fallback to returning JSON.
            '*' {
                renderJsonResponse(context.data, context.additionalFields, context.status)
            }
        }
    }

    /**
     * Renders a response that returns a JSON message body.
     *
     * @param data Holds all of the data that will be used during rendering
     * @param additionalFields A map of additional fields to include in the root level of the response.
     * @param status The HTTP status code to return. We expect non-error codes only.
     */
    void renderJsonResponse(Object data, Map<String, Object> additionalFields=[:], int status=HttpStatus.OK.value()) {
        render((Map) [
                text: jsonSerializer.serialize(data, new JsonSerializerContext(
                        additionalFields: additionalFields,
                        status: status,
                )),
                contentType: ContentType.JSON.mediaType.toString(),
                encoding: StandardCharsets.UTF_8.name(),
      ])
    }

    /**
     * Renders a response that returns a CSV file.
     *
     * @param data Holds all of the data that will be used during rendering
     * @param config Configuration for how the file should be written.
     */
    void renderCsvResponse(Object data) {
        // TODO: to be implemented
        throw new RuntimeException("This endpoint does not support formatting to CSV.")
    }

    /**
     * Renders a response that returns an XLS Excel file.
     *
     * @param data Holds all of the data that will be used during rendering
     * @param config Configuration for how the file should be written.
     */
    void renderXlsResponse(Object data) {
        // TODO: to be implemented
        throw new RuntimeException("This endpoint does not support formatting to XLS.")
    }

    /**
     * Renders a response that returns an XLSX Excel file.
     *
     * @param data Holds all of the data that will be used during rendering
     * @param config Configuration for how the file should be written.
     */
    void renderXlsxResponse(Object data) {
        // TODO: to be implemented
        throw new RuntimeException("This endpoint does not support formatting to XLSX.")
    }

    /**
     * Renders a response that returns a PDF.
     *
     * @param model Holds all of the data that will be used during rendering
     * @param template The path to the template to be rendered
     * @param fileNameArgs Will be concatenated to construct the file name of the file being output
     */
    void renderPdfResponse(Object model, String template, Collection<Object> fileNameArgs) {
        if (StringUtils.isBlank(template)) {
            throw new RuntimeException("This endpoint does not support formatting to PDF.")
        }

        renderPdf(
                model: model,
                template: template,
                filename: fileNameGenerator.generate(FileExtension.PDF, fileNameArgs ?: defaultFileName),
        )
    }

    /**
     * Renders a response that returns HTML.
     *
     * @param model Holds all of the data that will be used during rendering
     * @param renderType The type of server-side rendering to perform
     * @param htmlFile The path to the view/template to be rendered
     */
    void renderHtmlResponse(Object model, RenderType renderType, String htmlFile) {
        if (StringUtils.isBlank(htmlFile)) {
            throw new RuntimeException("This endpoint does not support formatting to HTML.")
        }

        render((Map) [
                model: model,
                template: renderType == RenderType.TEMPLATE ? htmlFile : null,
                view: renderType == RenderType.VIEW ? htmlFile : null,
        ])
    }

    /**
     * The file name to use when returning a file and a name isn't specified in the configuration.
     */
    private Collection<Object> getDefaultFileName() {
        return ["openboxes_download", Instant.now()]
    }
}
