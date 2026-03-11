package org.pih.warehouse.core.http

import java.nio.charset.StandardCharsets
import javax.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component

import org.pih.warehouse.core.file.FileNameGenerator

/**
 * Wraps HttpServletResponse instances, configuring them for use in HTTP responses.
 */
@Component
class HttpServletResponseConfigurer {

    @Autowired
    FileNameGenerator fileNameGenerator

    /**
     * Sets the Content-Disposition header on the response.
     */
    HttpServletResponseConfigurer withContentDisposition(HttpServletResponse response, String fileName) {
        String fileNameAscii = formatPathAsAscii(fileName)
        String fileNameUtf8 = formatPathAsUtf8(fileName)

        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"${fileNameAscii}\"; filename*=UTF-8''${fileNameUtf8}")

        return this
    }

    /**
     * Sets the content type of the response.
     */
    HttpServletResponseConfigurer withContentType(HttpServletResponse response, ContentType contentType) {
        response.setContentType(contentType.mimeType)
        return this
    }

    /**
     * Builds a file name with the given args and sets the headers on the response associated with returning a file.
     */
    HttpServletResponseConfigurer withFile(
            HttpServletResponse response, ContentType contentType, Collection<Object> fileNameArgs) {

        withContentType(response, contentType)

        String fileName = fileNameGenerator.generate(contentType.fileType, fileNameArgs)
        withContentDisposition(response, fileName)

        return this
    }

    /**
     * Formats a given file name or path, removing all non-Ascii characters.
     */
    private formatPathAsAscii(String path) {
        return path.replaceAll("[^\\x00-\\x7F]", '')
    }

    /**
     * Formats a given file name or path as UTF-8, allowing it to support unicode characters.
     */
    private formatPathAsUtf8(String path) {
        return URLEncoder.encode(path, StandardCharsets.UTF_8.name())
                // The standard for web is to replace spaces with "%20"
                .replace("+", "%20")
    }
}
