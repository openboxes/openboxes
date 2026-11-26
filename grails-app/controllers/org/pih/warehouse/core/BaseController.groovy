package org.pih.warehouse.core

import java.nio.charset.StandardCharsets
import org.apache.commons.lang.StringUtils
import org.apache.poi.ss.usermodel.Workbook
import org.springframework.http.HttpStatus

import org.pih.warehouse.core.file.FileExtension
import org.pih.warehouse.core.file.FileNamePart
import org.pih.warehouse.core.file.FileNameGenerator
import org.pih.warehouse.core.http.ContentType
import org.pih.warehouse.core.http.HttpServletResponseConfigurer
import org.pih.warehouse.core.http.JsonSerializer
import org.pih.warehouse.core.http.JsonSerializerContext
import org.pih.warehouse.core.http.RenderType
import org.pih.warehouse.core.http.HttpResponseContext
import org.pih.warehouse.exporter.BulkDataExportComponentResolver
import org.pih.warehouse.exporter.BulkDataExportRequest
import org.pih.warehouse.exporter.BulkDataExporter
import org.pih.warehouse.exporter.BulkDataWriterResult
import org.pih.warehouse.importer.BulkDataType

/**
 * Base class for all Grails Controller components.
 *
 * We intentionally don't provide methods for rendering error responses. To return an error response,
 * throw an Exception, which will be processed by the {@link ErrorsController}.
 */
abstract class BaseController {

    FileNameGenerator fileNameGenerator
    JsonSerializer jsonSerializer
    HttpServletResponseConfigurer httpServletResponseConfigurer
    BulkDataExporter bulkDataExporter
    BulkDataExportComponentResolver bulkDataExportComponentResolver

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
     *  2) The value of the "format" request parameter -> /api/products?format=csv
     *  3) The extension at the end of the URI         -> /api/products.csv
     */
    void renderResponse(HttpResponseContext context) {
        if (context == null) {
            throw new RuntimeException("Response context is required to render an API response.")
        }

        switch (contentType) {
            case ContentType.JSON:
                renderJsonResponse(context.data, context.additionalFields, context.status)
                break
            case ContentType.CSV:
                renderCsvResponse(context.data, context.bulkDataType, context.fileNameArgs)
                break
            case ContentType.XLS:
                renderXlsResponse(context.data, context.bulkDataType, context.fileNameArgs)
                break
            case ContentType.XLSX:
                // TODO: Implement once ExcelWriter supports XLSX (see ExcelWriter.getSupportedContentTypes).
                throw new UnsupportedOperationException("Exporting to XLSX is not currently supported. " +
                        "Please use 'xls' instead (via the format=xls query parameter).")
            case ContentType.PDF:
                renderPdfResponse(context.data, context.htmlFile, context.fileNameArgs)
                break
            case ContentType.HTML:
                renderHtmlResponse(context.data, context.renderType, context.htmlFile)
                break
            default:
                // getContentType() should only ever return one of the content types handled above.
                throw new IllegalStateException("Unable to render a response for content type ${contentType}.")
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
     * @param bulkDataType Identifies the configuration to use when exporting the data
     * @param fileNameArgs Will be concatenated to construct the name of the file being output
     */
    void renderCsvResponse(Object data,
                           BulkDataType bulkDataType,
                           Collection<Object> fileNameArgs) {

        BulkDataWriterResult result = bulkDataExporter.exportData(new BulkDataExportRequest(
                contentType: ContentType.CSV,
                dataType: bulkDataType,
                toExport: toExportRows(data),
        ))

        try {
            Collection<Object> fileName = buildFileName(bulkDataType, ContentType.CSV, fileNameArgs)
            httpServletResponseConfigurer.withFile(response, ContentType.CSV, fileName)

            render((Map) [
                    contentType: ContentType.CSV.mediaType.toString(),
                    text: result.result as String,
                    encoding: StandardCharsets.UTF_8.name(),
            ])
        } finally {
            result.close()
        }
    }

    /**
     * Renders a response that returns an XLS Excel file.
     *
     * @param data Holds all of the data that will be used during rendering
     * @param bulkDataType Identifies the configuration to use when exporting the data
     * @param fileNameArgs Will be concatenated to construct the name of the file being output
     */
    void renderXlsResponse(Object data,
                           BulkDataType bulkDataType,
                           Collection<Object> fileNameArgs) {

        BulkDataWriterResult result = bulkDataExporter.exportData(new BulkDataExportRequest(
                contentType: ContentType.XLS,
                dataType: bulkDataType,
                toExport: toExportRows(data),
        ))

        try {
            Collection<Object> fileName = buildFileName(bulkDataType, ContentType.XLS, fileNameArgs)
            httpServletResponseConfigurer.withFile(response, ContentType.XLS, fileName)

            (result.result as Workbook).write(response.outputStream)
            response.outputStream.flush()
        } finally {
            result.close()
        }
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
     * Determines the desired content type of the response. This can be controlled by the client via:
     *  1) The HTTP Accept header                      -> Accept: text/csv
     *  2) The value of the "format" request parameter -> /api/products?format=csv
     *  3) The extension at the end of the URI         -> /api/products.csv
     *
     *  TODO: Option 1 is currently disabled via the following property: grails.mime.use.accept.header: false
     *        We should investigate re-enabling that. As a part of this, we'll need to ensure Grails plays nice with
     *        all modern browsers, which are notorious for sending complex accept headers that mess with Grails.
     */
    ContentType getContentType() {
        // https://grails.apache.org/docs/latest/guide/theWebLayer.html#contentNegotiation
        return withFormat {
            // When the client sends the "Accept: */*" header (meaning it accepts any format), the first format
            // defined here is used. We put JSON first since it's our preferred format. This is only relevant for
            // requests coming from non-browsers. All modern browsers send a variation of the following:
            // "Accept: text/html,application/xhtml+xml,application/xml" which will correctly enter the "html" block.
            json {
                return ContentType.JSON
            }
            csv {
                return ContentType.CSV
            }
            xls {
                return ContentType.XLS
            }
            xlsx {
                return ContentType.XLSX
            }
            pdf {
                return ContentType.PDF
            }
            html {
                return ContentType.HTML
            }
            // If we cannot resolve the requested format, fallback to returning JSON.
            '*' {
                return ContentType.JSON
            }
        } as ContentType
    }

    /**
     * The file name to use when returning a file and a name isn't specified in the configuration.
     */
    private Collection<Object> getDefaultFileName() {
        return ["openboxes_download", FileNamePart.CURRENT_TIME]
    }

    /**
     * Determines the parts of the name of an exported file. In order of precedence:
     *  1) The fileNameArgs given by the controller
     *  2) The fileNameArgs configured for the bulk data type (see BulkDataExportConfig/BulkDataImportExportConfig)
     *  3) The bulk data type and the current time
     *  4) The default file name
     */
    protected Collection<Object> buildFileName(BulkDataType bulkDataType,
                                               ContentType contentType,
                                               Collection<Object> fileNameArgs) {
        if (fileNameArgs) {
            return fileNameArgs
        }

        if (!bulkDataType) {
            return getDefaultFileName()
        }

        List<Object> configuredFileNameArgs = bulkDataExportComponentResolver.getFileNameArgs(bulkDataType, contentType)
        return configuredFileNameArgs ?: [bulkDataType, FileNamePart.CURRENT_TIME]
    }

    /**
     * Normalizes the data to be exported as a list of rows.
     */
    private List<Object> toExportRows(Object data) {
        if (data == null) {
            return []
        }
        return data instanceof Collection ? data as List<Object> : [data]
    }
}
