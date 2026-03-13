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

        // We set both filename and filename* for maximum compatability. filename* takes priority when supported.
        // filename* is a URL encoded UTF-8 string (and so supports unicode), while filename is an Ascii-only fallback
        // option used when parsing filename* fails.
        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"${fileNameAscii}\"; filename*=UTF-8''${fileNameUtf8}")

        return this
    }

    /**
     * Sets the content type of the response.
     */
    HttpServletResponseConfigurer withContentType(HttpServletResponse response, ContentType contentType) {
        response.setContentType(contentType.mediaType.toString())
        return this
    }

    /**
     * Builds a file name with the given args and sets the headers on the response associated with returning a file.
     */
    HttpServletResponseConfigurer withFile(
            HttpServletResponse response, ContentType contentType, Collection<Object> fileNameArgs) {

        withContentType(response, contentType)

        String fileName = fileNameGenerator.generate(contentType.fileExtension, fileNameArgs)
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
                // The standard for web transfer is to represent spaces with "%20". Note that this only applies to
                // the name as used in the HTTP request. The name of the actual file that will be generated on the
                // client will contain a proper space (" ") character.
                .replace("+", "%20")
    }
}
